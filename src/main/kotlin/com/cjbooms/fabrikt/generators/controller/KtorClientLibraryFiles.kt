package com.cjbooms.fabrikt.generators.controller

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.NOTHING
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asTypeName

object KtorClientLibraryFiles {
    private val ioException = ClassName("kotlinx.io", "IOException")
    private val exception = ClassName("kotlin", "Exception")
    private val stringMap =
        Map::class
            .asTypeName()
            .parameterizedBy(String::class.asTypeName(), String::class.asTypeName())

    fun ktorApiModels(clientPackage: String): FileSpec {
        val networkError = ClassName(clientPackage, "NetworkError")
        val networkResult = ClassName(clientPackage, "NetworkResult")
        return FileSpec
            .builder(clientPackage, "KtorApiModels")
            .addType(
                TypeSpec
                    .interfaceBuilder("NetworkError")
                    .addKdoc("Sealed interface representing all possible network errors that can occur during API calls.")
                    .addModifiers(KModifier.SEALED)
                    .addType(
                        TypeSpec
                            .classBuilder("Http")
                            .addKdoc(
                                "HTTP error response (4xx, 5xx status codes).\n" +
                                    "@property statusCode The HTTP status code\n" +
                                    "@property statusDescription The standard HTTP status description (e.g., \"Not Found\" for 404)\n" +
                                    "@property body The response body content, if any",
                            ).addModifiers(KModifier.DATA)
                            .primaryConstructor(
                                FunSpec
                                    .constructorBuilder()
                                    .addParameter("statusCode", Int::class)
                                    .addParameter("statusDescription", String::class)
                                    .addParameter(
                                        ParameterSpec
                                            .builder(
                                                "body",
                                                String::class.asTypeName().copy(nullable = true),
                                            ).defaultValue("null")
                                            .build(),
                                    ).build(),
                            ).addProperty(
                                PropertySpec.builder("statusCode", Int::class).initializer("statusCode").build(),
                            ).addProperty(
                                PropertySpec
                                    .builder("statusDescription", String::class)
                                    .initializer("statusDescription")
                                    .build(),
                            ).addProperty(
                                PropertySpec
                                    .builder("body", String::class.asTypeName().copy(nullable = true))
                                    .initializer("body")
                                    .build(),
                            ).addSuperinterface(networkError)
                            .build(),
                    ).addType(
                        TypeSpec
                            .classBuilder("Network")
                            .addKdoc(
                                "Network connectivity error (connection timeout, DNS failure, etc.).\n" +
                                    "@property cause The underlying IOException, if available",
                            ).addModifiers(KModifier.DATA)
                            .primaryConstructor(
                                FunSpec
                                    .constructorBuilder()
                                    .addParameter(
                                        ParameterSpec
                                            .builder("cause", ioException.copy(nullable = true))
                                            .defaultValue("null")
                                            .build(),
                                    ).build(),
                            ).addProperty(
                                PropertySpec
                                    .builder("cause", ioException.copy(nullable = true))
                                    .initializer("cause")
                                    .build(),
                            ).addSuperinterface(networkError)
                            .build(),
                    ).addType(
                        TypeSpec
                            .classBuilder("Serialization")
                            .addKdoc(
                                "Serialization/deserialization error when parsing the response.\n" +
                                    "@property cause The underlying exception",
                            ).addModifiers(KModifier.DATA)
                            .primaryConstructor(
                                FunSpec
                                    .constructorBuilder()
                                    .addParameter("cause", exception)
                                    .build(),
                            ).addProperty(
                                PropertySpec.builder("cause", exception).initializer("cause").build(),
                            ).addSuperinterface(networkError)
                            .build(),
                    ).addType(
                        TypeSpec
                            .classBuilder("Unknown")
                            .addKdoc(
                                "Unknown error that doesn't fit other categories.\n" +
                                    "@property cause The underlying exception, if available",
                            ).addModifiers(KModifier.DATA)
                            .primaryConstructor(
                                FunSpec
                                    .constructorBuilder()
                                    .addParameter(
                                        ParameterSpec
                                            .builder(
                                                "cause",
                                                Throwable::class.asTypeName().copy(nullable = true),
                                            ).defaultValue("null")
                                            .build(),
                                    ).build(),
                            ).addProperty(
                                PropertySpec
                                    .builder("cause", Throwable::class.asTypeName().copy(nullable = true))
                                    .initializer("cause")
                                    .build(),
                            ).addSuperinterface(networkError)
                            .build(),
                    ).build(),
            ).addType(
                TypeSpec
                    .interfaceBuilder("NetworkResult")
                    .addKdoc(
                        "Sealed interface representing the result of a network operation.\n" +
                            "@param T The type of data returned on success",
                    ).addModifiers(KModifier.SEALED)
                    .addTypeVariable(TypeVariableName("T", variance = KModifier.OUT))
                    .addType(
                        TypeSpec
                            .classBuilder("Success")
                            .addKdoc(
                                "Successful response with data.\n" +
                                    "@property data The deserialized response data",
                            ).addModifiers(KModifier.DATA)
                            .addTypeVariable(TypeVariableName("T", variance = KModifier.OUT))
                            .primaryConstructor(
                                FunSpec
                                    .constructorBuilder()
                                    .addParameter("data", TypeVariableName("T"))
                                    .build(),
                            ).addProperty(
                                PropertySpec.builder("data", TypeVariableName("T")).initializer("data").build(),
                            ).addSuperinterface(networkResult.parameterizedBy(TypeVariableName("T")))
                            .build(),
                    ).addType(
                        TypeSpec
                            .classBuilder("Failure")
                            .addKdoc(
                                "Failure response.\n" +
                                    "@property error The network error that occurred",
                            ).addModifiers(KModifier.DATA)
                            .primaryConstructor(
                                FunSpec
                                    .constructorBuilder()
                                    .addParameter("error", networkError)
                                    .build(),
                            ).addProperty(
                                PropertySpec.builder("error", networkError).initializer("error").build(),
                            ).addSuperinterface(networkResult.parameterizedBy(NOTHING))
                            .build(),
                    ).build(),
            ).build()
    }

