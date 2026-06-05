package com.cjbooms.fabrikt.generators.controller

import com.cjbooms.fabrikt.cli.ControllerCodeGenOptionType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.GeneratorUtils.groupingStrategyFrom
import com.cjbooms.fabrikt.generators.GeneratorUtils.toIncomingParameters
import com.cjbooms.fabrikt.generators.GeneratorUtils.toKdoc
import com.cjbooms.fabrikt.generators.ValidationAnnotations
import com.cjbooms.fabrikt.generators.controller.ControllerGeneratorUtils.isSseResponse
import com.cjbooms.fabrikt.generators.controller.ControllerGeneratorUtils.methodName
import com.cjbooms.fabrikt.generators.controller.ControllerGeneratorUtils.securitySupport
import com.cjbooms.fabrikt.generators.controller.ControllerGeneratorUtils.toSuccessResponseType
import com.cjbooms.fabrikt.generators.controller.metadata.SpringAnnotations
import com.cjbooms.fabrikt.generators.controller.metadata.SpringImports
import com.cjbooms.fabrikt.model.*
import com.cjbooms.fabrikt.util.FileUtils.addFileDisclaimer
import com.cjbooms.fabrikt.util.GroupingStrategy
import com.cjbooms.fabrikt.util.KaizenParserExtensions.groupedPaths
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isSingleResource
import com.cjbooms.fabrikt.util.toUpperCase
import com.reprezen.kaizen.oasparser.model3.Operation
import com.reprezen.kaizen.oasparser.model3.Path
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

