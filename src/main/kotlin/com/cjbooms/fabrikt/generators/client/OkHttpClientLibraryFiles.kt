package com.cjbooms.fabrikt.generators.client

import com.cjbooms.fabrikt.configurations.Packages
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asTypeName

object OkHttpClientLibraryFiles {
    private val headers = ClassName("okhttp3", "Headers")
    private val headersBuilder = ClassName("okhttp3", "Headers", "Builder")
    private val httpUrlBuilder = ClassName("okhttp3", "HttpUrl", "Builder")
    private val formBodyBuilder = ClassName("okhttp3", "FormBody", "Builder")
    private val okHttpClient = ClassName("okhttp3", "OkHttpClient")
    private val request = ClassName("okhttp3", "Request")
    private val requestBody = ClassName("okhttp3", "RequestBody")
    private val response = ClassName("okhttp3", "Response")
    private val responseBody = ClassName("okhttp3", "ResponseBody")
    private val objectMapper = ClassName("com.fasterxml.jackson.databind", "ObjectMapper")
    private val typeReference = ClassName("com.fasterxml.jackson.core.type", "TypeReference")
    private val runtimeException = ClassName("kotlin", "RuntimeException")

    private val suppressUnused = AnnotationSpec.builder(Suppress::class).addMember("%S", "unused").build()

    private fun apiResponse(packages: Packages) = ClassName(packages.client, "ApiResponse")
    private fun apiException(packages: Packages) = ClassName(packages.client, "ApiException")
    private fun throwsApiException(packages: Packages) =
        AnnotationSpec.builder(Throws::class).addMember("%T::class", apiException(packages)).build()

