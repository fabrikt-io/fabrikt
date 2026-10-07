package com.cjbooms.fabrikt.generators.client

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.GeneratorUtils.addDeprecation
import com.cjbooms.fabrikt.generators.GeneratorUtils.functionName
import com.cjbooms.fabrikt.generators.GeneratorUtils.getPrimaryContentMediaType
import com.cjbooms.fabrikt.generators.GeneratorUtils.toKdoc
import com.cjbooms.fabrikt.generators.TypeFactory
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.ADDITIONAL_HEADERS_PARAMETER_NAME
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.ADDITIONAL_QUERY_PARAMETERS_PARAMETER_NAME
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.ClientFunction
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.addIncomingParameters
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.addSuspendModifier
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.deriveClientParameters
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.getReturnType
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.groupedClientPaths
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.modelType
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.optionallyParameterizeWithResponseEntity
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.responseMediaTypes
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.simpleClientName
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.withoutAcceptHeader
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.withoutCollidingMediaTypeFunctions
import com.cjbooms.fabrikt.generators.client.metadata.SpringHttpInterfaceAnnotations
import com.cjbooms.fabrikt.model.ClientType
import com.cjbooms.fabrikt.model.Clients
import com.cjbooms.fabrikt.model.CookieParam
import com.cjbooms.fabrikt.model.Destinations
import com.cjbooms.fabrikt.model.GeneratedFile
import com.cjbooms.fabrikt.model.HeaderParam
import com.cjbooms.fabrikt.model.IncomingParameter
import com.cjbooms.fabrikt.model.KotlinTypeInfo
import com.cjbooms.fabrikt.model.OpenApiOperation
import com.cjbooms.fabrikt.model.PathParam
import com.cjbooms.fabrikt.model.QueryParam
import com.cjbooms.fabrikt.model.RequestParameter
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.requestOperations
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock

