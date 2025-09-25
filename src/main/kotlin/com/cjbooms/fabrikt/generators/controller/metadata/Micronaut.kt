package com.cjbooms.fabrikt.generators.controller.metadata

import com.cjbooms.fabrikt.util.toUpperCase
import com.squareup.kotlinpoet.ClassName

object MicronautImports {

    private object Packages {
        const val MICRONAUT_BASE = "io.micronaut"

        const val MICRONAUT_HTTP = "$MICRONAUT_BASE.http"

        const val MICRONAUT_HTTP_ANNOTATION = "$MICRONAUT_HTTP.annotation"

        const val MICRONAUT_SECURITY = "$MICRONAUT_BASE.security"

        const val MICRONAUT_SECURITY_ANNOTATION = "$MICRONAUT_SECURITY.annotation"

        const val MICRONAUT_AUTHENTICATION = "$MICRONAUT_SECURITY.authentication"

        const val MICRONAUT_RULE= "$MICRONAUT_SECURITY.rules"
    }

    val CONSUMES = ClassName(Packages.MICRONAUT_HTTP_ANNOTATION, "Consumes")
    val PRODUCES = ClassName(Packages.MICRONAUT_HTTP_ANNOTATION, "Produces")
    val RESPONSE = ClassName(Packages.MICRONAUT_HTTP, "HttpResponse")

    val SECURED = ClassName(Packages.MICRONAUT_SECURITY_ANNOTATION, "Secured")
    val AUTHENTICATION = ClassName(Packages.MICRONAUT_AUTHENTICATION, "Authentication")
    val SECURITY_RULE = Pair(Packages.MICRONAUT_RULE, "SecurityRule")

    const val SECURITY_RULE_IS_AUTHENTICATED = "SecurityRule.IS_AUTHENTICATED"
    const val SECURITY_RULE_IS_ANONYMOUS = "SecurityRule.IS_ANONYMOUS"

    val CONTROLLER = ClassName(Packages.MICRONAUT_HTTP_ANNOTATION, "Controller")
    val BODY = ClassName(Packages.MICRONAUT_HTTP_ANNOTATION, "Body")
    val PART = ClassName(Packages.MICRONAUT_HTTP_ANNOTATION, "Part")
    val HEADER = ClassName(Packages.MICRONAUT_HTTP_ANNOTATION, "Header")
    val QUERY_VALUE = ClassName(Packages.MICRONAUT_HTTP_ANNOTATION, "QueryValue")
    val PATH_VARIABLE = ClassName(Packages.MICRONAUT_HTTP_ANNOTATION, "PathVariable")


    object HttpMethods {
        val GET = ClassName(Packages.MICRONAUT_HTTP_ANNOTATION, "Get")
        val POST = ClassName(Packages.MICRONAUT_HTTP_ANNOTATION, "Post")
        val PUT = ClassName(Packages.MICRONAUT_HTTP_ANNOTATION, "Put")
        val PATCH = ClassName(Packages.MICRONAUT_HTTP_ANNOTATION, "Patch")
        val DELETE = ClassName(Packages.MICRONAUT_HTTP_ANNOTATION, "Delete")
        val OPTIONS = ClassName(Packages.MICRONAUT_HTTP_ANNOTATION, "Options")
        val HEAD = ClassName(Packages.MICRONAUT_HTTP_ANNOTATION, "Head")

        fun byName(name: String) =
            when (name.toUpperCase()) {
                "GET" -> GET
                "POST" -> POST
                "PUT" -> PUT
                "PATCH" -> PATCH
                "DELETE" -> DELETE
                "OPTIONS" -> OPTIONS
                "HEAD" -> HEAD
                else -> throw IllegalArgumentException(name)
            }
    }
}
