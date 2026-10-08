package com.cjbooms.fabrikt.generators.client

import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.ClientFunction
import com.cjbooms.fabrikt.model.OpenApi3Document
import com.cjbooms.fabrikt.model.OpenApiOperation
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.STRING
import java.util.logging.Logger

internal data class OAuth2SecurityAlternative(
    val schemeName: String,
    val scopes: List<String>,
)

internal data class OAuth2SecurityPlan(
    val alternatives: List<OAuth2SecurityAlternative>,
    val tokenRequired: Boolean,
)

internal class ClientOAuth2Security(
    private val document: OpenApi3Document,
) {
    fun forOperation(operation: OpenApiOperation): OAuth2SecurityPlan? {
        val requirements =
            if (operation.hasSecurityRequirements()) operation.securityRequirements else document.securityRequirements
        val alternatives = mutableListOf<OAuth2SecurityAlternative>()
        var allowsAnonymous = false
        var hasOtherAlternative = false
        requirements.forEach { requirement ->
            val schemes = requirement.requirements
            when {
                schemes.isEmpty() -> allowsAnonymous = true
                schemes.size == 1 && document.securitySchemes[schemes.keys.single()]?.type.equals("oauth2", ignoreCase = true) ->
                    alternatives.add(OAuth2SecurityAlternative(schemes.keys.single(), schemes.values.single().distinct()))
                else -> hasOtherAlternative = true
            }
        }
        if (hasOtherAlternative &&
            requirements.any { requirement ->
                requirement.requirements.keys.any { document.securitySchemes[it]?.type.equals("oauth2", ignoreCase = true) }
            }
        ) {
            Logger.getGlobal().warning(
                "Some security alternatives for operation '${operation.operationId ?: "<unnamed>"}' cannot be " +
                    "satisfied by OAuth2 alone. Only standalone OAuth2 alternatives will receive generated helpers; " +
                    "configure combined or other schemes through client headers or transport, or define a standalone OAuth2 alternative.",
            )
        }
        if (alternatives.isEmpty()) return null
        return OAuth2SecurityPlan(alternatives.distinct(), tokenRequired = !allowsAnonymous && !hasOtherAlternative)
    }
}

internal fun List<ClientFunction>.withOAuth2TokenWrapper(plan: OAuth2SecurityPlan?): List<ClientFunction> {
    if (plan == null) return this
    val function = first()
    return this +
        ClientFunction(function.spec.withOAuth2TokenWrapper(plan, TokenWrapperTarget.ADDITIONAL_HEADERS), function.isMediaTypeFunction)
}

internal fun FunSpec.withOAuth2TokenWrapper(
    plan: OAuth2SecurityPlan,
    target: TokenWrapperTarget,
): FunSpec {
    val alternatives = CodeBlock.builder().add("listOf(")
    plan.alternatives.forEachIndexed { index, alternative ->
        if (index > 0) alternatives.add(", ")
        alternatives.add(if (alternative.scopes.isEmpty()) "%S to setOf<String>(" else "%S to setOf(", alternative.schemeName)
        alternative.scopes.forEachIndexed { scopeIndex, scope ->
            if (scopeIndex > 0) alternatives.add(", ")
            alternatives.add("%S", scope)
        }
        alternatives.add(")")
    }
    alternatives.add(")")
    return withAccessTokenWrapper(
        prefix = "oauth2",
        suffix = "WithOAuth2Token",
        providerType =
            LambdaTypeName.get(
                parameters = arrayOf(STRING, SET.parameterizedBy(STRING)),
                returnType = STRING.copy(nullable = true),
            ),
        tokenExpression = { provider ->
            CodeBlock.of(
                "%L.firstNotNullOfOrNull { (scheme, scopes) -> %N(scheme, scopes)?.takeIf { it.isNotBlank() } }",
                alternatives.build(),
                provider,
            )
        },
        tokenRequired = plan.tokenRequired,
        missingTokenMessage = "An OAuth2 access token is required for this operation",
        target = target,
    )
}
