package com.cjbooms.fabrikt.generators.controller.metadata

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName

object SpringImports {

    private object Packages {
        private const val SPRING_BASE = "org.springframework"

        const val HTTP = "$SPRING_BASE.http"

        const val STEREOTYPE = "$SPRING_BASE.stereotype"

        const val WEB_BIND_ANNOTATION = "$SPRING_BASE.web.bind.annotation"

        const val MVC_METHOD_ANNOTATION = "$SPRING_BASE.web.servlet.mvc.method.annotation"

        const val VALIDATION_ANNOTATION = "$SPRING_BASE.validation.annotation"

        const val DATE_TIME_FORMAT = "org.springframework.format.annotation"

        const val SPRING_AUTHENTICATION = "$SPRING_BASE.security.core"

        const val JAVA_UTIL_CONCURRENT = "java.util.concurrent"
    }

    val CONTROLLER = ClassName(Packages.STEREOTYPE, "Controller")

    val RESPONSE_ENTITY = ClassName(Packages.HTTP, "ResponseEntity")

    val REQUEST_HEADER = ClassName(Packages.WEB_BIND_ANNOTATION, "RequestHeader")

    val REQUEST_BODY = ClassName(Packages.WEB_BIND_ANNOTATION, "RequestBody")

    val REQUEST_PARAM = ClassName(Packages.WEB_BIND_ANNOTATION, "RequestParam")

    val PATH_VARIABLE = ClassName(Packages.WEB_BIND_ANNOTATION, "PathVariable")

    val REQUEST_MAPPING = ClassName(Packages.WEB_BIND_ANNOTATION, "RequestMapping")

    val SSE_EMITTER = ClassName(Packages.MVC_METHOD_ANNOTATION, "SseEmitter")

    val VALIDATED = ClassName(Packages.VALIDATION_ANNOTATION, "Validated")

    val DATE_TIME_FORMAT = ClassName(Packages.DATE_TIME_FORMAT, "DateTimeFormat")

    val AUTHENTICATION = ClassName(Packages.SPRING_AUTHENTICATION, "Authentication")

    val COMPLETION_STAGE = ClassName(Packages.JAVA_UTIL_CONCURRENT, "CompletionStage")

    object DateTimeFormat {
        val ISO_DATE = "${DATE_TIME_FORMAT.simpleName}.ISO.DATE"

        val ISO_DATE_TIME = "${DATE_TIME_FORMAT.simpleName}.ISO.DATE_TIME"
    }

    object Static {
        val RESPONSE_STATUS = Pair(Packages.HTTP, "HttpStatus")

        val REQUEST_METHOD = Pair(Packages.WEB_BIND_ANNOTATION, "RequestMethod")
    }
}

object SpringAnnotations {
    val CONTROLLER: AnnotationSpec =
        AnnotationSpec
            .builder(SpringImports.CONTROLLER)
            .build()

    val VALIDATED: AnnotationSpec =
        AnnotationSpec
            .builder(SpringImports.VALIDATED)
            .build()

    fun dateTimeFormat( iso: String ): AnnotationSpec =
        AnnotationSpec
            .builder(SpringImports.DATE_TIME_FORMAT)
            .addMember( "iso = %L", iso )
            .build()

    fun requestMappingBuilder(): AnnotationSpec.Builder =
        AnnotationSpec
            .builder(SpringImports.REQUEST_MAPPING)

    fun requestHeaderBuilder(): AnnotationSpec.Builder =
        AnnotationSpec
            .builder(SpringImports.REQUEST_HEADER)

    fun requestBodyBuilder(): AnnotationSpec.Builder =
        AnnotationSpec
            .builder(SpringImports.REQUEST_BODY)

    fun requestParamBuilder(): AnnotationSpec.Builder =
        AnnotationSpec
            .builder(SpringImports.REQUEST_PARAM)

    fun requestPathVariableBuilder(): AnnotationSpec.Builder =
        AnnotationSpec
            .builder(SpringImports.PATH_VARIABLE)
}
