package com.cjbooms.fabrikt.generators.controller

import com.cjbooms.fabrikt.generators.model.ModelGenerator.Companion.toModelType
import com.cjbooms.fabrikt.model.ControllerType
import com.cjbooms.fabrikt.model.KotlinTypeInfo
import com.cjbooms.fabrikt.util.NormalisedString.camelCase
import com.reprezen.kaizen.oasparser.model3.Operation
import com.reprezen.kaizen.oasparser.model3.Response
import com.reprezen.kaizen.oasparser.model3.SecurityRequirement
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName

object ControllerGeneratorUtils {
    /**
     * This maps the OpenAPI response code with a ClassName object.
     * Pulling out the first response. This assumes first is happy path
     * may need to revisit if we want to have conditional responses
     */
    fun Operation.happyPathResponse(basePackage: String): TypeName {
        // Map of response code to nullable name of schema
        val responseDetails = happyPathResponseObject()
        return responseDetails
            .contentMediaTypes
            .map { it.value?.schema }
            .filterNotNull()
            .firstOrNull()
            ?.let { toModelType(basePackage, KotlinTypeInfo.from(it)) }
            ?: Unit::class.asTypeName()
    }

    fun Operation.isSseResponse(): Boolean {
        val responseDetails = happyPathResponseObject()
        return responseDetails.contentMediaTypes["text/event-stream"]
            ?.let { it.schema.type == "array" && it.schema.format == "event-stream" }
            ?: false
    }

    private fun Operation.happyPathResponseObject(): Response {
        val toResponseMapping: Map<Int, Response> = responses
            .filter { it.key != "default" }
            .map { (code, body) ->
                code.replace('X','0').toInt() to body
            }.toMap()

        // Happy path, just pull out the http code with the lowest value. Later we may have conditional responses
        val code: Int = toResponseMapping.keys.minOrNull()
            ?: throw IllegalStateException("Could not extract the response for $this")

        return toResponseMapping[code]!!
    }

    fun controllerName(resourceName: String) = "$resourceName${ControllerType.SUFFIX}"

    fun methodName(op: Operation, verb: String, isSingleResource: Boolean) =
        op.operationId?.camelCase() ?: httpVerbMethodName(verb, isSingleResource)

    private fun httpVerbMethodName(verb: String, isSingleResource: Boolean) =
        if (isSingleResource) "${verb}ById" else verb

    /**
     * Enum definition for different cases of security checks for a given operation.
     */
    enum class SecuritySupport(val allowsAuthenticated: Boolean, val allowsAnonymous: Boolean) {
        /**
         * When the operation does not support any way of security checks.
         */
        NO_SECURITY(false, false),

        /**
         * When the operation requires security checks
         */
        AUTHENTICATION_REQUIRED(true, false),

        /**
         * When the operation does not allow any way of security checks
         */
        AUTHENTICATION_PROHIBITED(false, true),

        /**
         * When the operation can support security checks.
         */
        AUTHENTICATION_OPTIONAL(true, true),
    }

    /**
     * Computes the [SecuritySupport] of a list of [SecurityRequirement]s.
     */
    fun List<SecurityRequirement>.securitySupport(): SecuritySupport {
        val containsEmptyObject = this.any { it.requirements.isEmpty() }
        val containsNonEmptyObject = this.any { it.requirements.isNotEmpty() }

        return when {
            containsEmptyObject && containsNonEmptyObject -> SecuritySupport.AUTHENTICATION_OPTIONAL
            containsEmptyObject -> SecuritySupport.AUTHENTICATION_PROHIBITED
            containsNonEmptyObject -> SecuritySupport.AUTHENTICATION_REQUIRED
            else -> SecuritySupport.NO_SECURITY
        }
    }

    /**
     * Computes the [SecuritySupport] of a given operation.
     * @param defaultSupport The "API-global" security support to use in case the operation itself does not define any.
     */
    fun Operation.securitySupport(defaultSupport: SecuritySupport? = null): SecuritySupport {
        if (!this.hasSecurityRequirements() && defaultSupport != null) {
            return defaultSupport
        }

        return this.securityRequirements.securitySupport()
    }
}
