package com.cjbooms.fabrikt.generators.controller

import com.cjbooms.fabrikt.cli.ControllerCodeGenOptionType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.GeneratorUtils.toIncomingParameters
import com.cjbooms.fabrikt.generators.GeneratorUtils.toKdoc
import com.cjbooms.fabrikt.generators.ValidationAnnotations
import com.cjbooms.fabrikt.generators.controller.ControllerGeneratorUtils.SecuritySupport
import com.cjbooms.fabrikt.generators.controller.ControllerGeneratorUtils.toSuccessResponseType
import com.cjbooms.fabrikt.generators.controller.ControllerGeneratorUtils.methodName
import com.cjbooms.fabrikt.generators.controller.ControllerGeneratorUtils.securitySupport
import com.cjbooms.fabrikt.generators.controller.metadata.MicronautImports
import com.cjbooms.fabrikt.generators.controller.metadata.MicronautImports.SECURITY_RULE_IS_ANONYMOUS
import com.cjbooms.fabrikt.generators.controller.metadata.MicronautImports.SECURITY_RULE_IS_AUTHENTICATED
import com.cjbooms.fabrikt.model.BodyParameter
import com.cjbooms.fabrikt.model.ControllerLibraryType
import com.cjbooms.fabrikt.model.ControllerType
import com.cjbooms.fabrikt.model.HeaderParam
import com.cjbooms.fabrikt.model.KotlinTypes
import com.cjbooms.fabrikt.model.PathParam
import com.cjbooms.fabrikt.model.QueryParam
import com.cjbooms.fabrikt.model.RequestParameter
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.FileUtils.addFileDisclaimer
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isSingleResource
import com.cjbooms.fabrikt.util.KaizenParserExtensions.routeToPaths
import com.reprezen.kaizen.oasparser.model3.Operation
import com.reprezen.kaizen.oasparser.model3.Path
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec

