package com.cjbooms.fabrikt.generators.client

import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.GeneratorUtils.functionName
import com.cjbooms.fabrikt.generators.GeneratorUtils.getPrimaryContentMediaType
import com.cjbooms.fabrikt.generators.GeneratorUtils.primaryPropertiesConstructor
import com.cjbooms.fabrikt.generators.GeneratorUtils.toClassName
import com.cjbooms.fabrikt.generators.GeneratorUtils.toKdoc
import com.cjbooms.fabrikt.generators.TypeFactory
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.ADDITIONAL_HEADERS_PARAMETER_NAME
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.ADDITIONAL_QUERY_PARAMETERS_PARAMETER_NAME
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.addIncomingParameters
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.deriveClientParameters
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.getReturnType
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.simpleClientName
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.toClientReturnType
import com.cjbooms.fabrikt.generators.model.JacksonMetadata.TYPE_REFERENCE_IMPORT
import com.cjbooms.fabrikt.model.BodyParameter
import com.cjbooms.fabrikt.model.ClientType
import com.cjbooms.fabrikt.model.Destinations
import com.cjbooms.fabrikt.model.GeneratedFile
import com.cjbooms.fabrikt.model.HandlebarsTemplates
import com.cjbooms.fabrikt.model.HeaderParam
import com.cjbooms.fabrikt.model.IncomingParameter
import com.cjbooms.fabrikt.model.KotlinTypeInfo
import com.cjbooms.fabrikt.model.PathParam
import com.cjbooms.fabrikt.model.QueryParam
import com.cjbooms.fabrikt.model.RequestParameter
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.KaizenParserExtensions.routeToPaths
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.javaparser.utils.CodeGenerationUtils
import com.reprezen.kaizen.oasparser.model3.Operation
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import java.nio.file.Path
import com.cjbooms.fabrikt.util.toUpperCase
import com.squareup.kotlinpoet.TypeName

class OkHttpSimpleClientGenerator(
    private val packages: Packages,
    private val api: SourceApi,
    private val srcPath: Path = Destinations.MAIN_KT_SOURCE
) {
    fun generateDynamicClientCode(): Collection<ClientType> {
        return api.openApi3.routeToPaths().map { (resourceName, paths) ->
            val funcSpecs: List<FunSpec> = paths.flatMap { (resource, path) ->
                path.operations.map { (verb, operation) ->
                    val parameters = deriveClientParameters(path, operation, packages.base)
                    FunSpec
                        .builder(functionName(operation, resource, verb))
                        .addModifiers(KModifier.PUBLIC)
                        .addKdoc(operation.toKdoc(parameters))
                        .addAnnotation(
                            AnnotationSpec.builder(Throws::class)
                                .addMember("%T::class", "ApiException".toClassName(packages.client)).build()
                        )
                        .addIncomingParameters(parameters)
                        .addParameter(
                            ParameterSpec.builder(
                                ADDITIONAL_HEADERS_PARAMETER_NAME,
                                TypeFactory.createMapOfStringToNonNullType(String::class.asTypeName())
                            )
                                .defaultValue("emptyMap()")
                                .build()
                        )
                        .addParameter(
                            ParameterSpec.builder(
                                ADDITIONAL_QUERY_PARAMETERS_PARAMETER_NAME,
                                TypeFactory.createMapOfStringToNonNullType(String::class.asTypeName())
                            )
                                .defaultValue("emptyMap()")
                                .build()
                        )
                        .addCode(
                            SimpleClientOperationStatement(
                                packages,
                                resource,
                                verb,
                                operation,
                                parameters,
                            ).toStatement()
                        )
                        .returns(operation.toClientReturnType(packages))
                        .build()
                }
            }

            val clientType = TypeSpec.classBuilder(simpleClientName(resourceName))
                .primaryPropertiesConstructor(
                    PropertySpec.builder("objectMapper", ObjectMapper::class.asTypeName(), KModifier.PRIVATE).build(),
                    PropertySpec.builder("baseUrl", String::class.asTypeName(), KModifier.PRIVATE).build(),
                    PropertySpec.builder("okHttpClient", "OkHttpClient".toClassName("okhttp3"), KModifier.PRIVATE).build()
                )
                .addAnnotation(AnnotationSpec.builder(Suppress::class).addMember("%S", "unused").build())
                .addFunctions(funcSpecs)
                .build()

            ClientType(clientType, packages.base, setOf(TYPE_REFERENCE_IMPORT))
        }.toSet()
    }

    fun generateLibrary(): Collection<GeneratedFile> {
        val codeDir = srcPath.resolve(CodeGenerationUtils.packageToPath(packages.base))
        val clientDir = codeDir.resolve("client")
        return setOf(
            HandlebarsTemplates.applyTemplate(
                template = HandlebarsTemplates.clientApiModels,
                input = packages,
                path = clientDir,
                fileName = "ApiModels.kt"
            ),
            HandlebarsTemplates.applyTemplate(
                template = HandlebarsTemplates.clientHttpUtils,
                input = packages,
                path = clientDir,
                fileName = "HttpUtil.kt"
            ),
            HandlebarsTemplates.applyTemplate(
                template = HandlebarsTemplates.clientOAuth,
                input = packages,
                path = clientDir,
                fileName = "OAuth.kt"
            )
        )
    }
}