    fun apiModels(packages: Packages, nonNullDataPayloads: Boolean): FileSpec {
        val dataType =
            if (nonNullDataPayloads) TypeVariableName("T") else TypeVariableName("T").copy(nullable = true)
        val dataParameter = ParameterSpec.builder("data", dataType)
            .apply { if (!nonNullDataPayloads) defaultValue("null") }
            .build()
        return FileSpec.builder(packages.client, "ApiModels")
            .indent("    ")
            .addType(
                TypeSpec.classBuilder("ApiResponse")
                    .addKdoc(
                        "API 2xx success response returned by API call.\n\n" +
                            "@param <T> The type of data that is deserialized from response body"
                    )
                    .addModifiers(KModifier.DATA)
                    .addTypeVariable(TypeVariableName("T"))
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter("statusCode", Int::class)
                            .addParameter("headers", headers)
                            .addParameter(dataParameter)
                            .build()
                    )
                    .addProperty(PropertySpec.builder("statusCode", Int::class).initializer("statusCode").build())
                    .addProperty(PropertySpec.builder("headers", headers).initializer("headers").build())
                    .addProperty(PropertySpec.builder("data", dataType).initializer("data").build())
                    .build()
            )
            .addType(
                TypeSpec.classBuilder("ApiException")
                    .addKdoc("API non-2xx failure responses returned by API call.")
                    .addModifiers(KModifier.OPEN)
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter("message", String::class)
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder("message", String::class, KModifier.OVERRIDE)
                            .initializer("message")
                            .build()
                    )
                    .superclass(runtimeException)
                    .addSuperclassConstructorParameter("message")
                    .build()
            )
            .addType(
                exceptionType("ApiRedirectException", "API 3xx redirect response returned by API call.", packages)
                    .addModifiers(KModifier.OPEN)
                    .build()
            )
            .addType(
                exceptionType("ApiClientException", "API 4xx failure responses returned by API call.", packages)
                    .addModifiers(KModifier.DATA)
                    .build()
            )
            .addType(
                exceptionType("ApiServerException", "API 5xx failure responses returned by API call.", packages)
                    .addModifiers(KModifier.DATA)
                    .build()
            )
            .build()
    }

    private fun exceptionType(name: String, kdoc: String, packages: Packages): TypeSpec.Builder =
        TypeSpec.classBuilder(name)
            .addKdoc(kdoc)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("statusCode", Int::class)
                    .addParameter("headers", headers)
                    .addParameter("message", String::class)
                    .build()
            )
            .addProperty(PropertySpec.builder("statusCode", Int::class).initializer("statusCode").build())
            .addProperty(PropertySpec.builder("headers", headers).initializer("headers").build())
            .addProperty(
                PropertySpec.builder("message", String::class, KModifier.OVERRIDE)
                    .initializer("message")
                    .build()
            )
            .superclass(apiException(packages))
            .addSuperclassConstructorParameter("message")

    fun httpUtil(packages: Packages, nonNullDataPayloads: Boolean): FileSpec =
        FileSpec.builder(packages.client, "HttpUtil")
            .indent("    ")
            .addFunction(
                FunSpec.builder("queryParam")
                    .addAnnotation(suppressUnused)
                    .addTypeVariable(TypeVariableName("T", Any::class))
                    .receiver(httpUrlBuilder)
                    .addParameter("key", String::class)
                    .addParameter("value", TypeVariableName("T").copy(nullable = true))
                    .returns(httpUrlBuilder)
                    .beginControlFlow("return this.apply")
                    .addStatement("if·(value·!=·null)·this.addQueryParameter(key,·value.toString())")
                    .endControlFlow()
                    .build()
            )
            .addFunction(
                FunSpec.builder("formParam")
                    .addAnnotation(suppressUnused)
                    .addTypeVariable(TypeVariableName("T", Any::class))
                    .receiver(formBodyBuilder)
                    .addParameter("key", String::class)
                    .addParameter("value", TypeVariableName("T").copy(nullable = true))
                    .returns(formBodyBuilder)
                    .beginControlFlow("return this.apply")
                    .addStatement("if·(value·!=·null)·this.add(key,·value.toString())")
                    .endControlFlow()
                    .build()
            )
            .addFunction(
                FunSpec.builder("queryParam")
                    .addAnnotation(suppressUnused)
                    .receiver(httpUrlBuilder)
                    .addParameter("key", String::class)
                    .addParameter(
                        "values",
                        List::class.asTypeName().parameterizedBy(Any::class.asTypeName()).copy(nullable = true)
                    )
                    .addParameter(
                        ParameterSpec.builder("explode", Boolean::class).defaultValue("true").build()
                    )
                    .returns(httpUrlBuilder)
                    .beginControlFlow("return this.apply")
                    .beginControlFlow("if (values != null)")
                    .addStatement("if·(explode)·values.forEach·{·addQueryParameter(key,·it.toString())·}")
                    .addStatement("else·addQueryParameter(key,·values.joinToString(%S))", ",")
                    .endControlFlow()
                    .endControlFlow()
                    .build()
            )
            .addFunction(
                FunSpec.builder("header")
                    .addAnnotation(suppressUnused)
                    .receiver(headersBuilder)
                    .addParameter("key", String::class)
                    .addParameter("value", Any::class.asTypeName().copy(nullable = true))
                    .returns(headersBuilder)
                    .beginControlFlow("return this.apply")
                    .addStatement("if·(value·!=·null)·this.add(key,·value.toString())")
                    .endControlFlow()
                    .build()
            )
            .addFunction(
                FunSpec.builder("execute")
                    .addAnnotation(throwsApiException(packages))
                    .addTypeVariable(TypeVariableName("T"))
                    .receiver(request)
                    .addParameter("client", okHttpClient)
                    .addParameter("objectMapper", objectMapper)
                    .addParameter("typeRef", typeReference.parameterizedBy(TypeVariableName("T")))
                    .returns(apiResponse(packages).parameterizedBy(TypeVariableName("T")))
                    .addCode(
                        if (nonNullDataPayloads) {
                            CodeBlock.builder()
                                .add("return doRequest(client)·{·response·->\n")
                                .indent()
                                .add("response.body?.string().isNotBlankOrNull()?.let·{\n")
                                .indent()
                                .add("objectMapper.readValue(it,·typeRef)\n")
                                .indent()
                                .add("?:·throw·ApiException(\"[\${response.code}]:·Response·body·deserialized·to·null\")\n")
                                .unindent()
                                .unindent()
                                .add("}·?:·throw·ApiException(\"[\${response.code}]:·Response·body·expected·but·not·returned\")\n")
                                .unindent()
                                .add("}\n")
                                .build()
                        } else {
                            CodeBlock.builder()
                                .add("return doRequest(client)·{·responseBody·->\n")
                                .indent()
                                .add("responseBody?.deserialize(objectMapper,·typeRef)\n")
                                .unindent()
                                .add("}\n")
                                .build()
                        }
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("execute")
                    .addAnnotation(throwsApiException(packages))
                    .receiver(request)
                    .addParameter("client", okHttpClient)
                    .returns(apiResponse(packages).parameterizedBy(ByteArray::class.asTypeName()))
                    .addCode(
                        if (nonNullDataPayloads) {
                            CodeBlock.builder()
                                .add("return doRequest(client)·{·response·->\n")
                                .indent()
                                .add("response.body?.deserialize()·?:·ByteArray(0)\n")
                                .unindent()
                                .add("}\n")
                                .build()
                        } else {
                            CodeBlock.builder()
                                .add("return doRequest(client)·{·responseBody·->\n")
                                .indent()
                                .add("responseBody?.deserialize()\n")
                                .unindent()
                                .add("}\n")
                                .build()
                        }
                    )
                    .build()
            )
            .apply {
                if (nonNullDataPayloads) {
                    addFunction(
                        FunSpec.builder("executeWithoutResponseBody")
                            .addAnnotation(throwsApiException(packages))
                            .receiver(request)
                            .addParameter("client", okHttpClient)
                            .returns(apiResponse(packages).parameterizedBy(UNIT))
                            .addStatement("return doRequest(client)·{}")
                            .build()
                    )
                }
            }
            .addFunction(
                FunSpec.builder("doRequest")
                    .addModifiers(KModifier.PRIVATE)
                    .addTypeVariable(TypeVariableName("T"))
                    .receiver(request)
                    .addParameter("client", okHttpClient)
                    .addParameter(
                        "bodyReader",
                        if (nonNullDataPayloads) {
                            LambdaTypeName.get(
                                parameters = arrayOf(response),
                                returnType = TypeVariableName("T")
                            )
                        } else {
                            LambdaTypeName.get(
                                parameters = arrayOf(responseBody.copy(nullable = true)),
                                returnType = TypeVariableName("T").copy(nullable = true)
                            )
                        }
                    )
                    .returns(apiResponse(packages).parameterizedBy(TypeVariableName("T")))
                    .addCode(
                        CodeBlock.builder()
                            .add("return client.newCall(this).execute().use·{·response·->\n")
                            .indent()
                            .beginControlFlow("when")
                            .add("response.isSuccessful·->\n")
                            .indent()
                            .add(
                                if (nonNullDataPayloads) {
                                    "ApiResponse(response.code,·response.headers,·bodyReader(response))\n"
                                } else {
                                    "ApiResponse(response.code,·response.headers,·bodyReader(response.body))\n"
                                }
                            )
                            .unindent()
                            .add("response.isRedirection()·->\n")
                            .indent()
                            .add("throw·ApiRedirectException(response.code,·response.headers,·response.errorMessage())\n")
                            .unindent()
                            .add("response.isBadRequest()·->\n")
                            .indent()
                            .add("throw·ApiClientException(response.code,·response.headers,·response.errorMessage())\n")
                            .unindent()
                            .add("response.isServerError()·->\n")
                            .indent()
                            .add("throw·ApiServerException(response.code,·response.headers,·response.errorMessage())\n")
                            .unindent()
                            .add("else·->·throw·ApiException(\"[\${response.code}]:·\${response.errorMessage()}\")\n")
                            .endControlFlow()
                            .unindent()
                            .add("}\n")
                            .build()
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("pathParam")
                    .addAnnotation(suppressUnused)
                    .receiver(String::class)
                    .addParameter(
                        "params",
                        Pair::class.asTypeName()
                            .parameterizedBy(String::class.asTypeName(), Any::class.asTypeName()),
                        KModifier.VARARG
                    )
                    .returns(String::class)
                    .addCode(
                        CodeBlock.builder()
                            .add("return params.fold(this)·{·acc,·param·->\n")
                            .indent()
                            .add("acc.replace(param.first,·param.second.toString())\n")
                            .unindent()
                            .add("}\n")
                            .build()
                    )
                    .build()
            )
            .apply {
                if (!nonNullDataPayloads) {
                    addFunction(
                        FunSpec.builder("deserialize")
                            .addTypeVariable(TypeVariableName("T"))
                            .receiver(responseBody)
                            .addParameter("objectMapper", objectMapper)
                            .addParameter("typeRef", typeReference.parameterizedBy(TypeVariableName("T")))
                            .returns(TypeVariableName("T").copy(nullable = true))
                            .addStatement(
                                "return·this.string().isNotBlankOrNull()?.let·{·objectMapper.readValue(it,·typeRef)·}"
                            )
                            .build()
                    )
                }
            }
            .addFunction(
                FunSpec.builder("deserialize")
                    .receiver(responseBody)
                    .returns(ByteArray::class.asTypeName().copy(nullable = !nonNullDataPayloads))
                    .addStatement("return this.byteStream().readAllBytes()")
                    .build()
            )
            .addFunction(
                FunSpec.builder("isNotBlankOrNull")
                    .receiver(String::class.asTypeName().copy(nullable = true))
                    .returns(String::class.asTypeName().copy(nullable = true))
                    .addStatement("return if (this.isNullOrBlank()) null else this")
                    .build()
            )
            .addFunction(
                FunSpec.builder("errorMessage")
                    .addModifiers(KModifier.PRIVATE)
                    .receiver(response)
                    .returns(String::class)
                    .addStatement("return this.body?.string() ?: this.message")
                    .build()
            )
            .addFunction(
                FunSpec.builder("isBadRequest")
                    .addModifiers(KModifier.PRIVATE)
                    .receiver(response)
                    .returns(Boolean::class)
                    .addStatement("return this.code in 400..499")
                    .build()
            )
            .addFunction(
                FunSpec.builder("isServerError")
                    .addModifiers(KModifier.PRIVATE)
                    .receiver(response)
                    .returns(Boolean::class)
                    .addStatement("return this.code in 500..599")
                    .build()
            )
            .addFunction(
                FunSpec.builder("isRedirection")
                    .addModifiers(KModifier.PRIVATE)
                    .receiver(response)
                    .returns(Boolean::class)
                    .addStatement("return this.code in 300..399")
                    .build()
            )
            .addType(
                TypeSpec.classBuilder("RequestBodyWithFilename")
                    .addModifiers(KModifier.DATA)
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter("requestBody", requestBody)
                            .addParameter("filename", String::class)
                            .build()
                    )
                    .addProperty(PropertySpec.builder("requestBody", requestBody).initializer("requestBody").build())
                    .addProperty(PropertySpec.builder("filename", String::class).initializer("filename").build())
                    .build()
            )
            .build()

    fun oAuth(packages: Packages): FileSpec {
        val authenticator = ClassName("okhttp3", "Authenticator")
        val interceptor = ClassName("okhttp3", "Interceptor")
        val interceptorChain = ClassName("okhttp3", "Interceptor", "Chain")
        val route = ClassName("okhttp3", "Route")
        val accessTokenType = LambdaTypeName.get(returnType = String::class.asTypeName())
        return FileSpec.builder(packages.client, "OAuth")
            .indent("    ")
            .addType(
                TypeSpec.classBuilder("OAuth2")
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter("accessToken", accessTokenType)
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder("accessToken", accessTokenType).initializer("accessToken").build()
                    )
                    .addSuperinterface(authenticator)
                    .addSuperinterface(interceptor)
                    .addFunction(
                        FunSpec.builder("authenticate")
                            .addModifiers(KModifier.OVERRIDE)
                            .addParameter("route", route.copy(nullable = true))
                            .addParameter("response", response)
                            .returns(request)
                            .addCode(
                                CodeBlock.builder()
                                    .add("return response.request.newBuilder()\n")
                                    .add("            .header(%S,·\"Bearer·\${accessToken().trim()}\")\n", "Authorization")
                                    .add("            .build()\n")
                                    .build()
                            )
                            .build()
                    )
                    .addFunction(
                        FunSpec.builder("intercept")
                            .addModifiers(KModifier.OVERRIDE)
                            .addParameter("chain", interceptorChain)
                            .returns(response)
                            .addCode(
                                CodeBlock.builder()
                                    .add("val request = chain.request().newBuilder()\n")
                                    .indent()
                                    .add(".header(%S,·\"Bearer·\${accessToken().trim()}\")\n", "Authorization")
                                    .add(".build()\n")
                                    .unindent()
                                    .add("return chain.proceed(request)\n")
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    }

    fun httpResilience4jUtil(packages: Packages): FileSpec {
        val circuitBreaker = ClassName("io.github.resilience4j.circuitbreaker", "CircuitBreaker")
        val circuitBreakerRegistry = ClassName("io.github.resilience4j.circuitbreaker", "CircuitBreakerRegistry")
        return FileSpec.builder(packages.client, "HttpResilience4jUtil")
            .indent("    ")
            .addFunction(
                FunSpec.builder("withCircuitBreaker")
                    .addTypeVariable(TypeVariableName("T"))
                    .addParameter("circuitBreakerRegistry", circuitBreakerRegistry)
                    .addParameter("apiClientName", String::class)
                    .addParameter(
                        "apiCall",
                        LambdaTypeName.get(returnType = apiResponse(packages).parameterizedBy(TypeVariableName("T")))
                    )
                    .returns(apiResponse(packages).parameterizedBy(TypeVariableName("T")))
                    .addStatement("val circuitBreaker = circuitBreakerRegistry.circuitBreaker(apiClientName)")
                    .addStatement("return·%T.decorateSupplier(circuitBreaker,·apiCall).get()", circuitBreaker)
                    .build()
            )
            .build()
    }
}