class MicronautControllerInterfaceGenerator(
    private val packages: Packages,
    private val api: SourceApi,
    private val validationAnnotations: ValidationAnnotations,
    private val options: Set<ControllerCodeGenOptionType> = emptySet(),
) : ControllerInterfaceGenerator, AnnotationBasedControllerInterfaceGenerator(packages, api, validationAnnotations) {

    private val useSuspendModifier: Boolean
        get() = options.any { it == ControllerCodeGenOptionType.SUSPEND_MODIFIER }

    private val addAuthenticationParameter: Boolean
        get() = options.any { it == ControllerCodeGenOptionType.AUTHENTICATION }

    override fun generate(): MicronautControllers =
        MicronautControllers(
            api.openApi3.routeToPaths().map { (resourceName, paths) ->
                buildController(resourceName, paths.values)
            }.toSet(),
            addAuthenticationParameter,
        )

    override fun generateLibrary(): Collection<ControllerLibraryType> = emptySet()

    override fun controllerBuilder(
        className: String,
        basePath: String,
    ) =
        TypeSpec.interfaceBuilder(className)
            .addAnnotation(
                AnnotationSpec
                    .builder(MicronautImports.CONTROLLER)
                    .build(),
            )

    override fun buildFunction(
        path: Path,
        op: Operation,
        verb: String,
    ): FunSpec {
        val methodName = methodName(op, verb, path.pathString.isSingleResource())
        val returnType = MicronautImports.RESPONSE.parameterizedBy(op.toSuccessResponseType(packages.base))
        val parameters = op.toIncomingParameters(packages.base, path.parameters, emptyList())
        val globalSecurity = this.api.openApi3.securityRequirements.securitySupport()

        // Main method builder
        val funcSpec = FunSpec
            .builder(methodName)
            .addModifiers(KModifier.ABSTRACT)
            .addKdoc(op.toKdoc(parameters))
            .addMicronautFunAnnotation(op, verb, path.pathString)
            .apply {
                if (useSuspendModifier) {
                    addModifiers(KModifier.SUSPEND)
                }
            }
            .returns(returnType)

        // Function parameters
        parameters
            .map {
                when (it) {
                    is BodyParameter ->
                        it
                            .toParameterSpecBuilder()
                            .addAnnotation(
                                AnnotationSpec
                                    .builder(MicronautImports.BODY).build(),
                            )
                            .maybeAddAnnotation(validationAnnotations.parameterValid())
                            .build()

                    is RequestParameter ->
                        it
                            .toParameterSpecBuilder()
                            .addValidationAnnotations(it)
                            .addMicronautParamAnnotation(it)
                            .build()
                }
            }
            .forEach { funcSpec.addParameter(it) }

        // Add authentication
        if (addAuthenticationParameter) {
            val securityOption = op.securitySupport(globalSecurity)

            if (securityOption.allowsAuthenticated) {
                val typeName =
                    MicronautImports.AUTHENTICATION
                        .copy(nullable = securityOption == SecuritySupport.AUTHENTICATION_OPTIONAL)
                funcSpec.addParameter(
                    ParameterSpec
                        .builder("authentication", typeName)
                        .build(),
                )
            }
        }

        return funcSpec.build()
    }

    private fun FunSpec.Builder.addMicronautFunAnnotation(op: Operation, verb: String, path: String): FunSpec.Builder {
        val globalSecurity =
            api.openApi3.securityRequirements.securitySupport()

        val produces = op.responses
            .flatMap { it.value.contentMediaTypes.keys }
            .toTypedArray()

        val consumes = op.requestBody
            .contentMediaTypes.keys
            .toTypedArray()

        this.addAnnotation(
            AnnotationSpec
                .builder(MicronautImports.HttpMethods.byName(verb))
                .addMember("uri = %S", path).build(),
        )

        if (consumes.isNotEmpty()) {
            this.addAnnotation(
                AnnotationSpec
                    .builder(MicronautImports.CONSUMES)
                    .addMember(
                        "value = %L",
                        consumes.joinToString(
                            prefix = "[",
                            postfix = "]",
                            separator = ", ",
                            transform = { "\"$it\"" },
                        ),
                    )
                    .build(),
            )
        }

        if (produces.isNotEmpty()) {
            this.addAnnotation(
                AnnotationSpec
                    .builder(MicronautImports.PRODUCES)
                    .addMember(
                        "value = %L",
                        produces.joinToString(
                            prefix = "[",
                            postfix = "]",
                            separator = ", ",
                            transform = { "\"$it\"" },
                        ),
                    )
                    .build(),
            )
        }

        if (addAuthenticationParameter) {
            val securityRule = when (op.securitySupport(globalSecurity)) {
                SecuritySupport.AUTHENTICATION_REQUIRED -> SECURITY_RULE_IS_AUTHENTICATED
                SecuritySupport.AUTHENTICATION_PROHIBITED -> SECURITY_RULE_IS_ANONYMOUS
                SecuritySupport.AUTHENTICATION_OPTIONAL -> "$SECURITY_RULE_IS_AUTHENTICATED, $SECURITY_RULE_IS_ANONYMOUS"
                else -> ""
            }

            if (securityRule != "") {
                this.addAnnotation(
                    AnnotationSpec
                        .builder(MicronautImports.SECURED)
                        .addMember(
                            securityRule,
                        )
                        .build(),
                )
            }
        }

        return this
    }

    private fun ParameterSpec.Builder.addMicronautParamAnnotation(parameter: RequestParameter): ParameterSpec.Builder =
        when (parameter.parameterLocation) {
            QueryParam ->
                AnnotationSpec
                    .builder(MicronautImports.QUERY_VALUE)

            HeaderParam ->
                AnnotationSpec
                    .builder(MicronautImports.HEADER)

            PathParam ->
                AnnotationSpec
                    .builder(MicronautImports.PATH_VARIABLE)
        }.let {
            it.addMember("value = %S", parameter.oasName)

            if (parameter.defaultValue != null) {
                it.addMember("defaultValue = %S", parameter.defaultValue)
            }
            this.addAnnotation(it.build())
        }
}

data class MicronautControllers(
    val controllers: Collection<ControllerType>,
    val addAuthenticationParameter: Boolean
) : KotlinTypes(controllers) {

    override val files: Collection<FileSpec> = super.files.map {
        it.toBuilder()
            .addFileDisclaimer()
            .apply {
                if (addAuthenticationParameter) {
                    addImport(MicronautImports.SECURITY_RULE.first, MicronautImports.SECURITY_RULE.second)
                }
            }
            .build()
    }
}