class SpringControllerInterfaceGenerator(
    private val packages: Packages,
    private val api: SourceApi,
    private val validationAnnotations: ValidationAnnotations,
    private val options: Set<ControllerCodeGenOptionType> = emptySet(),
) : ControllerInterfaceGenerator, AnnotationBasedControllerInterfaceGenerator(packages, api, validationAnnotations) {

    companion object {
        private const val EXTENSION_ASYNC_SUPPORT = "x-async-support"
    }

    private val addAuthenticationParameter: Boolean
        get() = options.any { it == ControllerCodeGenOptionType.AUTHENTICATION }
    private val groupingStrategy: GroupingStrategy
        get() = groupingStrategyFrom(options)

    override fun generate(): SpringControllers =
        SpringControllers(
            api.openApi3.groupedPaths(groupingStrategy).map { (resourceName, paths) ->
                buildController(resourceName, paths.values)
            }.toSet(),
        )

    override fun generateLibrary(): Collection<ControllerLibraryType> = emptySet()

    override fun controllerBuilder(
        className: String,
        basePath: String,
    ) =
        TypeSpec.interfaceBuilder(className)
            .addAnnotation(SpringAnnotations.CONTROLLER)
            .addAnnotation(SpringAnnotations.VALIDATED)
            .addAnnotation(SpringAnnotations.requestMappingBuilder().addMember("%S", basePath).build())

    override fun buildFunction(
        path: Path,
        op: Operation,
        verb: String,
    ): FunSpec {
        val methodName = methodName(op, verb, path.pathString.isSingleResource())
        val returnType = op.toSuccessResponseType(packages.base)
        val parameters = op.toIncomingParameters(packages.base, path.parameters, emptyList())
        val globalSecurity = api.openApi3.securityRequirements.securitySupport()

        // Main method builder
        val baseFunSpec = FunSpec
            .builder(methodName)
            .addModifiers(KModifier.ABSTRACT)
            .addKdoc(op.toKdoc(parameters))
            .addSpringFunAnnotation(op, verb, path.pathString)
            .addSuspendModifier()

        val explicitAsyncSupport = op.extensions[EXTENSION_ASYNC_SUPPORT] as? Boolean
        val asyncSupport = explicitAsyncSupport ?: options.contains(ControllerCodeGenOptionType.COMPLETION_STAGE)
        val springSseSupport = options.contains(ControllerCodeGenOptionType.SSE_EMITTER)

        val funcSpec = when {
            springSseSupport && op.isSseResponse() -> baseFunSpec.returns(SpringImports.SSE_EMITTER)
            asyncSupport -> baseFunSpec.returns(
                SpringImports.COMPLETION_STAGE.parameterizedBy(
                    SpringImports.RESPONSE_ENTITY.parameterizedBy(returnType)
                )
            )

            else -> baseFunSpec.returns(SpringImports.RESPONSE_ENTITY.parameterizedBy(returnType))
        }

        parameters
            .map {
                when (it) {
                    is MultipartParameter ->
                        toParameterSpecBuilder(it)
                            .addSpringParamAnnotation(it)
                            .maybeAddAnnotation(validationAnnotations.parameterValid())
                            .build()

                    is BodyParameter ->
                        it
                            .toParameterSpecBuilder()
                            .addAnnotation(SpringAnnotations.requestBodyBuilder().build())
                            .maybeAddAnnotation(validationAnnotations.parameterValid())
                            .build()

                    is RequestParameter ->
                        it
                            .toParameterSpecBuilder()
                            .addValidationAnnotations(it)
                            .addSpringParamAnnotation(it)
                            .build()
                }
            }
            .forEach { funcSpec.addParameter(it) }

        // Add authentication
        if (addAuthenticationParameter) {
            val securityOption = op.securitySupport(globalSecurity)

            if (securityOption.allowsAuthenticated) {
                val typeName =
                    SpringImports.AUTHENTICATION
                        .copy(nullable = securityOption == ControllerGeneratorUtils.SecuritySupport.AUTHENTICATION_OPTIONAL)
                funcSpec.addParameter(
                    ParameterSpec
                        .builder("authentication", typeName)
                        .build(),
                )
            }
        }

        return funcSpec.build()
    }

    private val springMultipartFileType = ClassName.bestGuess("org.springframework.web.multipart.MultipartFile")
    private val springMultipartFileTypeList = List::class.asClassName().parameterizedBy(springMultipartFileType)

    private fun toParameterSpecBuilder(parameter: MultipartParameter): ParameterSpec.Builder =
        ParameterSpec.builder(
            name = parameter.name,
            type = when {
                parameter.isBinaryFile && parameter.schema.type == "array" -> springMultipartFileTypeList
                parameter.isBinaryFile -> springMultipartFileType
                else -> parameter.type
            }.copy(nullable = !parameter.isRequired),
        )

    private fun FunSpec.Builder.addSpringFunAnnotation(op: Operation, verb: String, path: String): FunSpec.Builder {
        val produces = op.responses
            .flatMap { it.value.contentMediaTypes.keys }
            .distinct()
            .toTypedArray()

        val consumes = op.requestBody
            .contentMediaTypes.keys
            .toTypedArray()

        val funcAnnotation =
            SpringAnnotations
                .requestMappingBuilder()
                .addMember("value = [%S]", path)
                .addMember(
                    "produces = %L",
                    produces.joinToString(prefix = "[", postfix = "]", separator = ", ", transform = { "\"$it\"" }),
                )
                .addMember("method = [RequestMethod.%L]", verb.toUpperCase())

        if (consumes.isNotEmpty()) {
            funcAnnotation.addMember(
                "consumes = %L",
                consumes.joinToString(prefix = "[", postfix = "]", separator = ", ", transform = { "\"$it\"" }),
            )
        }

        this.addAnnotation(funcAnnotation.build())
        return this
    }

    private fun ParameterSpec.Builder.addSpringParamAnnotation(parameter: RequestParameter): ParameterSpec.Builder =
        when (parameter.parameterLocation) {
            QueryParam -> SpringAnnotations.requestParamBuilder()
            HeaderParam -> SpringAnnotations.requestHeaderBuilder()
            PathParam -> SpringAnnotations.requestPathVariableBuilder()
        }.let {
            it.addMember("value = %S", parameter.oasName)
            it.addMember("required = %L", parameter.isRequired)

            if (parameter.defaultValue != null) {
                it.addMember("defaultValue = %S", parameter.defaultValue)
            }

            if (parameter.typeInfo is KotlinTypeInfo.Date) {
                this.addAnnotation(SpringAnnotations.dateTimeFormat(SpringImports.DateTimeFormat.ISO_DATE))
            } else if (parameter.typeInfo is KotlinTypeInfo.DateTime) {
                this.addAnnotation(SpringAnnotations.dateTimeFormat(SpringImports.DateTimeFormat.ISO_DATE_TIME))
            }

            this.addAnnotation(it.build())
        }

    private fun ParameterSpec.Builder.addSpringParamAnnotation(parameter: MultipartParameter): ParameterSpec.Builder =
        when {
            parameter.isBinaryFile -> SpringAnnotations.requestPartBuilder()
            parameter.contentType == "application/json" -> SpringAnnotations.requestPartBuilder()
            else -> SpringAnnotations.requestParamBuilder()
        }.let {
            it.addMember("value = %S", parameter.oasName)
            it.addMember("required = %L", parameter.isRequired)
            this.addAnnotation(it.build())
        }

    private fun FunSpec.Builder.addSuspendModifier(): FunSpec.Builder {
        if (options.any { it == ControllerCodeGenOptionType.SUSPEND_MODIFIER }) {
            this.addModifiers(KModifier.SUSPEND)
        }
        return this
    }
}

data class SpringControllers(val controllers: Collection<ControllerType>) : KotlinTypes(controllers) {
    override val files: Collection<FileSpec> = super.files.map {
        it.toBuilder()
            .addFileDisclaimer()
            .addImport(SpringImports.Static.REQUEST_METHOD.first, SpringImports.Static.REQUEST_METHOD.second)
            .addImport(
                SpringImports.Static.RESPONSE_STATUS.first,
                SpringImports.Static.RESPONSE_STATUS.second,
            )
            .build()
    }
}
