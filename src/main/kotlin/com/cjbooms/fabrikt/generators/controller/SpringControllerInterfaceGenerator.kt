package com.cjbooms.fabrikt.generators.controller

import com.cjbooms.fabrikt.cli.ControllerCodeGenOptionType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.GeneratorUtils.addDeprecation
import com.cjbooms.fabrikt.generators.GeneratorUtils.getBodySuccessResponses
import com.cjbooms.fabrikt.generators.GeneratorUtils.groupingStrategyFrom
import com.cjbooms.fabrikt.generators.GeneratorUtils.toIncomingParameters
import com.cjbooms.fabrikt.generators.GeneratorUtils.toKCodeName
import com.cjbooms.fabrikt.generators.GeneratorUtils.toKdoc
import com.cjbooms.fabrikt.generators.ValidationAnnotations
import com.cjbooms.fabrikt.generators.controller.ControllerGeneratorUtils.isSseResponse
import com.cjbooms.fabrikt.generators.controller.ControllerGeneratorUtils.methodName
import com.cjbooms.fabrikt.generators.controller.ControllerGeneratorUtils.securitySupport
import com.cjbooms.fabrikt.generators.controller.ControllerGeneratorUtils.toSuccessResponseType
import com.cjbooms.fabrikt.generators.controller.metadata.SpringAnnotations
import com.cjbooms.fabrikt.generators.controller.metadata.SpringImports
import com.cjbooms.fabrikt.generators.model.ModelGenerator.Companion.toModelType
import com.cjbooms.fabrikt.model.BodyParameter
import com.cjbooms.fabrikt.model.ControllerLibraryType
import com.cjbooms.fabrikt.model.ControllerType
import com.cjbooms.fabrikt.model.CookieParam
import com.cjbooms.fabrikt.model.HeaderParam
import com.cjbooms.fabrikt.model.KotlinTypeInfo
import com.cjbooms.fabrikt.model.KotlinTypes
import com.cjbooms.fabrikt.model.MultipartParameter
import com.cjbooms.fabrikt.model.OpenApiOperation
import com.cjbooms.fabrikt.model.OpenApiPath
import com.cjbooms.fabrikt.model.OpenApiSchema
import com.cjbooms.fabrikt.model.PathParam
import com.cjbooms.fabrikt.model.QueryParam
import com.cjbooms.fabrikt.model.RequestParameter
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.FileUtils.addFileDisclaimer
import com.cjbooms.fabrikt.util.GroupingStrategy
import com.cjbooms.fabrikt.util.SchemaParserExtensions.groupedPaths
import com.cjbooms.fabrikt.util.SchemaParserExtensions.isSingleResource
import com.cjbooms.fabrikt.util.capitalized
import com.cjbooms.fabrikt.util.toUpperCase
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import java.util.logging.Logger

