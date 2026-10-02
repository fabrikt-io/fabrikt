package com.cjbooms.fabrikt.generators.client

import com.cjbooms.fabrikt.model.OpenApi3Document
import com.cjbooms.fabrikt.model.OpenApiOperation
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.asTypeName
import java.util.logging.Logger

internal data class BearerSecurityPlan(
    val schemeNames: List<String>,
    val tokenRequired: Boolean,
)

internal class ClientBearerSecurity(
    private val document: OpenApi3Document,
) {
    fun forOperation(operation: OpenApiOperation): BearerSecurityPlan? {
        val requirements =
            if (operation.hasSecurityRequirements()) operation.securityRequirements else document.securityRequirements
        if (requirements.isEmpty()) return null

        val bearerSchemes = mutableListOf<String>()
        var hasOtherAlternative = false
        var allowsAnonymous = false
        requirements.forEach { requirement ->
            val names = requirement.requirements.keys
            when {
                names.isEmpty() -> allowsAnonymous = true
                names.size == 1 &&
                    document.securitySchemes[names.first()]?.let { scheme ->
                        scheme.type.equals("http", ignoreCase = true) && scheme.scheme.equals("bearer", ignoreCase = true)
                    } == true -> bearerSchemes.add(names.first())
                else -> hasOtherAlternative = true
            }
        }

        if (hasOtherAlternative) {
            logger.warning(
                "Some security alternatives for operation '${operation.operationId ?: "<unnamed>"}' cannot be " +
                    "satisfied by Bearer alone. Only standalone HTTP Bearer alternatives will receive generated helpers; " +
                    "configure other schemes through client headers or transport, or define a standalone Bearer alternative.",
            )
        }
        if (bearerSchemes.isEmpty()) return null
        return BearerSecurityPlan(bearerSchemes.distinct(), tokenRequired = !allowsAnonymous && !hasOtherAlternative)
    }

    companion object {
        private val logger = Logger.getGlobal()
    }
}

internal enum class BearerWrapperTarget {
    ADDITIONAL_HEADERS,
    API_CONFIGURATION,
}

internal fun FunSpec.withBearerTokenWrapper(
    plan: BearerSecurityPlan,
    target: BearerWrapperTarget,
): FunSpec {
    val parameterNames = parameters.map { it.name }.toSet()
    val providerName = uniqueBearerName("bearerTokenProvider", parameterNames)
    val tokenName = uniqueBearerName("bearerToken", parameterNames + providerName)
    val adjustedName = uniqueBearerName("bearerHeaders", parameterNames + providerName + tokenName)
    val isSuspend = KModifier.SUSPEND in modifiers
    val names = CodeBlock.builder().add("listOf(")
    plan.schemeNames.forEachIndexed { index, schemeName ->
        if (index > 0) names.add(", ")
        names.add("%S", schemeName)
    }
    names.add(")")

    return FunSpec
        .builder("${name}WithBearerToken")
        .apply { if (isSuspend) addModifiers(KModifier.SUSPEND) }
        .addParameter(
            providerName,
            LambdaTypeName.get(
                parameters = arrayOf(String::class.asTypeName()),
                returnType = String::class.asTypeName().copy(nullable = true),
            ),
        ).addParameters(
            parameters.map { original ->
                ParameterSpec
                    .builder(original.name, original.type)
                    .apply { original.defaultValue?.let { defaultValue(it) } }
                    .build()
            },
        ).returns(returnType ?: Unit::class.asTypeName())
        .addCode(
            CodeBlock
                .builder()
                .addStatement(
                    "val %N = %L.firstNotNullOfOrNull { scheme -> %N(scheme)?.takeIf { it.isNotBlank() } }",
                    tokenName,
                    names.build(),
                    providerName,
                ).apply {
                    if (plan.tokenRequired) {
                        addStatement("checkNotNull(%N) { %S }", tokenName, "A Bearer token is required for this operation")
                    }
                }.apply {
                    when (target) {
                        BearerWrapperTarget.ADDITIONAL_HEADERS ->
                            addStatement(
                                "val %N = %N?.let { additionalHeaders + (%S to \"Bearer \$it\") } ?: additionalHeaders",
                                adjustedName,
                                tokenName,
                                "Authorization",
                            )
                        BearerWrapperTarget.API_CONFIGURATION ->
                            addStatement(
                                "val %N = %N?.let { apiConfiguration.copy(customHeaders = apiConfiguration.customHeaders + (%S to \"Bearer \$it\")) } ?: apiConfiguration",
                                adjustedName,
                                tokenName,
                                "Authorization",
                            )
                    }
                }.add("return %N(\n", name)
                .indent()
                .apply {
                    parameters.forEach { original ->
                        val value =
                            when (target) {
                                BearerWrapperTarget.ADDITIONAL_HEADERS ->
                                    if (original.name == "additionalHeaders") adjustedName else original.name
                                BearerWrapperTarget.API_CONFIGURATION ->
                                    if (original.name == "apiConfiguration") adjustedName else original.name
                            }
                        add("%N = %N,\n", original.name, value)
                    }
                }.unindent()
                .add(")\n")
                .build(),
        ).build()
}

private fun uniqueBearerName(
    base: String,
    used: Set<String>,
): String = generateSequence(base) { "${it}Extra" }.first { it !in used }
