package com.cjbooms.fabrikt.generators.controller

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.GeneratorUtils.splitByType
import com.cjbooms.fabrikt.generators.GeneratorUtils.toIncomingParameters
import com.cjbooms.fabrikt.generators.GeneratorUtils.toKCodeName
import com.cjbooms.fabrikt.generators.client.ClientGenerator
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.transformToFileUploadType
import com.cjbooms.fabrikt.generators.controller.ControllerGeneratorUtils.happyPathResponse
import com.cjbooms.fabrikt.model.ClientType
import com.cjbooms.fabrikt.model.Clients
import com.cjbooms.fabrikt.model.Destinations
import com.cjbooms.fabrikt.model.GeneratedFile
import com.cjbooms.fabrikt.model.HandlebarsTemplates
import com.cjbooms.fabrikt.model.IncomingParameter
import com.cjbooms.fabrikt.model.KotlinTypeInfo
import com.cjbooms.fabrikt.model.MultipartParameter
import com.cjbooms.fabrikt.model.RequestParameter
import com.cjbooms.fabrikt.model.SimpleFile
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.KaizenParserExtensions.routeToPaths
import com.github.javaparser.utils.CodeGenerationUtils
import com.reprezen.kaizen.oasparser.model3.Operation
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import java.nio.file.Path