class SpringControllerInterfaceGenerator(
    private val packages: Packages,
    private val api: SourceApi,
    private val validationAnnotations: ValidationAnnotations,
    private val options: Set<ControllerCodeGenOptionType> = emptySet(),
) : AnnotationBasedControllerInterfaceGenerator(packages, api, validationAnnotations),
    ControllerInterfaceGenerator {
    companion object {
        private const val EXTENSION_ASYNC_SUPPORT = "x-async-support"
        private const val EXTENSION_SPRING_API_VERSION = "x-spring-api-version"
    }

    private val addAuthenticationParameter: Boolean
        get() = options.any { it == ControllerCodeGenOptionType.AUTHENTICATION }
    private val groupingStrategy: GroupingStrategy
        get() = groupingStrategyFrom(options)

    override fun generate(): SpringControllers =
        SpringControllers(
            api.openApi3
                .groupedPaths(groupingStrategy)
                .map { (resourceName, paths) ->
                    buildController(resourceName, paths.values)
                }.toSet(),
        )

    override fun generateLibrary(): Collection<ControllerLibraryType> = emptySet()

    override fun supportsOperation(
        verb: String,
        path: String,
    ): Boolean {
        if (!verb.equals("query", ignoreCase = true)) return true
        Logger.getGlobal().warning(
            "Skipping QUERY $path for Spring controllers because the supported stable Spring versions lack RequestMethod.QUERY. " +
                "Implement this route separately or select the Ktor or Micronaut controller target.",
        )
        return false
    }

    override fun controllerBuilder(
        className: String,
        basePath: String,
    ) = TypeSpec
        .interfaceBuilder(className)
        .addAnnotation(SpringAnnotations.CONTROLLER)
        .addAnnotation(SpringAnnotations.VALIDATED)
        .addAnnotation(SpringAnnotations.requestMappingBuilder().addMember("%S", basePath).build())

    override fun buildFunction(
        path: OpenApiPath,
        op: OpenApiOperation,
        verb: String,
    ): FunSpec =
        buildFunction(
            path,
            op,
            verb,
            methodName = methodName(op, verb, path.pathString.isSingleResource()),
            returnType = op.toSuccessResponseType(packages.base),
        )

    private fun buildFunction(
        path: OpenApiPath,
        op: OpenApiOperation,
        verb: String,
        methodName: String,
        returnType: TypeName,
        producesOverride: List<String>? = null,
        version: String? = null,
    ): FunSpec {
        val parameters = op.toIncomingParameters(packages.base, path.parameters, emptyList(), validationAnnotations)
        val globalSecurity = api.openApi3.securityRequirements.securitySupport()

        // Main method builder
        val baseFunSpec =
            FunSpec
                .builder(methodName)
                .addDeprecation(op)
                .addModifiers(KModifier.ABSTRACT)
                .addKdoc(op.toKdoc(parameters))
                .addSpringFunAnnotation(op, verb, path.pathString, producesOverride, version)
                .addSuspendModifier()

        val explicitAsyncSupport = op.extensions[EXTENSION_ASYNC_SUPPORT] as? Boolean
        val asyncSupport = explicitAsyncSupport ?: options.contains(ControllerCodeGenOptionType.COMPLETION_STAGE)
        val springSseSupport = options.contains(ControllerCodeGenOptionType.SSE_EMITTER)

        val funcSpec =
            when {
                springSseSupport && op.isSseResponse() -> baseFunSpec.returns(SpringImports.SSE_EMITTER)
                asyncSupport ->
                    baseFunSpec.returns(
                        SpringImports.COMPLETION_STAGE.parameterizedBy(
                            SpringImports.RESPONSE_ENTITY.parameterizedBy(returnType),
                        ),
                    )

                else -> baseFunSpec.returns(SpringImports.RESPONSE_ENTITY.parameterizedBy(returnType))
            }

        parameters
            .map {
                when (it) {
                    is MultipartParameter ->
                        toParameterSpecBuilder(it)
                            .addSpringParamAnnotation(it)
                            .maybeAddValidAnnotation(it)
                            .build()

                    is BodyParameter ->
                        it
                            .toParameterSpecBuilder()
                            .addAnnotation(SpringAnnotations.requestBodyBuilder().build())
                            .maybeAddValidAnnotation(it)
                            .build()

                    is RequestParameter ->
                        it
                            .toParameterSpecBuilder()
                            .addValidationAnnotations(it)
                            .addSpringParamAnnotation(it)
                            .build()
                }
            }.forEach { funcSpec.addParameter(it) }

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

    override fun buildFunctions(
        path: OpenApiPath,
        op: OpenApiOperation,
        verb: String,
    ): List<FunSpec> {
        val versioned = op.versionedSuccessMethods()
        if (versioned.isEmpty()) {
            if (ControllerCodeGenOptionType.RESPONSE_MEDIA_TYPE_VERSIONS in options &&
                op.hasAnyVersionedSuccessMediaType()
            ) {
                Logger.getGlobal().warning(
                    "Skipping per-version methods for ${verb.uppercase()} ${path.pathString}: success media types " +
                        "are not exclusively versioned variants of a single media type. Generated the single aggregate method.",
                )
            }
            return listOf(buildFunction(path, op, verb))
        }
        return versioned.map { mediaType ->
            buildFunction(
                path,
                op,
                verb,
                methodName =
                    methodName(op, verb, path.pathString.isSingleResource()) +
                        "Version" + mediaType.version.toKCodeName().capitalized(),
                returnType = mediaType.schema.toResponseType(),
                producesOverride = listOf(mediaType.key),
                version = mediaType.version,
            )
        }
    }

    private fun OpenApiSchema.toResponseType(): TypeName = toModelType(packages.base, KotlinTypeInfo.fromResponse(this), isNullable)

    private fun OpenApiOperation.versionedSuccessMethods(): List<VersionedSuccessMediaType> {
        if (ControllerCodeGenOptionType.RESPONSE_MEDIA_TYPE_VERSIONS !in options) return emptyList()
        val entries = getBodySuccessResponses().flatMap { it.contentMediaTypes.entries }
        val parsed = entries.mapNotNull { (key, mediaType) -> parseVersionedMediaType(key, mediaType.schema) }
        val byVersion = parsed.groupBy { it.version }
        val eligible =
            parsed.size == entries.size &&
                parsed.map { it.baseType }.distinct().size == 1 &&
                byVersion.size >= 2 &&
                // Several success codes may repeat a version; they must agree on the schema to yield one method.
                byVersion.values.all { variants -> variants.map { it.schema }.distinct().size == 1 }
        return if (eligible) byVersion.values.map { it.first() } else emptyList()
    }

    private fun OpenApiOperation.hasAnyVersionedSuccessMediaType(): Boolean =
        getBodySuccessResponses().flatMap { it.contentMediaTypes.keys }.any { it.versionParameter() != null }

    private data class VersionedSuccessMediaType(
        val baseType: String,
        val version: String,
        val key: String,
        val schema: OpenApiSchema,
    )

    private fun parseVersionedMediaType(
        key: String,
        schema: OpenApiSchema,
    ): VersionedSuccessMediaType? =
        key.versionParameter()?.let { VersionedSuccessMediaType(key.substringBefore(';').trim(), it, key, schema) }

    /**
     * The value of the `version` media-type parameter, e.g. `1` for `application/x.example.contributor+json;version=1`.
     */
    private fun String.versionParameter(): String? =
        substringAfter(';', missingDelimiterValue = "")
            .split(';')
            .map { it.split('=', limit = 2).map(String::trim) }
            .firstOrNull { it.size == 2 && it[0] == "version" && it[1].isNotBlank() }
            ?.get(1)

    private val springMultipartFileType = ClassName.bestGuess("org.springframework.web.multipart.MultipartFile")
    private val springMultipartFileTypeList = List::class.asClassName().parameterizedBy(springMultipartFileType)

    private fun toParameterSpecBuilder(parameter: MultipartParameter): ParameterSpec.Builder =
        ParameterSpec.builder(
            name = parameter.name,
            type =
                when {
                    parameter.isBinaryFile && parameter.schema.type == "array" -> springMultipartFileTypeList
                    parameter.isBinaryFile -> springMultipartFileType
                    else -> parameter.type
                }.copy(nullable = !parameter.isRequired),
        )

    private fun FunSpec.Builder.addSpringFunAnnotation(
        op: OpenApiOperation,
        verb: String,
        path: String,
        producesOverride: List<String>? = null,
        version: String? = null,
    ): FunSpec.Builder {
        val produces =
            (
                producesOverride ?: op.responses
                    .flatMap { it.value.contentMediaTypes.keys }
                    .distinct()
            ).toTypedArray()

        val consumes =
            op.requestBody
                .contentMediaTypes.keys
                .toTypedArray()

        val funcAnnotation =
            SpringAnnotations
                .requestMappingBuilder()
                .addMember("value = [%S]", path)
                .addMember(
                    "produces = %L",
                    produces.joinToString(prefix = "[", postfix = "]", separator = ", ", transform = { "\"$it\"" }),
                ).addMember("method = [RequestMethod.%L]", verb.toUpperCase())

        if (consumes.isNotEmpty()) {
            funcAnnotation.addMember(
                "consumes = %L",
                consumes.joinToString(prefix = "[", postfix = "]", separator = ", ", transform = { "\"$it\"" }),
            )
        }

        (version ?: op.springApiVersion(verb, path))?.let { funcAnnotation.addMember("version = %S", it) }

        this.addAnnotation(funcAnnotation.build())
        return this
    }

    private fun OpenApiOperation.springApiVersion(
        verb: String,
        path: String,
    ): String? =
        when (val version = extensions[EXTENSION_SPRING_API_VERSION]) {
            null -> null
            is String -> version.ifBlank { null }
            is Number -> version.toString()
            else -> {
                Logger.getGlobal().warning(
                    "Skipping $EXTENSION_SPRING_API_VERSION on ${verb.uppercase()} $path: only a string or a number " +
                        "maps to @RequestMapping(version = ...). No version attribute is generated for this operation.",
                )
                null
            }
        }

    private fun ParameterSpec.Builder.addSpringParamAnnotation(parameter: RequestParameter): ParameterSpec.Builder =
        when (parameter.parameterLocation) {
            QueryParam -> SpringAnnotations.requestParamBuilder()
            HeaderParam -> SpringAnnotations.requestHeaderBuilder()
            PathParam -> SpringAnnotations.requestPathVariableBuilder()
            CookieParam -> SpringAnnotations.cookieValueBuilder()
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

data class SpringControllers(
    val controllers: Collection<ControllerType>,
) : KotlinTypes(controllers) {
    override val files: Collection<FileSpec> =
        super.files.map {
            it
                .toBuilder()
                .addFileDisclaimer()
                .addImport(SpringImports.Static.REQUEST_METHOD.first, SpringImports.Static.REQUEST_METHOD.second)
                .addImport(
                    SpringImports.Static.RESPONSE_STATUS.first,
                    SpringImports.Static.RESPONSE_STATUS.second,
                ).build()
        }
}
