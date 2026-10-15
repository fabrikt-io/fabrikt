package com.cjbooms.fabrikt.generators.client

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.ClientFunction
import com.cjbooms.fabrikt.model.OpenApi3Document
import com.cjbooms.fabrikt.model.OpenApiOperation
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.STRING
import java.util.logging.Logger

internal enum class HeaderCredentialKind(
    val suffix: String,
    val provider: String,
) {
    API_KEY("ApiKey", "apiKeyProvider"),
    BEARER("BearerToken", "bearerTokenProvider"),
    OAUTH2("OAuth2Token", "oauth2TokenProvider"),
}

internal data class HeaderCredential(
    val kind: HeaderCredentialKind,
    val scheme: String,
    val header: String,
    val scopes: List<String> = emptyList(),
)

internal data class HeaderSecurityPlan(
    val alternatives: List<List<HeaderCredential>>,
) {
    val kinds: List<HeaderCredentialKind> = HeaderCredentialKind.entries.filter { kind -> alternatives.first().any { it.kind == kind } }
}

internal class ClientHeaderApiKeySecurity(
    private val document: OpenApi3Document,
) {
    fun forOperation(
        operation: OpenApiOperation,
        options: Set<ClientCodeGenOptionType>,
    ): List<HeaderSecurityPlan>? {
        if (ClientCodeGenOptionType.OPENAPI_API_KEY_AUTHENTICATION !in options) return null
        val requirements = if (operation.hasSecurityRequirements()) operation.securityRequirements else document.securityRequirements
        if (requirements.none {
                it.requirements.keys.any { name ->
                    document.securitySchemes[name]?.type.equals("apiKey", true)
                }
            }
        ) {
            return null
        }
        val alternatives =
            requirements.mapNotNull { requirement ->
                if (requirement.requirements.isEmpty()) return@mapNotNull null
                val credentials =
                    requirement.requirements.map { (name, scopes) ->
                        val scheme = document.securitySchemes[name]
                        when {
                            scheme?.type.equals("apiKey", true) &&
                                scheme?.location == "header" &&
                                scheme.name?.matches(Regex("[!#\$%&'*+.^_`|~0-9A-Za-z-]+")) == true ->
                                HeaderCredential(HeaderCredentialKind.API_KEY, name, scheme.name!!)
                            scheme?.type.equals("oauth2", true) &&
                                ClientCodeGenOptionType.OPENAPI_OAUTH2_AUTHENTICATION in options ->
                                HeaderCredential(HeaderCredentialKind.OAUTH2, name, "Authorization", scopes.distinct())
                            scheme?.type.equals("http", true) &&
                                scheme?.scheme.equals("bearer", true) &&
                                ClientCodeGenOptionType.OPENAPI_BEARER_AUTHENTICATION in options ->
                                HeaderCredential(HeaderCredentialKind.BEARER, name, "Authorization")
                            else -> null
                        }
                    }
                if (credentials.any { it == null } ||
                    credentials
                        .filterNotNull()
                        .map { it.header.lowercase() }
                        .distinct()
                        .size != credentials.size
                ) {
                    Logger.getGlobal().warning(
                        "A security alternative for operation '${operation.operationId ?: "<unnamed>"}' " +
                            "requires unsupported or conflicting headers. " +
                            "No helper is generated for that complete alternative; enable the relevant authentication options " +
                            "or supply all credentials through the ordinary client method.",
                    )
                    null
                } else {
                    credentials.filterNotNull()
                }
            }
        return alternatives
            .distinct()
            .groupBy { it.map { credential -> credential.kind }.toSet() }
            .values
            .map(::HeaderSecurityPlan)
    }
}

internal fun List<ClientFunction>.withHeaderSecurityWrappers(plans: List<HeaderSecurityPlan>): List<ClientFunction> {
    val function = first()
    return this +
        plans.map {
            ClientFunction(function.spec.withHeaderSecurityWrapper(it, TokenWrapperTarget.ADDITIONAL_HEADERS), function.isMediaTypeFunction)
        }
}

