package com.cjbooms.fabrikt.generators.client

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.GeneratorUtils.functionName
import com.cjbooms.fabrikt.generators.GeneratorUtils.getPrimaryContentMediaType
import com.cjbooms.fabrikt.generators.GeneratorUtils.toKdoc
import com.cjbooms.fabrikt.generators.MutableSettings
import com.cjbooms.fabrikt.generators.TypeFactory
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.ADDITIONAL_HEADERS_PARAMETER_NAME
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.ADDITIONAL_QUERY_PARAMETERS_PARAMETER_NAME
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.addIncomingParameters
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.addSuspendModifier
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.deriveClientParameters
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.getReturnType
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.optionallyParameterizeWithResponseEntity
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.simpleClientName
import com.cjbooms.fabrikt.generators.client.metadata.OpenFeignAnnotations
import com.cjbooms.fabrikt.model.ClientType
import com.cjbooms.fabrikt.model.Clients
import com.cjbooms.fabrikt.model.GeneratedFile
import com.cjbooms.fabrikt.model.HeaderParam
import com.cjbooms.fabrikt.model.IncomingParameter
import com.cjbooms.fabrikt.model.KotlinTypeInfo
import com.cjbooms.fabrikt.model.MultipartParameter
import com.cjbooms.fabrikt.model.PathParam
import com.cjbooms.fabrikt.model.QueryParam
import com.cjbooms.fabrikt.model.RequestParameter
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.KaizenParserExtensions.routeToPaths
import com.cjbooms.fabrikt.util.toUpperCase
import com.reprezen.kaizen.oasparser.model3.Operation
import com.reprezen.kaizen.oasparser.model3.Path
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock

class OpenFeignInterfaceGenerator(
    private val packages: Packages,
    private val api: SourceApi,
) : ClientGenerator {
    override fun generate(options: Set<ClientCodeGenOptionType>): Clients {
        val clientTypes = api.openApi3.routeToPaths().map { (resourceName, paths) ->
            val funcSpecs: List<FunSpec> = paths.flatMap { (resource, path) ->
                path.operations.map { (verb, operation) ->
                    buildFunction(path, resource, operation, verb, options)
                }
            }

            val clientType = TypeSpec.interfaceBuilder(simpleClientName(resourceName))
                .addAnnotation(AnnotationSpec.builder(Suppress::class).addMember("%S", "unused").build())
                .apply {
                    if (options.contains(ClientCodeGenOptionType.SPRING_CLOUD_OPENFEIGN_STARTER_ANNOTATION)) {
                        addAnnotation(
                            OpenFeignAnnotations.feignClientBuilder()
                                .addMember(
                                    "name = %S",
                                    MutableSettings.openfeignClientName
                                )
                                .addMember("contextId = %S", resourceName)
                                .build()
                        )
                    }
                }
                .addFunctions(funcSpecs)
                .build()

            ClientType(clientType, packages.base)
        }.toSet()

        return Clients(clientTypes)
    }

    override fun generateLibrary(options: Set<ClientCodeGenOptionType>): Collection<GeneratedFile> = emptyList()

    private fun buildFunction(
        path: Path,
        resource: String,
        operation: Operation,
        verb: String,
        options: Set<ClientCodeGenOptionType>,
    ): FunSpec {
        val parameters = deriveClientParameters(path, operation, packages.base)
        return FunSpec
            .builder(functionName(operation, resource, verb))
            .addModifiers(KModifier.ABSTRACT)
            .addKdoc(operation.toKdoc(parameters))
            .addRequestLineAnnotation(resource, verb, parameters)
            .addHeadersAnnotation(operation, parameters)
            .addSuspendModifier(options)
            .addIncomingParameters(
                parameters,
                annotateRequestParameterWith = { parameter ->
                    OpenFeignAnnotations.paramBuilder()
                        .addMember("%S", parameter.name)
                        .build()
                },
                annotateMultipartParameterWith = { parameter ->
                    OpenFeignAnnotations.requestPartBuilder()
                        .addMember("value = %S", parameter.oasName)
                        .build()
                },
            )
            .addParameter(
                ParameterSpec.builder(
                    ADDITIONAL_HEADERS_PARAMETER_NAME,
                    TypeFactory.createMapOfStringToNonNullType(String::class.asTypeName()),
                )
                    .addAnnotation(OpenFeignAnnotations.HEADER_MAP)
                    .defaultValue("emptyMap()")
                    .build(),
            )
            .addParameter(
                ParameterSpec.builder(
                    ADDITIONAL_QUERY_PARAMETERS_PARAMETER_NAME,
                    TypeFactory.createMapOfStringToNonNullType(String::class.asTypeName()),
                )
                    .addAnnotation(OpenFeignAnnotations.QUERY_MAP)
                    .defaultValue("emptyMap()")
                    .build(),
            )
            .returns(
                operation
                    .getReturnType(packages)
                    .optionallyParameterizeWithResponseEntity(options)
            )
            .build()
    }

    /**
     * Adds OpenFeign's @RequestLine annotation to the function spec
     */
    private fun FunSpec.Builder.addRequestLineAnnotation(
        resource: String,
        verb: String,
        parameters: List<IncomingParameter>,
    ): FunSpec.Builder {
        val annotation = RequestLineBuilder(resource, verb, parameters).build()
        addAnnotation(annotation)
        return this
    }

    private class RequestLineBuilder(
        private val resource: String,
        private val verb: String,
        private val parameters: List<IncomingParameter>,
    ) {
        /**
         * Gives the url path so that it can be used as input for OpenFeign's @RequestLine.
         * For query and path variables, the deduplicated parameter name will be used instead of the original name.
         */
        private fun buildUrlPath(): String {
            val (pathParameters, queryParameters) = parameters.getPathAndQueryParameters()
            val resolvedResource = resourceWithDeduplicatedParamNames(resource, pathParameters)
            return if (queryParameters.isNotEmpty()) {
                "$resolvedResource?${queryParameters.joinToString("&") { "${it.originalName}={${it.name}}" }}"
            } else {
                resolvedResource
            }
        }

        private fun List<IncomingParameter>.getPathAndQueryParameters():
                Pair<List<RequestParameter>, List<RequestParameter>> {
            val queryParameters = mutableListOf<RequestParameter>()
            val pathVariables = mutableListOf<RequestParameter>()
            for (parameter in this) {
                if (parameter is RequestParameter) {
                    when (parameter.parameterLocation) {
                        is QueryParam -> queryParameters.add(parameter)
                        is PathParam -> pathVariables.add(parameter)
                        else -> {
                            // ignore
                        }
                    }
                }
            }
            return Pair(pathVariables, queryParameters)
        }

        private fun resourceWithDeduplicatedParamNames(
            resource: String,
            pathParameters: List<RequestParameter>,
        ): String {
            var resolvedResource = resource
            pathParameters.forEach { parameter ->
                if (parameter.originalName != parameter.name) {
                    resolvedResource = resolvedResource.replace("{${parameter.originalName}}", "{${parameter.name}}")
                }
            }
            return resolvedResource
        }

        fun build(): AnnotationSpec {
            val urlPath = buildUrlPath()
            return OpenFeignAnnotations.requestLineBuilder()
                .addMember("%S", "${verb.toUpperCase()} $urlPath")
                .build()
        }
    }

    /**
     * Adds OpenFeign @Headers annotation to the function spec if any headers are detected.
     * Formatting:
     *  - If only one header is used, it will be formatted like @Headers("Accept: application/json").
     *  - If multiple headers are used, it will be formatted as an array like
     *    @Headers(["Brand: {brand}", "Accept: application/json"]).
     * Defined as constant when:
     * - If the defined header in the OAS spec is an enum with only one value, the header will be set as a constant.
     * - If it is the added accepted header
     * Accept header is added when:
     * - If no Accept header is specified yet, then it will be added based on the primary content media type
     *   as specified in the response.
     */
    private fun FunSpec.Builder.addHeadersAnnotation(
        operation: Operation,
        parameters: List<IncomingParameter>,
    ): FunSpec.Builder {
        HeadersAnnotationBuilder(operation, parameters).build()?.let { annotation ->
            addAnnotation(annotation)
        }

        return this
    }

    private class HeadersAnnotationBuilder(
        private val operation: Operation,
        private val parameters: List<IncomingParameter>,
    ) {
        fun build(): AnnotationSpec? {
            val headersValueParts = mutableListOf<String>()
            var acceptHeaderExists = false
            val headerParameters = parameters.getHeaderParameters()
            for (parameter in headerParameters) {
                headersValueParts.add(buildHeadersAnnotationValue(parameter))
                acceptHeaderExists =
                    acceptHeaderExists || parameter.name == ClientGeneratorUtils.ACCEPT_HEADER_NAME
            }
            // Add default accept header
            if (!acceptHeaderExists) {
                getDefaultAcceptHeaderAnnotationValue()?.let {
                    headersValueParts.add(it)
                }
            }

            return if (headersValueParts.isNotEmpty()) {
                val headersBuilder = OpenFeignAnnotations.headersBuilder()
                headersValueParts.forEach { headersBuilder.addMember("%S", it) }
                return headersBuilder.build()
            } else {
                null
            }
        }

        private fun List<IncomingParameter>.getHeaderParameters(): List<RequestParameter> {
            val headerRequestParameters = mutableListOf<RequestParameter>()
            for (parameter in this) {
                if (parameter is RequestParameter && parameter.parameterLocation is HeaderParam) {
                    headerRequestParameters.add(parameter)
                }
            }
            return headerRequestParameters
        }

        private fun buildHeadersAnnotationValue(parameter: RequestParameter): String {
            val parameterValue =
                if (
                    parameter.typeInfo is KotlinTypeInfo.Enum &&
                    parameter.typeInfo.entries.size == 1
                ) {
                    parameter.typeInfo.entries.first()
                } else {
                    "{${parameter.name}}"
                }
            return buildCodeBlock {
                add("%L", "${parameter.originalName}: $parameterValue")
            }.toString()
        }

        private fun getDefaultAcceptHeaderAnnotationValue(): String? {
            return operation.getPrimaryContentMediaType()?.key?.let { mediaType ->
                buildCodeBlock {
                    add(
                        "%L",
                        "${ClientGeneratorUtils.ACCEPT_HEADER_NAME}: $mediaType",
                    )
                }.toString()
            }
        }
    }
}