class SpringHttpInterfaceGenerator(
    private val packages: Packages,
    private val api: SourceApi,
    private val srcPath: java.nio.file.Path = Destinations.MAIN_KT_SOURCE,
) : ClientGenerator {
    private val bearerSecurity = ClientBearerSecurity(api.openApi3)

    override fun generate(options: Set<ClientCodeGenOptionType>): Clients {
        val clientTypes =
            api
                .groupedClientPaths(options)
                .map { (resourceName, paths) ->
                    val funcSpecs: List<FunSpec> =
                        paths
                            .flatMap { (resource, path) ->
                                api.requestOperations(path).flatMap { (verb, operation) ->
                                    val parameters = deriveClientParameters(path, operation, packages.base)
                                    val securityPlan =
                                        if (ClientCodeGenOptionType.OPENAPI_BEARER_AUTHENTICATION in options) {
                                            bearerSecurity.forOperation(operation)
                                        } else {
                                            null
                                        }
                                    val baseName = functionName(operation, resource, verb)
                                    val baseFunction =
                                        ClientFunction(
                                            buildFunction(
                                                operation,
                                                resource,
                                                verb,
                                                options,
                                                parameters,
                                                baseName,
                                                operation.getReturnType(packages).optionallyParameterizeWithResponseEntity(options),
                                                null,
                                                null,
                                            ),
                                            isMediaTypeFunction = false,
                                        )
                                    val mediaTypeFunctions =
                                        operation.responseMediaTypes(options).map { r ->
                                            ClientFunction(
                                                buildFunction(
                                                    operation,
                                                    resource,
                                                    verb,
                                                    options,
                                                    parameters.withoutAcceptHeader(),
                                                    r.functionName(baseName),
                                                    r.modelType(packages).optionallyParameterizeWithResponseEntity(options),
                                                    r.mediaType,
                                                    buildCodeBlock {
                                                        add(
                                                            "\nAlways sends Accept: %L; use [%N] to choose another representation.\n",
                                                            r.mediaType,
                                                            baseName,
                                                        )
                                                    },
                                                ),
                                                isMediaTypeFunction = true,
                                            )
                                        }
                                    (
                                        listOf(
                                            listOf(baseFunction),
                                        ) + mediaTypeFunctions.map { listOf(it) }
                                    ).map { it.withBearerTokenWrapper(securityPlan) }
                                }
                            }.withoutCollidingMediaTypeFunctions(simpleClientName(resourceName))

                    val clientType =
                        TypeSpec
                            .interfaceBuilder(simpleClientName(resourceName))
                            .addAnnotation(AnnotationSpec.builder(Suppress::class).addMember("%S", "unused").build())
                            .addFunctions(funcSpecs)
                            .build()

                    ClientType(clientType, packages.base)
                }.toSet()

        return Clients(clientTypes)
    }

    private fun buildFunction(
        operation: OpenApiOperation,
        resource: String,
        verb: String,
        options: Set<ClientCodeGenOptionType>,
        parameters: List<IncomingParameter>,
        functionName: String,
        returnType: TypeName,
        acceptMediaType: String?,
        extraKdoc: CodeBlock?,
    ): FunSpec =
        FunSpec
            .builder(functionName)
            .addDeprecation(operation)
            .addModifiers(KModifier.ABSTRACT)
            .addKdoc(operation.toKdoc(parameters))
            .apply { extraKdoc?.let { addKdoc(it) } }
            .addHttpExchangeAnnotation(operation, resource, parameters, verb, acceptMediaType)
            .addSuspendModifier(options)
            .addIncomingParameters(
                parameters,
                annotateRequestParameterWith = { parameter ->
                    when (parameter.parameterLocation) {
                        is QueryParam -> {
                            SpringHttpInterfaceAnnotations
                                .requestParamBuilder()
                                .addMember("%S", parameter.originalName)
                                .build()
                        }

                        is HeaderParam -> {
                            SpringHttpInterfaceAnnotations
                                .requestHeaderBuilder()
                                .addMember("%S", parameter.originalName)
                                .build()
                        }

                        is PathParam -> {
                            SpringHttpInterfaceAnnotations
                                .pathVariableBuilder()
                                .addMember("%S", parameter.originalName)
                                .build()
                        }

                        is CookieParam -> {
                            SpringHttpInterfaceAnnotations
                                .cookieValueBuilder()
                                .addMember("%S", parameter.originalName)
                                .addMember("required = %L", parameter.isRequired)
                                .build()
                        }
                    }
                },
                annotateBodyParameterWith = { _ ->
                    SpringHttpInterfaceAnnotations
                        .requestBodyBuilder()
                        .build()
                },
            ).addParameter(
                ParameterSpec
                    .builder(
                        ADDITIONAL_HEADERS_PARAMETER_NAME,
                        TypeFactory.createMapOfStringToNonNullType(Any::class.asTypeName()),
                    ).addAnnotation(SpringHttpInterfaceAnnotations.requestHeaderBuilder().build())
                    .defaultValue("emptyMap()")
                    .build(),
            ).addParameter(
                ParameterSpec
                    .builder(
                        ADDITIONAL_QUERY_PARAMETERS_PARAMETER_NAME,
                        TypeFactory.createMapOfStringToNonNullType(Any::class.asTypeName()),
                    ).addAnnotation(SpringHttpInterfaceAnnotations.requestParamBuilder().build())
                    .defaultValue("emptyMap()")
                    .build(),
            ).returns(returnType)
            .build()

    private fun FunSpec.Builder.addHttpExchangeAnnotation(
        operation: OpenApiOperation,
        resource: String,
        parameters: List<IncomingParameter>,
        verb: String,
        acceptMediaType: String?,
    ): FunSpec.Builder =
        apply {
            val annotation = HttpExchangeAnnotationBuilder(operation, resource, parameters, verb, acceptMediaType).build()
            addAnnotation(annotation)
        }

    private class HttpExchangeAnnotationBuilder(
        private val operation: OpenApiOperation,
        private val resource: String,
        private val parameters: List<IncomingParameter>,
        private val verb: String,
        private val acceptMediaType: String?,
    ) {
        fun build(): AnnotationSpec {
            val headerParams = parameters.getHeaderParameters()

            return SpringHttpInterfaceAnnotations
                .httpExchangeBuilder()
                .addUrl()
                .addMember("method=%S", verb.uppercase())
                .addContentType(headerParams)
                .addAccepts(headerParams, acceptMediaType)
                .addHeaders(headerParams)
                .build()
        }

        private fun AnnotationSpec.Builder.addUrl(): AnnotationSpec.Builder =
            apply {
                addMember("url=%S", resource)
            }

        private fun AnnotationSpec.Builder.addContentType(headerParams: List<RequestParameter>): AnnotationSpec.Builder =
            apply {
                val headerContentType =
                    headerParams
                        .filter { header ->
                            header.typeInfo is KotlinTypeInfo.Enum && header.typeInfo.entries.size == 1
                        }.singleOrNull { header ->
                            header.name == ClientGeneratorUtils.CONTENT_TYPE_HEADER_NAME
                        }

                val contentType =
                    headerContentType?.let {
                        it.typeInfo as KotlinTypeInfo.Enum
                        it.typeInfo.entries.first()
                    } ?: operation.requestBody.getPrimaryContentMediaType()?.key

                if (contentType != null) {
                    addMember("contentType=%S", contentType)
                }
            }

        private fun AnnotationSpec.Builder.addAccepts(
            headerParams: List<RequestParameter>,
            acceptMediaType: String?,
        ): AnnotationSpec.Builder =
            apply {
                if (acceptMediaType != null) {
                    addMember("accept=%L", listOf(buildCodeBlock { add("%S", acceptMediaType) }))
                    return@apply
                }
                val acceptHeaders =
                    headerParams
                        .filter { header ->
                            header.typeInfo is KotlinTypeInfo.Enum && header.typeInfo.entries.size == 1
                        }.filter { header ->
                            header.name == ClientGeneratorUtils.ACCEPT_HEADER_NAME
                        }.map { header ->
                            header.typeInfo as KotlinTypeInfo.Enum
                            buildCodeBlock {
                                add("%S", header.typeInfo.entries.first())
                            }
                        }.takeIf { it.isNotEmpty() }
                        ?: run {
                            // Add default accept header
                            val block =
                                operation.getPrimaryContentMediaType()?.key?.let { mediaType ->
                                    buildCodeBlock {
                                        add("%S", mediaType)
                                    }
                                }
                            listOfNotNull(block)
                        }

                if (acceptHeaders.isNotEmpty()) {
                    addMember("accept=%L", acceptHeaders)
                }
            }

        private fun AnnotationSpec.Builder.addHeaders(headerParams: List<RequestParameter>): AnnotationSpec.Builder =
            apply {
                val headerValues =
                    headerParams
                        .filter { header ->
                            header.typeInfo is KotlinTypeInfo.Enum && header.typeInfo.entries.size == 1
                        }.filterNot { header ->
                            header.name == ClientGeneratorUtils.ACCEPT_HEADER_NAME
                        }.map { header ->
                            header.typeInfo as KotlinTypeInfo.Enum
                            buildCodeBlock {
                                add("%S=%S", header.originalName, header.typeInfo.entries.first())
                            }
                        }

                if (headerValues.isNotEmpty()) {
                    addMember("headers=%L", headerValues)
                }
            }

        private fun List<IncomingParameter>.getHeaderParameters(): List<RequestParameter> =
            filterIsInstance<RequestParameter>()
                .filter { it.parameterLocation is HeaderParam }

        private fun List<IncomingParameter>.getPathParameters(): List<RequestParameter> =
            filterIsInstance<RequestParameter>()
                .filter { it.parameterLocation is PathParam }
    }

    override fun generateLibrary(options: Set<ClientCodeGenOptionType>): Collection<GeneratedFile> = setOf()
}
