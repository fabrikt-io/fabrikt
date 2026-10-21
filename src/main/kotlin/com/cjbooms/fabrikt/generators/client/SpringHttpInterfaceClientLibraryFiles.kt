package com.cjbooms.fabrikt.generators.client

import com.cjbooms.fabrikt.configurations.Packages
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName

object SpringHttpInterfaceClientLibraryFiles {
    private val httpEntity = ClassName("org.springframework.http", "HttpEntity")
    private val httpHeaders = ClassName("org.springframework.http", "HttpHeaders")
    private val mediaType = ClassName("org.springframework.http", "MediaType")

    fun jsonPart(packages: Packages): FileSpec {
        val boundedType = TypeVariableName("T", ANY)
        return FileSpec
            .builder(packages.client, "JsonPart")
            .addType(
                TypeSpec
                    .classBuilder("JsonPart")
                    .addKdoc("A single JSON multipart part, including when its value is a collection.")
                    .addTypeVariable(boundedType)
                    .primaryConstructor(
                        FunSpec
                            .constructorBuilder()
                            .addParameter("value", boundedType)
                            .build(),
                    ).superclass(httpEntity.parameterizedBy(TypeVariableName("T")))
                    .addSuperclassConstructorParameter("value")
                    .addSuperclassConstructorParameter(
                        "%T().apply { contentType = %T.APPLICATION_JSON }",
                        httpHeaders,
                        mediaType,
                    ).build(),
            ).build()
    }
}