data class SimpleClientOperationStatement(
    private val packages: Packages,
    private val resource: String,
    private val verb: String,
    private val operation: Operation,
    private val parameters: List<IncomingParameter>
) {
    fun toStatement(): CodeBlock =
        CodeBlock.builder()
            .addUrlStatement()
            .addPathParamStatement()
            .addQueryParamStatement()
            .addHeaderParamStatement()
            .addRequestStatement()
            .addRequestExecutionStatement()
            .build()

    private fun CodeBlock.Builder.addUrlStatement(): CodeBlock.Builder {
        this.add("val httpUrl: %T = \"%L\"", "HttpUrl".toClassName("okhttp3"), "\$baseUrl$resource")
        return this
    }

    private fun CodeBlock.Builder.addPathParamStatement(): CodeBlock.Builder {
        parameters
            .filterIsInstance<RequestParameter>()
            .filter { it.parameterLocation == PathParam }
            .forEach {
                this.add("\n.pathParam(%S to %N)", "{${it.originalName}}", it.name)
            }

        this.add("\n.%T()\n.newBuilder()", "toHttpUrl".toClassName("okhttp3.HttpUrl.Companion"))
        return this
    }

    /**
     * Only supports `form` style query params with either explode true or false. See [Open API 3.0
     * serialization](https://swagger.io/docs/specification/serialization) query parameters style values
     */
    private fun CodeBlock.Builder.addQueryParamStatement(): CodeBlock.Builder {
        parameters
            .filterIsInstance<RequestParameter>()
            .filter { it.parameterLocation == QueryParam }
            .forEach {
                when (it.typeInfo) {
                    is KotlinTypeInfo.Array -> this.add(
                        "\n.%T(%S, %N, %L)",
                        "queryParam".toClassName(packages.client),
                        it.originalName,
                        it.name,
                        if (it.explode == null || it.explode == true) "true" else "false"
                    )
                    else -> this.add(
                        "\n.%T(%S, %N)",
                        "queryParam".toClassName(packages.client),
                        it.originalName,
                        it.name
                    )
                }
            }
        this.add("\n.also { builder -> additionalQueryParameters.forEach { builder.queryParam(it.key, it.value) } }")
        return this.add("\n.build()\n")
    }

    private fun CodeBlock.Builder.addHeaderParamStatement(): CodeBlock.Builder {
        this.add("\nval headerBuilder = Headers.Builder()")
        parameters
            .filterIsInstance<RequestParameter>()
            .filter { it.parameterLocation == HeaderParam }
            .forEach {
                this.add(
                    "\n.%T(%S, %L)",
                    "header".toClassName(packages.client),
                    it.originalName,
                    it.name + if (it.typeInfo is KotlinTypeInfo.Enum) "?.value" else ""
                )
            }
        this.add("\nadditionalHeaders.forEach { headerBuilder.header(it.key, it.value) }")

        return this.add("\nval httpHeaders: %T = headerBuilder.build()\n", "Headers".toClassName("okhttp3"))
    }

    private fun CodeBlock.Builder.addRequestStatement(): CodeBlock.Builder {
        this.add("\nval request: %T = %T.Builder()", "Request".toClassName("okhttp3"), "Request".toClassName("okhttp3"))
        this.add("\n.url(httpUrl)\n.headers(httpHeaders)")
        when (val op = verb.toUpperCase()) {
            "PUT" -> this.addRequestSerializerStatement("put")
            "POST" -> this.addRequestSerializerStatement("post")
            "PATCH" -> this.addRequestSerializerStatement("patch")
            "HEAD" -> this.add("\n.head()")
            "GET" -> this.add("\n.get()")
            "DELETE" -> this.add("\n.delete()")
            else -> throw NotImplementedError("API operation $op is not supported")
        }
        return this.add("\n.build()\n")
    }

    private fun CodeBlock.Builder.addRequestExecutionStatement() =
        when (operation.getReturnType()) {
            is KotlinTypeInfo.ByteArray ->
                this.add("\nreturn request.execute(okHttpClient)\n")
            else ->
                this.add("\nreturn request.execute(okHttpClient, objectMapper, jacksonTypeRef())\n")
        }

    private fun CodeBlock.Builder.addRequestSerializerStatement(verb: String) {
        val requestBody = operation.requestBody
        val toRequestBody = "toRequestBody".toClassName("okhttp3.RequestBody.Companion")
        parameters.filterIsInstance<BodyParameter>().firstOrNull()?.let {
            this.add(
                "\n.%N(objectMapper.writeValueAsString(%N).%T(%S.%T()))",
                verb,
                it.name,
                toRequestBody,
                requestBody.getPrimaryContentMediaType()?.key,
                "toMediaType".toClassName("okhttp3.MediaType.Companion")
            )
        } ?: this.add("\n.%N(ByteArray(0).%T())", verb, toRequestBody)
    }
}