class KtorClientGenerator(
    private val packages: Packages,
    private val api: SourceApi,
    private val srcPath: Path = Destinations.MAIN_KT_SOURCE,
) : ClientGenerator {

    private val networkResultClassName = ClassName(packages.client, "NetworkResult")
    private val networkErrorClassName = ClassName(packages.client, "NetworkError")

    override fun generate(options: Set<ClientCodeGenOptionType>): Clients {
        val resources: List<TypeSpec> = api.openApi3.routeToPaths().flatMap { (resourceName, paths) ->
            val clientClassBuilder = TypeSpec.classBuilder(resourceName + "Client")
                .addProperty(
                    PropertySpec.builder("httpClient", ClassName("io.ktor.client", "HttpClient"))
                        .addModifiers(KModifier.PRIVATE)
                        .initializer("httpClient")
                        .build()
                )
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter("httpClient", ClassName("io.ktor.client", "HttpClient"))
                        .build()
                )

            paths.forEach { path ->
                path.value.operations.map { (verb, operation) ->
                    val params = operation.toIncomingParameters(
                        packages.base, path.value.parameters, emptyList()
                    )

                    val (pathParams, queryParams, headerParams, bodyParams, multipartParams) = params.splitByType()

                    val responseType = operation.happyPathResponse(packages.base)
                    val returnType = networkResultClassName.parameterizedBy(responseType)

                    // build client function with NetworkResult<T> return type
                    val clientFunctionBuilder = FunSpec.builder(clientRequestFunctionName(operation, verb, pathParams))
                        .addModifiers(KModifier.SUSPEND)
                        .returns(returnType)
                        .addCode(
                            CodeBlock.builder()
                                .apply {
                                    // Build the URL
                                    val urlBuilder = buildString {
                                        append(path.value.pathString)
                                        pathParams.forEach { param ->
                                            val placeholder = "{${param.originalName}}"
                                            val index = indexOf(placeholder)
                                            if (index >= 0) {
                                                replace(index, index + placeholder.length, "\${${param.name}}")
                                            }
                                        }
                                    }

                                    if (queryParams.isEmpty()) {
                                        addStatement("val url = %P", urlBuilder)
                                    } else {
                                        add("val url = buildString {\n")
                                        indent()
                                        addStatement("append(%P)", urlBuilder)
                                        addStatement("val params = buildList {")
                                        indent()
                                        queryParams.forEach { param ->
                                            val isArrayType = param.typeInfo is KotlinTypeInfo.Array
                                            if (isArrayType) {
                                                if (param.isRequired) {
                                                    addStatement("%L.forEach { add(\"%L=\${it}\") }", param.name, param.originalName)
                                                } else {
                                                    addStatement("%L?.forEach { add(\"%L=\${it}\") }", param.name, param.originalName)
                                                }
                                            } else {
                                                if (param.isRequired) {
                                                    addStatement("add(\"%L=\${%L}\")", param.originalName, param.name)
                                                } else {
                                                    addStatement("%L?.let { add(\"%L=\${it}\") }", param.name, param.originalName)
                                                }
                                            }
                                        }
                                        unindent()
                                        addStatement("}")
                                        addStatement("if (params.isNotEmpty()) append(\"?\").append(params.joinToString(\"&\"))")
                                        unindent()
                                        addStatement("}")
                                    }
                                }
                                .addStatement("")
                                // Start try block
                                .beginControlFlow("return try")
                                .addStatement(
                                    "val response = httpClient.%M(url) {",
                                    MemberName("io.ktor.client.request", verb, isExtension = true)
                                )
                                .indent()
                                .apply {
                                    addStatement(
                                        "%M(\"Accept\", \"application/json\")",
                                        MemberName("io.ktor.client.request", "header")
                                    )
                                    if (multipartParams.isNotEmpty()) {
                                        addMultipartBody(multipartParams)
                                    } else if (bodyParams.isNotEmpty()) {
                                        addStatement(
                                            "%M(\"Content-Type\", \"application/json\")",
                                            MemberName("io.ktor.client.request", "header")
                                        )
                                        addStatement(
                                            "%M(%L)",
                                            MemberName("io.ktor.client.request", "setBody"),
                                            bodyParams.first().name
                                        )
                                    }
                                    headerParams.forEach {
                                        addStatement(
                                            "%M(%S, %L)",
                                            MemberName("io.ktor.client.request", "header"),
                                            it.originalName,
                                            it.name
                                        )
                                    }
                                }
                                .unindent()
                                .addStatement("}")
                                .addStatement("")
                                .beginControlFlow(
                                    "if (response.status.%M())",
                                    MemberName("io.ktor.http", "isSuccess")
                                )
                                .addStatement(
                                    "%T.Success(response.%M())",
                                    networkResultClassName,
                                    MemberName("io.ktor.client.call", "body"),
                                )
                                .nextControlFlow("else")
                                .addStatement(
                                    "val errorBody = response.%M().ifBlank { null }",
                                    MemberName("io.ktor.client.statement", "bodyAsText")
                                )
                                .addStatement(
                                    "%T.Failure(%T.Http(statusCode = response.status.value, statusDescription = response.status.description, body = errorBody))",
                                    networkResultClassName,
                                    networkErrorClassName
                                )
                                .endControlFlow()
                                // Catch ResponseException
                                .nextControlFlow(
                                    "catch (e: %T)",
                                    ClassName("io.ktor.client.plugins", "ResponseException")
                                )
                                .addStatement("val status = e.response.status")
                                .addStatement(
                                    "val body = runCatching { e.response.%M() }.getOrNull()?.ifBlank { null }",
                                    MemberName("io.ktor.client.statement", "bodyAsText")
                                )
                                .addStatement(
                                    "%T.Failure(%T.Http(status.value, status.description, body))",
                                    networkResultClassName,
                                    networkErrorClassName
                                )
                                // Catch IOException
                                .nextControlFlow(
                                    "catch (e: %T)",
                                    ClassName("java.io", "IOException")
                                )
                                .addStatement(
                                    "%T.Failure(%T.Network(e))",
                                    networkResultClassName,
                                    networkErrorClassName
                                )
                                // Catch ContentConvertException (thrown by Ktor's ContentNegotiation)
                                .nextControlFlow(
                                    "catch (e: %T)",
                                    ClassName("io.ktor.serialization", "ContentConvertException")
                                )
                                .addStatement(
                                    "%T.Failure(%T.Serialization(e))",
                                    networkResultClassName,
                                    networkErrorClassName
                                )
                                // Catch NoTransformationFoundException (wrong content type)
                                .nextControlFlow(
                                    "catch (e: %T)",
                                    ClassName("io.ktor.client.call", "NoTransformationFoundException")
                                )
                                .addStatement(
                                    "%T.Failure(%T.Serialization(e))",
                                    networkResultClassName,
                                    networkErrorClassName
                                )
                                // Catch CancellationException - rethrow
                                .nextControlFlow(
                                    "catch (e: %T)",
                                    ClassName("kotlinx.coroutines", "CancellationException")
                                )
                                .addStatement("throw e")
                                // Catch all other exceptions
                                .nextControlFlow("catch (e: Exception)")
                                .addStatement(
                                    "%T.Failure(%T.Unknown(e))",
                                    networkResultClassName,
                                    networkErrorClassName
                                )
                                .endControlFlow()
                                .build()
                        )
                    if (multipartParams.isNotEmpty()) {
                        multipartParams.forEach { param ->
                            val baseType = if (param.isFile) {
                                transformToFileUploadType(param.type, packages.client)
                            } else {
                                param.type
                            }
                            val paramType = if (param.isRequired) baseType else baseType.copy(nullable = true)
                            clientFunctionBuilder.addParameter(
                                ParameterSpec.builder(param.name, paramType)
                                    .apply { if (!param.isRequired) defaultValue("null") }
                                    .build()
                            )
                        }
                    } else if (bodyParams.isNotEmpty()) {
                        clientFunctionBuilder.addParameter(
                            ParameterSpec.builder(bodyParams.first().name, bodyParams.first().type)
                                .build()
                        )
                    }
                    (pathParams + queryParams + headerParams).forEach { param ->
                        val defaultValue = if (!param.isRequired) "null" else null
                        clientFunctionBuilder.addParameter(
                            ParameterSpec.builder(param.name, param.type.copy(nullable = !param.isRequired))
                                .apply { if (defaultValue != null) defaultValue(defaultValue) }
                                .build()
                        )
                    }

                    clientFunctionBuilder.addKdoc(buildFunKdoc(operation, params))

                    clientClassBuilder.addFunction(clientFunctionBuilder.build())
                }
            }

            listOf(clientClassBuilder.build())
        }

        return Clients(resources.map { ClientType(it, packages.base) }.toSet())
    }

    override fun generateLibrary(options: Set<ClientCodeGenOptionType>): Collection<GeneratedFile> {
        val codeDir = srcPath.resolve(CodeGenerationUtils.packageToPath(packages.base))
        val clientDir = codeDir.resolve("client")

        // Generate KtorApiModels.kt (NetworkResult, NetworkError)
        val apiModelsFile = HandlebarsTemplates.applyTemplate(
            template = HandlebarsTemplates.ktorClientApiModels,
            input = packages,
            path = clientDir,
            fileName = "KtorApiModels.kt"
        )

        // Generate FileUpload support class
        val fileUploadType = TypeSpec.classBuilder("FileUpload")
            .addModifiers(KModifier.DATA)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("content", ByteArray::class)
                    .addParameter(
                        ParameterSpec.builder("filename", String::class.asTypeName().copy(nullable = true))
                            .defaultValue("null")
                            .build()
                    )
                    .build()
            )
            .addProperty(
                PropertySpec.builder("content", ByteArray::class)
                    .initializer("content")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("filename", String::class.asTypeName().copy(nullable = true))
                    .initializer("filename")
                    .build()
            )
            .addKdoc(
                """
                |Represents a file upload with optional filename.
                |
                |@param content The file content as a byte array
                |@param filename Optional filename to use in the Content-Disposition header
                """.trimMargin()
            )
            .build()

        val fileSpec = FileSpec.builder(packages.client, "FileUpload")
            .addType(fileUploadType)
            .build()

        val fileUploadFile = SimpleFile(
            clientDir.resolve("FileUpload.kt"),
            fileSpec.toString()
        )

        return setOf(apiModelsFile, fileUploadFile)
    }

    private fun CodeBlock.Builder.addMultipartBody(multipartParams: List<MultipartParameter>) {
        val multiPartFormDataContent = ClassName("io.ktor.client.request.forms", "MultiPartFormDataContent")
        val formPart = MemberName("io.ktor.client.request.forms", "formData")
        val appendFun = MemberName("io.ktor.client.request.forms", "append")
        val headersOf = MemberName("io.ktor.http", "headersOf")
        val contentDisposition = ClassName("io.ktor.http", "HttpHeaders")

        addStatement(
            "%M(%T(",
            MemberName("io.ktor.client.request", "setBody"),
            multiPartFormDataContent
        )
        addStatement("%M {", formPart)
        indent()

        for (param in multipartParams) {
            if (param.isFile) {
                if (param.schema.type == "array") {
                    // Array of files - use FileUpload.filename if provided, otherwise default to index-based name
                    if (param.isRequired) {
                        addStatement("%N.forEachIndexed { index, fileUpload ->", param.name)
                    } else {
                        addStatement("%N?.forEachIndexed { index, fileUpload ->", param.name)
                    }
                    indent()
                    addStatement(
                        "val filename = fileUpload.filename ?: %S + \"_\" + index",
                        param.oasName
                    )
                    addStatement(
                        "%M(%S, fileUpload.content, %M(%T.ContentDisposition, %P))",
                        appendFun,
                        param.oasName,
                        headersOf,
                        contentDisposition,
                        "filename=\"\$filename\""
                    )
                    unindent()
                    addStatement("}")
                } else {
                    // Single file - use FileUpload.filename if provided, otherwise default to param name
                    val filenameValueVar = "${param.oasName}FilenameValue"
                    if (param.isRequired) {
                        addStatement(
                            "val %L = %N.filename ?: %S",
                            filenameValueVar,
                            param.name,
                            param.oasName
                        )
                        addStatement(
                            "%M(%S, %N.content, %M(%T.ContentDisposition, %P))",
                            appendFun,
                            param.oasName,
                            param.name,
                            headersOf,
                            contentDisposition,
                            "filename=\"\$${filenameValueVar}\""
                        )
                    } else {
                        addStatement("%N?.let { fileUpload ->", param.name)
                        indent()
                        addStatement(
                            "val %L = fileUpload.filename ?: %S",
                            filenameValueVar,
                            param.oasName
                        )
                        addStatement(
                            "%M(%S, fileUpload.content, %M(%T.ContentDisposition, %P))",
                            appendFun,
                            param.oasName,
                            headersOf,
                            contentDisposition,
                            "filename=\"\$${filenameValueVar}\""
                        )
                        unindent()
                        addStatement("}")
                    }
                }
            } else {
                // Non-file part
                if (param.isRequired) {
                    addStatement(
                        "%M(%S, %N.toString())",
                        appendFun,
                        param.oasName,
                        param.name
                    )
                } else {
                    addStatement("%N?.let { %M(%S, it.toString()) }", param.name, appendFun, param.oasName)
                }
            }
        }

        unindent()
        addStatement("}")
        addStatement("))")
    }

    private fun clientRequestFunctionName(op: Operation, verb: String, params: List<RequestParameter>) =
        if (op.operationId != null) {
            op.operationId.replaceFirstChar { it.lowercase() }
        } else {
            buildString {
                append(verb.lowercase())
                append(if (params.isNotEmpty()) "By" + params.joinToString("And") { it -> it.name.replaceFirstChar { it.uppercase() } } else "")
            }
        }

    private fun buildFunKdoc(operation: Operation, parameters: List<IncomingParameter>): CodeBlock {
        val (pathParams, queryParams, headerParams, bodyParams) = parameters.splitByType()
        val kDoc = CodeBlock.builder()

        // add summary and description
        val methodDesc = listOf(operation.summary.orEmpty(), operation.description.orEmpty()).filter { it.isNotEmpty() }
        if (methodDesc.isNotEmpty()) {
            methodDesc.forEach { kDoc.add("%L\n", it) }
            kDoc.add("\n")
        }

        // document parameters
        if (parameters.isNotEmpty()) {
            kDoc.add("Parameters:\n")
            (bodyParams + pathParams + queryParams + headerParams).forEach {
                kDoc.add("\t @param %L %L\n", it.name.toKCodeName(), it.description?.trimIndent().orEmpty()).build()
            }
        }

        // document response
        val happyPathResponse = operation.happyPathResponse(packages.base)
        kDoc.add("\nReturns:\n")
        kDoc.add("\t[NetworkResult.Success] with [%L] if the request was successful.\n", happyPathResponse.toString())
        kDoc.add("\t[NetworkResult.Failure] with a [NetworkError] if the request failed.\n")

        return kDoc.build()
    }
}