internal fun FunSpec.withHeaderSecurityWrapper(
    plan: HeaderSecurityPlan,
    target: TokenWrapperTarget,
): FunSpec {
    val used = parameters.map { it.name }.toMutableSet()

    fun unique(base: String): String = generateSequence(base) { "${it}Extra" }.first { used.add(it) }
    val providers = plan.kinds.associateWith { unique(it.provider) }
    val headers = unique("authenticationHeaders")
    val adjusted = unique("authenticatedConfiguration")
    val cache = unique("credentialCache")
    val code = CodeBlock.builder().addStatement("val %N = mutableMapOf<Pair<String, Set<String>>, String?>()", cache)
    code.add("val %N = sequenceOf<() -> Map<String, String>?>(\n", headers).indent()
    plan.alternatives.forEach { alternative ->
        code.add("{\n").indent()
        val values =
            alternative.map { credential ->
                val value = unique("credential")
                val scopes = CodeBlock.builder().add("setOf<String>(")
                credential.scopes.forEachIndexed { index, scope ->
                    if (index > 0) scopes.add(", ")
                    scopes.add("%S", scope)
                }
                scopes.add(")")
                val lookup = CodeBlock.of("%S to %L", credential.scheme, scopes.build())
                code.beginControlFlow("if (!%N.containsKey(%L))", cache, lookup)
                code.addStatement(
                    "%N[%L] = %N(%S%L)?.takeIf { it.isNotBlank() }",
                    cache,
                    lookup,
                    providers.getValue(credential.kind),
                    credential.scheme,
                    if (credential.kind == HeaderCredentialKind.OAUTH2) CodeBlock.of(", %L", scopes.build()) else CodeBlock.of(""),
                )
                code.endControlFlow()
                code.addStatement("val %N = %N[%L]", value, cache, lookup)
                code.addStatement(
                    "require(%N == null || %N.none { it.code < 32 || it.code == 127 }) { %S }",
                    value,
                    value,
                    "Authentication credentials must not contain control characters",
                )
                credential to value
            }
        code.add("if (%L) mapOf(\n", values.joinToString(" && ") { "${it.second} != null" }).indent()
        values.forEach { (credential, value) ->
            if (credential.kind == HeaderCredentialKind.API_KEY) {
                code.add("%S to %N!!,\n", credential.header, value)
            } else {
                code.add("%S to (\"Bearer \" + %N!!),\n", credential.header, value)
            }
        }
        code
            .unindent()
            .add(") else null\n")
            .unindent()
            .add("},\n")
    }
    code.unindent().add(").firstNotNullOfOrNull { it() }\n")
    code.addStatement("checkNotNull(%N) { %S }", headers, "Credentials for a complete security alternative are required for this operation")
    val originalHeaders = if (target == TokenWrapperTarget.ADDITIONAL_HEADERS) "additionalHeaders" else "apiConfiguration.customHeaders"
    val merged =
        CodeBlock.of(
            "%L.filterKeys { existing -> %N.keys.none { it.equals(existing, ignoreCase = true) } } + %N",
            originalHeaders,
            headers,
            headers,
        )
    when (target) {
        TokenWrapperTarget.ADDITIONAL_HEADERS -> code.addStatement("val %N = %L", adjusted, merged)
        TokenWrapperTarget.API_CONFIGURATION -> code.addStatement("val %N = apiConfiguration.copy(customHeaders = %L)", adjusted, merged)
    }
    code.add("return %N(\n", name).indent()
    parameters.forEach { parameter ->
        val replace = parameter.name == if (target == TokenWrapperTarget.ADDITIONAL_HEADERS) "additionalHeaders" else "apiConfiguration"
        code.add("%N = %N,\n", parameter.name, if (replace) adjusted else parameter.name)
    }
    code.unindent().add(")\n")
    val isSuspend = KModifier.SUSPEND in modifiers
    return FunSpec
        .builder(name + "With" + plan.kinds.joinToString("And") { it.suffix })
        .apply { if (isSuspend) addModifiers(KModifier.SUSPEND) }
        .apply {
            providers.forEach { (kind, provider) ->
                val args = if (kind == HeaderCredentialKind.OAUTH2) arrayOf(STRING, SET.parameterizedBy(STRING)) else arrayOf(STRING)
                addParameter(provider, LambdaTypeName.get(parameters = args, returnType = STRING.copy(nullable = true)))
            }
        }.addParameters(parameters.map { ParameterSpec.builder(it.name, it.type).apply { it.defaultValue?.let(::defaultValue) }.build() })
        .returns(returnType)
        .addCode(code.build())
        .build()
}
