package com.cjbooms.fabrikt.generators.client

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.GeneratorUtils.functionName
import com.cjbooms.fabrikt.generators.GeneratorUtils.getPrimaryContentMediaType
import com.cjbooms.fabrikt.generators.GeneratorUtils.toKdoc
import com.cjbooms.fabrikt.generators.TypeFactory
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.ADDITIONAL_HEADERS_PARAMETER_NAME
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.ADDITIONAL_QUERY_PARAMETERS_PARAMETER_NAME
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.addIncomingParameters
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.addSuspendModifier
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.deriveClientParameters
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.getReturnType
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.optionallyParameterizeWithResponseEntity
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.simpleClientName
import com.cjbooms.fabrikt.generators.client.metadata.SpringHttpInterfaceAnnotations
import com.cjbooms.fabrikt.model.ClientType
import com.cjbooms.fabrikt.model.Clients
import com.cjbooms.fabrikt.model.Destinations
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
import com.reprezen.kaizen.oasparser.model3.Operation
import com.reprezen.kaizen.oasparser.model3.Path
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.buildCodeBlock

class SpringHttpInterfaceGenerator(
    private val packages: Packages,
    private val api: SourceApi,
    private val srcPath: java.nio.file.Path = Destinations.MAIN_KT_SOURCE
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
                .addFunctions(funcSpecs)
                .build()

            ClientType(clientType, packages.base)
        }.toSet()

        return Clients(clientTypes)
    }

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
            .addHttpExchangeAnnotation(operation, resource, parameters, verb)
            .addSuspendModifier(options)
            .addIncomingParameters(
                parameters,
                annotateRequestParameterWith = { parameter ->
                    when (parameter.parameterLocation) {
                        is QueryParam -> {
                            SpringHttpInterfaceAnnotations.requestParamBuilder()
                                .addMember("%S", parameter.originalName)
                                .build()
                        }

                        is HeaderParam -> {
                            SpringHttpInterfaceAnnotations.requestHeaderBuilder()
                                .addMember("%S", parameter.originalName)
                                .build()
                        }

                        is PathParam -> {
                            SpringHttpInterfaceAnnotations.pathVariableBuilder()
                                .addMember("%S", parameter.originalName)
                                .build()
                        }
                    }
                },
                annotateBodyParameterWith = { _ ->
                    SpringHttpInterfaceAnnotations.requestBodyBuilder()
                        .build()
                },
                annotateMultipartParameterWith = { parameter ->
                    SpringHttpInterfaceAnnotations.requestPartBuilder()
                        .addMember("value = %S", parameter.oasName)
                        .build()
                }
            )
            .addParameter(
                ParameterSpec.builder(
                    ADDITIONAL_HEADERS_PARAMETER_NAME,
                    TypeFactory.createMapOfStringToNonNullType(Any::class.asTypeName()),
                )
                    .addAnnotation(SpringHttpInterfaceAnnotations.requestHeaderBuilder().build())
                    .defaultValue("emptyMap()")
                    .build(),
            )
            .addParameter(
                ParameterSpec.builder(
                    ADDITIONAL_QUERY_PARAMETERS_PARAMETER_NAME,
                    TypeFactory.createMapOfStringToNonNullType(Any::class.asTypeName()),
                )
                    .addAnnotation(SpringHttpInterfaceAnnotations.requestParamBuilder().build())
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

    private fun FunSpec.Builder.addHttpExchangeAnnotation(
        operation: Operation,
        resource: String,
        parameters: List<IncomingParameter>,
        verb: String,
    ): FunSpec.Builder = apply {
        val annotation = HttpExchangeAnnotationBuilder(operation, resource, parameters, verb).build()
        addAnnotation(annotation)
    }

    private class HttpExchangeAnnotationBuilder(
        private val operation: Operation,
        private val resource: String,
        private val parameters: List<IncomingParameter>,
        private val verb: String,
    ) {
        fun build(): AnnotationSpec {
            val headerParams = parameters.getHeaderParameters()

            return SpringHttpInterfaceAnnotations.httpExchangeBuilder()
                .addUrl()
                .addMember("method=%S", verb.uppercase())
                .addContentType(headerParams)
                .addAccepts(headerParams)
                .addHeaders(headerParams)
                .build()
        }

        private fun AnnotationSpec.Builder.addUrl(): AnnotationSpec.Builder = apply {
            addMember("url=%S", resource)
        }

        private fun AnnotationSpec.Builder.addContentType(
            headerParams: List<RequestParameter>
        ): AnnotationSpec.Builder = apply {
            val contentType = headerParams.filter { header ->
                header.typeInfo is KotlinTypeInfo.Enum && header.typeInfo.entries.size == 1
            }.singleOrNull { header ->
                header.name == ClientGeneratorUtils.CONTENT_TYPE_HEADER_NAME
            }

            if (contentType != null) {
                contentType.typeInfo as KotlinTypeInfo.Enum
                addMember("contentType=%S", contentType.typeInfo.entries.first())
            }
        }

        private fun AnnotationSpec.Builder.addAccepts(
            headerParams: List<RequestParameter>,
        ): AnnotationSpec.Builder = apply {
            val acceptHeaders = headerParams.filter { header ->
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
                    val block = operation.getPrimaryContentMediaType()?.key?.let { mediaType ->
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

        private fun AnnotationSpec.Builder.addHeaders(
            headerParams: List<RequestParameter>,
        ): AnnotationSpec.Builder = apply {
            val headerValues = headerParams.filter { header ->
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
