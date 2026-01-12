package com.cjbooms.fabrikt.generators.client.metadata

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName

object SpringHttpInterfaceImports {
    object Packages {
        const val BIND_ANNOTATION = "org.springframework.web.bind.annotation"
        const val SERVICE_ANNOTATION = "org.springframework.web.service.annotation"
    }

    val REQUEST_PARAM = ClassName(Packages.BIND_ANNOTATION, "RequestParam")
    val REQUEST_HEADER = ClassName(Packages.BIND_ANNOTATION, "RequestHeader")
    val REQUEST_BODY = ClassName(Packages.BIND_ANNOTATION, "RequestBody")
    val PATH_VARIABLE = ClassName(Packages.BIND_ANNOTATION, "PathVariable")
    val REQUEST_PART = ClassName(Packages.BIND_ANNOTATION, "RequestPart")

    val HTTP_EXCHANGE = ClassName(Packages.SERVICE_ANNOTATION, "HttpExchange")
}

object SpringHttpInterfaceAnnotations {
    fun requestParamBuilder(): AnnotationSpec.Builder = AnnotationSpec
        .builder(SpringHttpInterfaceImports.REQUEST_PARAM)

    fun requestHeaderBuilder(): AnnotationSpec.Builder = AnnotationSpec
        .builder(SpringHttpInterfaceImports.REQUEST_HEADER)

    fun requestBodyBuilder(): AnnotationSpec.Builder = AnnotationSpec
        .builder(SpringHttpInterfaceImports.REQUEST_BODY)

    fun pathVariableBuilder(): AnnotationSpec.Builder = AnnotationSpec
        .builder(SpringHttpInterfaceImports.PATH_VARIABLE)

    fun httpExchangeBuilder(): AnnotationSpec.Builder = AnnotationSpec
        .builder(SpringHttpInterfaceImports.HTTP_EXCHANGE)

    fun requestPartBuilder(): AnnotationSpec.Builder = AnnotationSpec
        .builder(SpringHttpInterfaceImports.REQUEST_PART)
}