    fun ktorApiConfiguration(
        clientPackage: String,
        basePath: String,
    ): FileSpec {
        val apiConfiguration = ClassName(clientPackage, "ApiConfiguration")
        return FileSpec
            .builder(clientPackage, "KtorApiConfiguration")
            .addType(
                TypeSpec
                    .classBuilder("ApiConfiguration")
                    .addKdoc(
                        "Configuration for the API.\n" +
                            "@property basePath The base URL path for the API\n" +
                            "@property customHeaders A map of custom HTTP headers to include in every request",
                    ).primaryConstructor(
                        FunSpec
                            .constructorBuilder()
                            .addParameter(
                                ParameterSpec.builder("basePath", String::class).defaultValue("%S", basePath).build(),
                            ).addParameter(
                                ParameterSpec.builder("customHeaders", stringMap).defaultValue("mapOf()").build(),
                            ).build(),
                    ).addProperty(PropertySpec.builder("basePath", String::class).initializer("basePath").build())
                    .addProperty(PropertySpec.builder("customHeaders", stringMap).initializer("customHeaders").build())
                    .addFunction(
                        FunSpec
                            .builder("copy")
                            .addKdoc(
                                "Creates a copy of this configuration with optional overrides.\n" +
                                    "@param basePath The new base path, defaults to the current one\n" +
                                    "@param customHeaders The new custom headers, defaults to the current ones\n" +
                                    "@return A new ApiConfiguration instance",
                            ).addParameter(
                                ParameterSpec
                                    .builder("basePath", String::class)
                                    .defaultValue("this.basePath")
                                    .build(),
                            ).addParameter(
                                ParameterSpec
                                    .builder("customHeaders", stringMap)
                                    .defaultValue("this.customHeaders")
                                    .build(),
                            ).returns(apiConfiguration)
                            .addStatement("return ApiConfiguration(basePath, customHeaders)")
                            .build(),
                    ).build(),
            ).build()
    }
}
