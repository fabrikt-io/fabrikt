package com.cjbooms.fabrikt.generators.client

import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.ClientFunction
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

internal enum class TokenWrapperTarget {
    ADDITIONAL_HEADERS,
    API_CONFIGURATION,
}

internal fun List<ClientFunction>.withBearerTokenWrapper(plan: BearerSecurityPlan?): List<ClientFunction> {
    if (plan == null) return this
    val function = first()
    return this +
        ClientFunction(
            function.spec.withBearerTokenWrapper(plan, TokenWrapperTarget.ADDITIONAL_HEADERS),
            function.isMediaTypeFunction,
        )
}

internal fun FunSpec.withBearerTokenWrapper(
    plan: BearerSecurityPlan,
    target: TokenWrapperTarget,
): FunSpec {
    val names = CodeBlock.builder().add("listOf(")
    plan.schemeNames.forEachIndexed { index, schemeName ->
        if (index > 0) names.add(", ")
        names.add("%S", schemeName)
    }
    names.add(")")

    return withAccessTokenWrapper(
        prefix = "bearer",
        suffix = "WithBearerToken",
        providerType =
            LambdaTypeName.get(
                parameters = arrayOf(String::class.asTypeName()),
                returnType = String::class.asTypeName().copy(nullable = true),
            ),
        tokenExpression = { provider ->
            CodeBlock.of("%L.firstNotNullOfOrNull { scheme -> %N(scheme)?.takeIf { it.isNotBlank() } }", names.build(), provider)
        },
        tokenRequired = plan.tokenRequired,
        missingTokenMessage = "A Bearer token is required for this operation",
        target = target,
    )
}

internal fun FunSpec.withAccessTokenWrapper(
    prefix: String,
    suffix: String,
    providerType: LambdaTypeName,
    tokenExpression: (String) -> CodeBlock,
    tokenRequired: Boolean,
    missingTokenMessage: String,
    target: TokenWrapperTarget,
): FunSpec {
    val parameterNames = parameters.map { it.name }.toSet()
    val providerName = uniqueTokenName("${prefix}TokenProvider", parameterNames)
    val tokenName = uniqueTokenName("${prefix}Token", parameterNames + providerName)
    val adjustedName = uniqueTokenName("${prefix}Headers", parameterNames + providerName + tokenName)
    val isSuspend = KModifier.SUSPEND in modifiers

    return FunSpec
        .builder("$name$suffix")
        .apply { if (isSuspend) addModifiers(KModifier.SUSPEND) }
        .addParameter(
            providerName,
            providerType,
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
                    "val %N = %L",
                    tokenName,
                    tokenExpression(providerName),
                ).apply {
                    if (tokenRequired) {
                        addStatement("checkNotNull(%N) { %S }", tokenName, missingTokenMessage)
                    }
                }.apply {
                    when (target) {
                        TokenWrapperTarget.ADDITIONAL_HEADERS ->
                            addStatement(
                                "val %N = %N?.let { additionalHeaders + (%S to \"Bearer \$it\") } ?: additionalHeaders",
                                adjustedName,
                                tokenName,
                                "Authorization",
                            )
                        TokenWrapperTarget.API_CONFIGURATION ->
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
                                TokenWrapperTarget.ADDITIONAL_HEADERS ->
                                    if (original.name == "additionalHeaders") adjustedName else original.name
                                TokenWrapperTarget.API_CONFIGURATION ->
                                    if (original.name == "apiConfiguration") adjustedName else original.name
                            }
                        add("%N = %N,\n", original.name, value)
                    }
                }.unindent()
                .add(")\n")
                .build(),
        ).build()
}

private fun uniqueTokenName(
    base: String,
    used: Set<String>,
): String = generateSequence(base) { "${it}Extra" }.first { it !in used }
