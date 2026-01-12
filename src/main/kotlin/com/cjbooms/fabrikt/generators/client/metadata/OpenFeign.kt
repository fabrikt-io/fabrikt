package com.cjbooms.fabrikt.generators.client.metadata

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName

object OpenFeignImports {

    private object Packages {
        const val FEIGN = "feign"
        const val SPRING_STARTER = "org.springframework.cloud.openfeign"
    }

    val REQUEST_LINE = ClassName(Packages.FEIGN, "RequestLine")

    val HEADERS = ClassName(Packages.FEIGN, "Headers")

    val HEADER_MAP = ClassName(Packages.FEIGN, "HeaderMap")

    val QUERY_MAP = ClassName(Packages.FEIGN, "QueryMap")

    val PARAM = ClassName(Packages.FEIGN, "Param")

    val FEIGN_CLIENT = ClassName(Packages.SPRING_STARTER, "FeignClient")

    val REQUEST_PART = ClassName("org.springframework.web.bind.annotation", "RequestPart")
}

object OpenFeignAnnotations {
    val HEADER_MAP: AnnotationSpec =
        AnnotationSpec
            .builder(OpenFeignImports.HEADER_MAP)
            .build()

    val QUERY_MAP: AnnotationSpec =
        AnnotationSpec
            .builder(OpenFeignImports.QUERY_MAP)
            .build()

    fun requestLineBuilder(): AnnotationSpec.Builder =
        AnnotationSpec
            .builder(OpenFeignImports.REQUEST_LINE)

    fun headersBuilder(): AnnotationSpec.Builder =
        AnnotationSpec
            .builder(OpenFeignImports.HEADERS)

    fun paramBuilder(): AnnotationSpec.Builder =
        AnnotationSpec
            .builder(OpenFeignImports.PARAM)

    fun feignClientBuilder(): AnnotationSpec.Builder =
        AnnotationSpec
            .builder(OpenFeignImports.FEIGN_CLIENT)

    fun requestPartBuilder(): AnnotationSpec.Builder =
        AnnotationSpec
            .builder(OpenFeignImports.REQUEST_PART)
}
