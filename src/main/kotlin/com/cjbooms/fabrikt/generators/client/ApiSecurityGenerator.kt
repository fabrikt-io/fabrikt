package com.cjbooms.fabrikt.generators.client

import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.model.OpenApi3Document
import com.cjbooms.fabrikt.model.OpenApiOAuth2Flow
import com.cjbooms.fabrikt.model.OpenApiSecurityScheme
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.GeneratedVisibilityModifiers.withVisibility
import com.cjbooms.fabrikt.util.requestOperations
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import java.util.logging.Logger

/**
 * Emits a single target-neutral `ApiSecurity.kt`: the security-scheme dictionary declared by the
 * specification, the effective security requirement of every operation, and a pure resolver that
 * turns caller-supplied credentials into request headers.
 *
 * The generated file carries contract data only. It performs no transport work, acquires no
 * credentials and is never applied automatically by a generated client.
 */
class ApiSecurityGenerator(
    private val packages: Packages,
    private val api: SourceApi,
) {
    private val document: OpenApi3Document = api.openApi3
    private val securitySchemeClassName = ClassName(packages.client, "SecurityScheme")
    private val credentialClassName = ClassName(packages.client, "Credential")
    private val locationClassName = ClassName(packages.client, "Location")
    private val flowClassName = ClassName(packages.client, "Flow")
    private val oauth2FlowClassName = ClassName(packages.client, "OAuth2Flow")
    private val securityRequirementClassName = ClassName(packages.client, "SecurityRequirement")
    private val headerMapType = MAP.parameterizedBy(STRING, STRING)
    private val providerType =
        LambdaTypeName.get(
            parameters =
                listOf(
                    ParameterSpec.builder("scheme", STRING).build(),
                    ParameterSpec.builder("scopes", SET.parameterizedBy(STRING)).build(),
                ),
            returnType = credentialClassName.copy(nullable = true),
        )

    fun generate(): FileSpec {
        val locationType =
            enum(
                "Location",
                listOf("HEADER", "QUERY", "COOKIE"),
                kdoc = "Where an api key travels. HEADER resolves to a request header; QUERY and COOKIE do not.",
            )
        val flowType =
            enum(
                "Flow",
                listOf("CLIENT_CREDENTIALS", "AUTHORIZATION_CODE", "IMPLICIT", "PASSWORD"),
                kdoc = "OAuth2 flow names, preserved from the spec as data.",
            )
        return FileSpec
            .builder(packages.client, "ApiSecurity")
            .addImport("java.util", "Base64")
            .addType(securitySchemeInterface())
            .addType(locationType)
            .addType(flowType)
            .addType(oauth2FlowDataClass())
            .addType(securityRequirementDataClass())
            .addType(credentialInterface())
            .addType(apiSecurityObject())
            .addFunction(credentialHeadersFunction())
            .addFunction(headerValueFunction())
            .addFunction(resolveFunction())
            .addFunction(resolveOrThrowFunction())
            .build()
            .withVisibility()
    }

    private class ConstructorProperty(
        val name: String,
        val type: TypeName,
        val defaultValue: CodeBlock? = null,
    )

    private data class OperationRequirement(
        val operationId: String?,
        val method: String,
        val path: String,
        val requirement: CodeBlock,
    ) {
        val operationKey: String = operationId ?: "$method $path"
        val routeKey: String = "$method $path"
    }

    /**
     * Effective requirements per operation: an operation without its own `security` inherits the
     * document-level requirements, an explicit `security: []` clears them. Keys are the raw
     * `operationId`, so they are invariant under `GROUP_BY_TAG`, response media-type functions and
     * `--operation-id-transform`.
     */
    private fun operationRequirements(): List<OperationRequirement> =
        document.paths.values
            .flatMap { path ->
                api.requestOperations(path).map { (verb, operation) ->
                    val method = verb.uppercase()
                    val pathTemplate = path.pathString
                    val effective =
                        if (operation.hasSecurityRequirements()) {
                            operation.securityRequirements
                        } else {
                            document.securityRequirements
                        }
                    OperationRequirement(
                        operationId = operation.operationId,
                        method = method,
                        path = pathTemplate,
                        requirement =
                            securityRequirementCode(
                                effective.map { requirement -> requirement.requirements.mapValues { (_, scopes) -> scopes.toSet() } },
                            ),
                    )
                }
            }.distinctBy { it.operationKey }
            .sortedBy { it.routeKey }

    private fun securitySchemeInterface(): TypeSpec =
        TypeSpec
            .interfaceBuilder("SecurityScheme")
            .addModifiers(KModifier.SEALED, KModifier.PUBLIC)
            .addKdoc(
                "One entry in the schemes dictionary. Only header api keys and bearer/basic/OpenID Connect " +
                    "tokens resolve to request headers here; the other shapes are contract data.",
            ).addType(
                dataClass(
                    "Http",
                    ConstructorProperty("scheme", STRING),
                    ConstructorProperty("bearerFormat", STRING.copy(nullable = true), CodeBlock.of("null")),
                    parent = securitySchemeClassName,
                    kdoc = "RFC 7235 HTTP scheme. 'bearer' and 'basic' resolve to headers; other values do not.",
                ),
            ).addType(
                dataClass(
                    "ApiKey",
                    ConstructorProperty("location", locationClassName),
                    ConstructorProperty("name", STRING),
                    parent = securitySchemeClassName,
                    kdoc = "Key in a header, query or cookie. Only the header location resolves to a request header.",
                ),
            ).addType(
                dataClass(
                    "OAuth2",
                    ConstructorProperty("flows", MAP.parameterizedBy(flowClassName, oauth2FlowClassName)),
                    parent = securitySchemeClassName,
                    kdoc = "OAuth2 scheme. The flows are metadata; the resolver turns a token into a Bearer header.",
                ),
            ).addType(
                dataClass(
                    "OpenIdConnect",
                    ConstructorProperty("openIdConnectUrl", STRING),
                    parent = securitySchemeClassName,
                    kdoc = "OpenID Connect discovery document. The resolver treats it as a Bearer token source.",
                ),
            ).build()

    private fun credentialInterface(): TypeSpec =
        TypeSpec
            .interfaceBuilder("Credential")
            .addModifiers(KModifier.SEALED, KModifier.PUBLIC)
            .addKdoc(
                "What the caller's provider returns for one scheme. A token becomes a Bearer header, " +
                    "basic credentials the Basic header, and an api key its header name.",
            ).addType(
                dataClass(
                    "Token",
                    ConstructorProperty("token", STRING),
                    parent = credentialClassName,
                    kdoc = "Access token, without the Bearer prefix.",
                ),
            ).addType(
                dataClass(
                    "Basic",
                    ConstructorProperty("username", STRING),
                    ConstructorProperty("password", STRING),
                    parent = credentialClassName,
                    kdoc = "Username and password for HTTP Basic.",
                ),
            ).addType(
                dataClass(
                    "ApiKey",
                    ConstructorProperty("key", STRING),
                    parent = credentialClassName,
                    kdoc = "The key value sent under the scheme's header name.",
                ),
            ).build()

    private fun oauth2FlowDataClass(): TypeSpec =
        dataClass(
            "OAuth2Flow",
            ConstructorProperty("authorizationUrl", STRING.copy(nullable = true)),
            ConstructorProperty("tokenUrl", STRING.copy(nullable = true)),
            ConstructorProperty("refreshUrl", STRING.copy(nullable = true)),
            ConstructorProperty("scopes", MAP.parameterizedBy(STRING, STRING)),
            kdoc = "One OAuth2 flow: its endpoints and scope descriptions. Contract data; never consumed by the resolver.",
        )

    private fun securityRequirementDataClass(): TypeSpec =
        dataClass(
            "SecurityRequirement",
            ConstructorProperty(
                "alternatives",
                LIST.parameterizedBy(MAP.parameterizedBy(STRING, SET.parameterizedBy(STRING))),
            ),
            kdoc =
                "One operation's effective requirement. Each entry in alternatives is an OR option; the " +
                    "scheme-to-scopes map inside an entry is an AND group. An empty list means security: [].",
        )

    private fun dataClass(
        name: String,
        vararg properties: ConstructorProperty,
        parent: ClassName? = null,
        kdoc: String? = null,
    ): TypeSpec {
        val constructor =
            FunSpec
                .constructorBuilder()
                .apply {
                    properties.forEach { property ->
                        addParameter(
                            ParameterSpec
                                .builder(property.name, property.type)
                                .apply { property.defaultValue?.let { defaultValue("%L", it) } }
                                .build(),
                        )
                    }
                }.build()
        return TypeSpec
            .classBuilder(name)
            .addModifiers(KModifier.DATA, KModifier.PUBLIC)
            .apply { kdoc?.let { addKdoc(it) } }
            .primaryConstructor(constructor)
            .addProperties(
                properties.map { property ->
                    PropertySpec
                        .builder(property.name, property.type)
                        .addModifiers(KModifier.PUBLIC)
                        .initializer(property.name)
                        .build()
                },
            ).apply { parent?.let(::addSuperinterface) }
            .build()
    }

    private fun enum(
        name: String,
        constants: List<String>,
        kdoc: String? = null,
    ): TypeSpec =
        TypeSpec
            .enumBuilder(name)
            .addModifiers(KModifier.PUBLIC)
            .apply { kdoc?.let { addKdoc(it) } }
            .apply { constants.forEach { addEnumConstant(it, TypeSpec.anonymousClassBuilder().build()) } }
            .build()

    private fun apiSecurityObject(): TypeSpec =
        TypeSpec
            .objectBuilder("ApiSecurity")
            .addModifiers(KModifier.PUBLIC)
            .addKdoc(
                "The security declaration for this API. Contract data only: nothing here sends a request or " +
                    "attaches credentials. Feed the maps and resolver results into the client yourself.",
            ).addProperty(
                PropertySpec
                    .builder("schemes", MAP.parameterizedBy(STRING, securitySchemeClassName))
                    .addModifiers(KModifier.PUBLIC)
                    .addKdoc(
                        "Security schemes keyed by their component name. Header api keys and bearer/basic/OpenID " +
                            "Connect tokens resolve to headers; query/cookie keys are kept as contract data. Unsupported " +
                            "types such as mutualTLS are omitted with a warning, since they are transport-level, not headers.",
                    ).initializer(schemesCode())
                    .build(),
            ).addProperty(
                PropertySpec
                    .builder("requirements", MAP.parameterizedBy(STRING, securityRequirementClassName))
                    .addModifiers(KModifier.PUBLIC)
                    .addKdoc(
                        "Every operation's effective requirement, keyed by the raw operationId, or by " +
                            "\"VERB /path\" when an operation has none. Keys do not change under naming options.",
                    ).initializer(requirementsCode(byPath = false))
                    .build(),
            ).addProperty(
                PropertySpec
                    .builder("requirementsByPath", MAP.parameterizedBy(STRING, securityRequirementClassName))
                    .addModifiers(KModifier.PUBLIC)
                    .addKdoc(
                        "The same requirements keyed by \"VERB /path template\", for transport interceptors " +
                            "that only know a method and a path.",
                    ).initializer(requirementsCode(byPath = true))
                    .build(),
            ).addFunction(resolveForFunction())
            .build()

    private fun schemesCode(): CodeBlock {
        val builder = CodeBlock.builder().add("mapOf(\n").indent()
        document.securitySchemes.toSortedMap().forEach { (name, scheme) ->
            securitySchemeCode(name, scheme)?.let { builder.add("%S to %L,\n", name, it) }
        }
        return builder.unindent().add(")").build()
    }

    private fun securitySchemeCode(
        name: String,
        scheme: OpenApiSecurityScheme,
    ): CodeBlock? =
        when (scheme.type?.lowercase()) {
            "apikey" -> {
                val location =
                    when (scheme.location?.lowercase()) {
                        "header" -> "HEADER"
                        "query" -> "QUERY"
                        "cookie" -> "COOKIE"
                        else -> null
                    }
                val keyName = scheme.name
                if (location == null || keyName == null) {
                    omittedScheme(name, scheme)
                } else {
                    CodeBlock.of("%T.ApiKey(%T.%L, %S)", securitySchemeClassName, locationClassName, location, keyName)
                }
            }
            "http" -> {
                val httpScheme = scheme.scheme
                when {
                    httpScheme == null -> omittedScheme(name, scheme)
                    scheme.bearerFormat == null -> CodeBlock.of("%T.Http(%S)", securitySchemeClassName, httpScheme)
                    else -> CodeBlock.of("%T.Http(%S, %S)", securitySchemeClassName, httpScheme, scheme.bearerFormat)
                }
            }
            "oauth2" -> {
                if (scheme.oauth2Flows.isEmpty()) {
                    Logger.getGlobal().warning(
                        "OAuth2 scheme '$name' declares no resolvable flows; its URLs and scopes may be declared via \$ref, " +
                            "which is not resolved.",
                    )
                }
                CodeBlock.of("%T.OAuth2(%L)", securitySchemeClassName, oauth2FlowsCode(scheme.oauth2Flows))
            }
            "openidconnect" -> {
                val url = scheme.openIdConnectUrl
                if (url == null) omittedScheme(name, scheme) else CodeBlock.of("%T.OpenIdConnect(%S)", securitySchemeClassName, url)
            }
            else -> omittedScheme(name, scheme)
        }

    /**
     * A scheme without a credential representation that a header resolver can produce is left out of
     * the dictionary, so requirements referencing it can never resolve to a complete alternative.
     */
    private fun omittedScheme(
        name: String,
        scheme: OpenApiSecurityScheme,
    ): CodeBlock? {
        Logger.getGlobal().warning(
            "Security scheme '$name' of type '${scheme.type}' has no header credential representation; " +
                "it is omitted from the generated security declaration.",
        )
        return null
    }

    private fun oauth2FlowsCode(flows: Map<String, OpenApiOAuth2Flow>): CodeBlock {
        if (flows.isEmpty()) return CodeBlock.of("emptyMap()")
        val builder = CodeBlock.builder().add("mapOf(\n").indent()
        OAUTH2_FLOW_NAMES.forEach { (flowKey, constant) ->
            val flow = flows[flowKey] ?: return@forEach
            builder
                .add("%T.%L to %T(\n", flowClassName, constant, oauth2FlowClassName)
                .indent()
                .add("authorizationUrl = %L,\n", flow.authorizationUrl.literal())
                .add("tokenUrl = %L,\n", flow.tokenUrl.literal())
                .add("refreshUrl = %L,\n", flow.refreshUrl.literal())
                .add("scopes = %L,\n", stringMapCode(flow.scopes))
                .unindent()
                .add("),\n")
        }
        return builder.unindent().add(")").build()
    }

    private fun stringMapCode(map: Map<String, String>): CodeBlock {
        if (map.isEmpty()) return CodeBlock.of("emptyMap()")
        val builder = CodeBlock.builder().add("mapOf(\n").indent()
        map.toSortedMap().forEach { (key, value) -> builder.add("%S to %S,\n", key, value) }
        return builder.unindent().add(")").build()
    }

    private fun String?.literal(): CodeBlock = if (this == null) CodeBlock.of("null") else CodeBlock.of("%S", this)

    private fun securityRequirementCode(alternatives: List<Map<String, Set<String>>>): CodeBlock {
        if (alternatives.isEmpty()) return CodeBlock.of("%T(emptyList())", securityRequirementClassName)
        val builder =
            CodeBlock
                .builder()
                .add("%T(\n", securityRequirementClassName)
                .indent()
                .add("listOf(\n")
                .indent()
        alternatives.forEach { alternative ->
            if (alternative.isEmpty()) {
                builder.add("emptyMap(),\n")
            } else {
                builder.add("mapOf(\n").indent()
                alternative.forEach { (scheme, scopes) -> builder.add("%S to %L,\n", scheme, scopeSetCode(scopes)) }
                builder.unindent().add("),\n")
            }
        }
        return builder
            .unindent()
            .add("),\n")
            .unindent()
            .add(")")
            .build()
    }

    private fun scopeSetCode(scopes: Set<String>): CodeBlock {
        if (scopes.isEmpty()) return CodeBlock.of("emptySet<String>()")
        val builder = CodeBlock.builder().add("setOf(")
        scopes.sorted().forEachIndexed { index, scope ->
            if (index > 0) builder.add(", ")
            builder.add("%S", scope)
        }
        return builder.add(")").build()
    }

    private fun requirementsCode(byPath: Boolean): CodeBlock {
        val operations = operationRequirements()
        if (operations.isEmpty()) return CodeBlock.of("emptyMap()")
        val builder = CodeBlock.builder().add("mapOf(\n").indent()
        operations.forEach { operation ->
            builder.add("%S to %L,\n", if (byPath) operation.routeKey else operation.operationKey, operation.requirement)
        }
        return builder.unindent().add(")").build()
    }

    private fun resolveForFunction(): FunSpec =
        FunSpec
            .builder("resolveFor")
            .addModifiers(KModifier.PUBLIC)
            .addKdoc(
                "Resolves the requirement for one concrete request. Exact \"METHOD /path\" match first, then\n" +
                    "trailing-segment alignment of same-verb templates; a leading path parameter never earns\n" +
                    "specificity because it may be the transport's base path. A literal base-path prefix is not\n" +
                    "disambiguated from a matching literal route segment, so strip a known prefix first. Two equally\n" +
                    "specific routes, or a provider that yields nothing, produce an empty map, never a throw.",
            ).addParameter("method", STRING)
            .addParameter("path", STRING)
            .addParameter("provider", providerType)
            .returns(headerMapType)
            .addCode(RESOLVE_FOR_BODY)
            .build()

    private fun resolveFunction(): FunSpec =
        FunSpec
            .builder("resolve")
            .addModifiers(KModifier.PUBLIC)
            .addKdoc(
                "Turns this requirement and caller credentials into request headers. Alternatives are tried in\n" +
                    "order; a null credential, a credential that mismatches its scheme, a blank token, an api-key\n" +
                    "value containing control characters, or two credentials on the same header name\n" +
                    "(case-insensitively) with different values make that alternative incomplete. The first\n" +
                    "complete alternative's headers are returned, otherwise an empty map.",
            ).receiver(securityRequirementClassName)
            .addParameter("schemes", MAP.parameterizedBy(STRING, securitySchemeClassName))
            .addParameter("provider", providerType)
            .returns(headerMapType)
            .addCode(RESOLVE_BODY)
            .build()

    private fun resolveOrThrowFunction(): FunSpec =
        FunSpec
            .builder("resolveOrThrow")
            .addModifiers(KModifier.PUBLIC)
            .addKdoc(
                "Like [resolve], but throws when this requirement has alternatives yet none complete. An\n" +
                    "empty requirement (security: []) still returns an empty map rather than throwing.",
            ).receiver(securityRequirementClassName)
            .addParameter("schemes", MAP.parameterizedBy(STRING, securitySchemeClassName))
            .addParameter("provider", providerType)
            .returns(headerMapType)
            .addCode(
                """
                val headers = resolve(schemes, provider)
                check(headers.isNotEmpty() || alternatives.isEmpty()) {
                  "Credentials for a complete security alternative are required for this operation"
                }
                return headers
                """.trimIndent(),
            ).build()

    private fun credentialHeadersFunction(): FunSpec =
        FunSpec
            .builder("credentialHeaders")
            .addModifiers(KModifier.PRIVATE)
            .addParameter("scheme", securitySchemeClassName.copy(nullable = true))
            .addParameter("credential", credentialClassName.copy(nullable = true))
            .returns(headerMapType.copy(nullable = true))
            .addCode(CREDENTIAL_HEADERS_BODY)
            .build()

    private fun headerValueFunction(): FunSpec =
        FunSpec
            .builder("headerValue")
            .addModifiers(KModifier.PRIVATE)
            .addParameter("value", STRING)
            .returns(STRING.copy(nullable = true))
            .addCode(
                "return value.takeIf { candidate -> candidate.isNotBlank() && " +
                    "candidate.none { character -> character.code < 32 || character.code == 127 } }\n",
            ).build()

    companion object {
        private val OAUTH2_FLOW_NAMES =
            listOf(
                "clientCredentials" to "CLIENT_CREDENTIALS",
                "authorizationCode" to "AUTHORIZATION_CODE",
                "implicit" to "IMPLICIT",
                "password" to "PASSWORD",
            )

        private val RESOLVE_BODY =
            """
            for (alternative in alternatives) {
              if (alternative.isEmpty()) continue
              val headers = linkedMapOf<String, String>()
              var complete = true
              for ((schemeName, scopes) in alternative) {
                val scheme = schemes[schemeName]
                val resolved = if (scheme == null) null else credentialHeaders(scheme, provider(schemeName, scopes))
                if (resolved == null) {
                  complete = false
                  break
                }
                var conflict = false
                for ((name, value) in resolved) {
                  val existing = headers.entries.firstOrNull { it.key.equals(name, ignoreCase = true) }
                  if (existing != null) {
                    if (existing.value != value) {
                      conflict = true
                      break
                    }
                  } else {
                    headers[name] = value
                  }
                }
                if (conflict) {
                  complete = false
                  break
                }
              }
              if (complete) return headers
            }
            return emptyMap()
            """.trimIndent()

        private val RESOLVE_FOR_BODY =
            """
            val methodKey = method.uppercase()
            requirementsByPath[methodKey + " " + path]?.let { return it.resolve(schemes, provider) }
            val concrete = path.trim('/').let { if (it.isEmpty()) emptyList() else it.split('/') }
            var best: SecurityRequirement? = null
            var bestSegments = -1
            var bestEffectiveLength = -1
            var bestWildcards = Int.MAX_VALUE
            var ambiguous = false
            for ((templateKey, requirement) in requirementsByPath) {
              val separator = templateKey.indexOf(' ')
              if (separator <= 0 || templateKey.substring(0, separator) != methodKey) continue
              val template = templateKey.substring(separator + 1).trim('/')
              val segments = if (template.isEmpty()) emptyList() else template.split('/')
              if (segments.isEmpty() || segments.size > concrete.size) continue
              val tail = concrete.subList(concrete.size - segments.size, concrete.size)
              var wildcards = 0
              var matches = true
              for (index in segments.indices) {
                val segment = segments[index]
                if (segment.startsWith("{") && segment.endsWith("}")) {
                  wildcards++
                  if (tail[index].isEmpty()) matches = false
                } else if (segment != tail[index]) {
                  matches = false
                }
              }
              if (matches) {
                val leadingWildcards = segments.takeWhile { it.startsWith("{") && it.endsWith("}") }.size
                val effectiveLength = segments.size - leadingWildcards
                val moreSpecific =
                  effectiveLength > bestEffectiveLength ||
                    (effectiveLength == bestEffectiveLength && wildcards < bestWildcards)
                if (moreSpecific) {
                  best = requirement
                  bestSegments = segments.size
                  bestEffectiveLength = effectiveLength
                  bestWildcards = wildcards
                  ambiguous = false
                } else if (requirement != best) {
                  val rawTie = segments.size == bestSegments && wildcards == bestWildcards
                  val effectiveTie = effectiveLength == bestEffectiveLength && wildcards == bestWildcards
                  if (rawTie || effectiveTie) ambiguous = true
                }
              }
            }
            return if (ambiguous) emptyMap() else best?.resolve(schemes, provider) ?: emptyMap()
            """.trimIndent()

        private val CREDENTIAL_HEADERS_BODY =
            """
            if (scheme == null || credential == null) return null
            return when (scheme) {
              is SecurityScheme.ApiKey ->
                if (scheme.location == Location.HEADER && credential is Credential.ApiKey) {
                  headerValue(credential.key)?.let { mapOf(scheme.name to it) }
                } else {
                  null
                }
              is SecurityScheme.Http ->
                when (scheme.scheme.lowercase()) {
                  "bearer" ->
                    if (credential is Credential.Token) headerValue(credential.token)?.let { mapOf("Authorization" to "Bearer " + it) } else null
                  "basic" ->
                    if (credential is Credential.Basic) {
                      mapOf(
                        "Authorization" to
                          "Basic " +
                          Base64.getEncoder().encodeToString(
                            (credential.username + ":" + credential.password).toByteArray(Charsets.ISO_8859_1),
                          ),
                      )
                    } else {
                      null
                    }
                  else -> null
                }
              is SecurityScheme.OAuth2 ->
                if (credential is Credential.Token) headerValue(credential.token)?.let { mapOf("Authorization" to "Bearer " + it) } else null
              is SecurityScheme.OpenIdConnect ->
                if (credential is Credential.Token) headerValue(credential.token)?.let { mapOf("Authorization" to "Bearer " + it) } else null
            }
            """.trimIndent()
    }
}
