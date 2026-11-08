package examples.apiSecurity.client

import java.util.Base64
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.Set

/**
 * One entry in the schemes dictionary. Only header api keys and bearer/basic/OpenID Connect tokens resolve to request headers here; the other shapes are contract data.
 */
public sealed interface SecurityScheme {
    /**
     * RFC 7235 HTTP scheme. 'bearer' and 'basic' resolve to headers; other values do not.
     */
    public data class Http(
        public val scheme: String,
        public val bearerFormat: String? = null,
    ) : SecurityScheme

    /**
     * Key in a header, query or cookie. Only the header location resolves to a request header.
     */
    public data class ApiKey(
        public val location: Location,
        public val name: String,
    ) : SecurityScheme

    /**
     * OAuth2 scheme. The flows are metadata; the resolver turns a token into a Bearer header.
     */
    public data class OAuth2(
        public val flows: Map<Flow, OAuth2Flow>,
    ) : SecurityScheme

    /**
     * OpenID Connect discovery document. The resolver treats it as a Bearer token source.
     */
    public data class OpenIdConnect(
        public val openIdConnectUrl: String,
    ) : SecurityScheme
}

/**
 * Where an api key travels. HEADER resolves to a request header; QUERY and COOKIE do not.
 */
public enum class Location {
    HEADER,
    QUERY,
    COOKIE,
}

/**
 * OAuth2 flow names, preserved from the spec as data.
 */
public enum class Flow {
    CLIENT_CREDENTIALS,
    AUTHORIZATION_CODE,
    IMPLICIT,
    PASSWORD,
}

/**
 * One OAuth2 flow: its endpoints and scope descriptions. Contract data; never consumed by the resolver.
 */
public data class OAuth2Flow(
    public val authorizationUrl: String?,
    public val tokenUrl: String?,
    public val refreshUrl: String?,
    public val scopes: Map<String, String>,
)

/**
 * One alternative in a security requirement: an AND group mapping scheme name to required scopes.
 */
public typealias SecurityAlternative = Map<String, Set<String>>

/**
 * One operation's effective requirement. Each entry in alternatives is an OR option; the scheme-to-scopes map inside an entry is an AND group. An empty list means security: [].
 */
public data class SecurityRequirement(
    public val alternatives: List<SecurityAlternative>,
)

/**
 * What the caller's provider returns for one scheme. A token becomes a Bearer header, basic credentials the Basic header, and an api key its header name.
 */
public sealed interface Credential {
    /**
     * Access token, without the Bearer prefix.
     */
    public data class Token(
        public val token: String,
    ) : Credential

    /**
     * Username and password for HTTP Basic.
     */
    public data class Basic(
        public val username: String,
        public val password: String,
    ) : Credential

    /**
     * The key value sent under the scheme's header name.
     */
    public data class ApiKey(
        public val key: String,
    ) : Credential
}

/**
 * Returns the caller's credential for one scheme and its required scopes, or null when that credential is unavailable, so a later alternative is tried.
 */
public fun interface CredentialProvider {
    /**
     * Provides the credential for the named security scheme. The scopes are those the operation requires for that scheme; returning null skips the alternative. The returned token must actually grant every requested scope, since the resolver cannot verify claims.
     */
    public fun provide(
        scheme: String,
        scopes: Set<String>,
    ): Credential?
}

/**
 * The security declaration for this API. Contract data only: nothing here sends a request or attaches credentials. Feed the maps and resolver results into the client yourself.
 */
public object ApiSecurity {
    /**
     * Security schemes keyed by their component name. Header api keys and bearer/basic/OpenID Connect tokens resolve to headers; query/cookie keys are kept as contract data. Unsupported types such as mutualTLS are omitted with a warning, since they are transport-level, not headers.
     */
    public val schemes: Map<String, SecurityScheme> =
        mapOf(
            "ApiKeyAuth" to SecurityScheme.ApiKey(Location.HEADER, "X-API-Key"),
            "BasicAuth" to SecurityScheme.Http("basic"),
            "BearerAuth" to SecurityScheme.Http("bearer", "JWT"),
            "CaseKey" to SecurityScheme.ApiKey(Location.HEADER, "x-api-key"),
            "OAuth" to
                SecurityScheme.OAuth2(
                    mapOf(
                        Flow.CLIENT_CREDENTIALS to
                            OAuth2Flow(
                                authorizationUrl = null,
                                tokenUrl = "https://example.com/oauth/token",
                                refreshUrl = null,
                                scopes =
                                    mapOf(
                                        "read:documents" to "Read documents",
                                        "write:documents" to "Write documents",
                                    ),
                            ),
                        Flow.AUTHORIZATION_CODE to
                            OAuth2Flow(
                                authorizationUrl = "https://example.com/oauth/authorize",
                                tokenUrl = "https://example.com/oauth/token",
                                refreshUrl = "https://example.com/oauth/refresh",
                                scopes =
                                    mapOf(
                                        "read:documents" to "Read documents",
                                    ),
                            ),
                    ),
                ),
            "OpenId" to SecurityScheme.OpenIdConnect("https://example.com/.well-known/openid-configuration"),
            "QueryKey" to SecurityScheme.ApiKey(Location.QUERY, "api_key"),
            "SecondKey" to SecurityScheme.ApiKey(Location.HEADER, "X-API-Key"),
            "TenantKey" to SecurityScheme.ApiKey(Location.HEADER, "X-Tenant-Key"),
        )

    /**
     * Every operation's effective requirement, keyed by the raw operationId, or by "VERB /path" when an operation has none. The key is the spec's operationId as written (e.g. get_document_by_id), not the generated method name (getDocumentById).
     */
    public val requirements: Map<String, SecurityRequirement> =
        mapOf(
            "deleteDocument" to SecurityRequirement(emptyList()),
            "GET /anonymous" to
                SecurityRequirement(
                    listOf(
                        mapOf(
                            "ApiKeyAuth" to emptySet<String>(),
                        ),
                    ),
                ),
            "getHeaderConflict" to
                SecurityRequirement(
                    listOf(
                        mapOf(
                            "ApiKeyAuth" to emptySet<String>(),
                            "SecondKey" to emptySet<String>(),
                        ),
                        mapOf(
                            "ApiKeyAuth" to emptySet<String>(),
                            "CaseKey" to emptySet<String>(),
                        ),
                        mapOf(
                            "BearerAuth" to emptySet<String>(),
                        ),
                    ),
                ),
            "getDocuments" to
                SecurityRequirement(
                    listOf(
                        mapOf(
                            "ApiKeyAuth" to emptySet<String>(),
                        ),
                    ),
                ),
            "get_document_by_id" to
                SecurityRequirement(
                    listOf(
                        mapOf(
                            "BearerAuth" to emptySet<String>(),
                        ),
                        mapOf(
                            "BasicAuth" to emptySet<String>(),
                        ),
                    ),
                ),
            "getReports" to
                SecurityRequirement(
                    listOf(
                        mapOf(
                            "QueryKey" to emptySet<String>(),
                        ),
                        mapOf(
                            "ApiKeyAuth" to emptySet<String>(),
                        ),
                    ),
                ),
            "getSession" to
                SecurityRequirement(
                    listOf(
                        mapOf(
                            "OpenId" to emptySet<String>(),
                        ),
                    ),
                ),
            "getStatus" to
                SecurityRequirement(
                    listOf(
                        emptyMap(),
                        mapOf(
                            "OAuth" to setOf("read:documents"),
                        ),
                    ),
                ),
            "getSubDocument" to
                SecurityRequirement(
                    listOf(
                        mapOf(
                            "ApiKeyAuth" to emptySet<String>(),
                        ),
                    ),
                ),
            "getTeam" to
                SecurityRequirement(
                    listOf(
                        mapOf(
                            "BearerAuth" to emptySet<String>(),
                        ),
                    ),
                ),
            "listMembers" to
                SecurityRequirement(
                    listOf(
                        mapOf(
                            "ApiKeyAuth" to emptySet<String>(),
                        ),
                    ),
                ),
            "getTenantDocument" to
                SecurityRequirement(
                    listOf(
                        mapOf(
                            "TenantKey" to emptySet<String>(),
                        ),
                    ),
                ),
            "createDocument" to
                SecurityRequirement(
                    listOf(
                        mapOf(
                            "ApiKeyAuth" to emptySet<String>(),
                            "OAuth" to setOf("read:documents", "write:documents"),
                        ),
                    ),
                ),
        )

    /**
     * The same requirements keyed by "VERB /path template", for transport interceptors that only know a method and a path.
     */
    public val requirementsByPath: Map<String, SecurityRequirement> =
        mapOf(
            "DELETE /documents/{id}" to SecurityRequirement(emptyList()),
            "GET /anonymous" to
                SecurityRequirement(
                    listOf(
                        mapOf(
                            "ApiKeyAuth" to emptySet<String>(),
                        ),
                    ),
                ),
            "GET /conflict" to
                SecurityRequirement(
                    listOf(
                        mapOf(
                            "ApiKeyAuth" to emptySet<String>(),
                            "SecondKey" to emptySet<String>(),
                        ),
                        mapOf(
                            "ApiKeyAuth" to emptySet<String>(),
                            "CaseKey" to emptySet<String>(),
                        ),
                        mapOf(
                            "BearerAuth" to emptySet<String>(),
                        ),
                    ),
                ),
            "GET /documents" to
                SecurityRequirement(
                    listOf(
                        mapOf(
                            "ApiKeyAuth" to emptySet<String>(),
                        ),
                    ),
                ),
            "GET /documents/{id}" to
                SecurityRequirement(
                    listOf(
                        mapOf(
                            "BearerAuth" to emptySet<String>(),
                        ),
                        mapOf(
                            "BasicAuth" to emptySet<String>(),
                        ),
                    ),
                ),
            "GET /reports" to
                SecurityRequirement(
                    listOf(
                        mapOf(
                            "QueryKey" to emptySet<String>(),
                        ),
                        mapOf(
                            "ApiKeyAuth" to emptySet<String>(),
                        ),
                    ),
                ),
            "GET /session" to
                SecurityRequirement(
                    listOf(
                        mapOf(
                            "OpenId" to emptySet<String>(),
                        ),
                    ),
                ),
            "GET /status" to
                SecurityRequirement(
                    listOf(
                        emptyMap(),
                        mapOf(
                            "OAuth" to setOf("read:documents"),
                        ),
                    ),
                ),
            "GET /sub/documents/{id}" to
                SecurityRequirement(
                    listOf(
                        mapOf(
                            "ApiKeyAuth" to emptySet<String>(),
                        ),
                    ),
                ),
            "GET /teams/{id}" to
                SecurityRequirement(
                    listOf(
                        mapOf(
                            "BearerAuth" to emptySet<String>(),
                        ),
                    ),
                ),
            "GET /{name}/members" to
                SecurityRequirement(
                    listOf(
                        mapOf(
                            "ApiKeyAuth" to emptySet<String>(),
                        ),
                    ),
                ),
            "GET /{tenant}/documents/{id}" to
                SecurityRequirement(
                    listOf(
                        mapOf(
                            "TenantKey" to emptySet<String>(),
                        ),
                    ),
                ),
            "POST /documents" to
                SecurityRequirement(
                    listOf(
                        mapOf(
                            "ApiKeyAuth" to emptySet<String>(),
                            "OAuth" to setOf("read:documents", "write:documents"),
                        ),
                    ),
                ),
        )

    /**
     * Resolves the requirement for one concrete request. Exact "METHOD /path" match first, then
     * trailing-segment alignment of same-verb templates; a leading path parameter never earns
     * specificity because it may be the transport's base path. A literal base-path prefix is not
     * disambiguated from a matching literal route segment, so strip a known prefix first. Two equally
     * specific routes, or a provider that yields nothing, produce an empty map, never a throw.
     */
    public fun resolveFor(
        method: String,
        path: String,
        provider: CredentialProvider,
    ): Map<String, String> {
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
    }
}

private fun credentialHeaders(
    scheme: SecurityScheme?,
    credential: Credential?,
): Map<String, String>? {
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
                    if (credential is Credential.Token) {
                        headerValue(
                            credential.token,
                        )?.let { mapOf("Authorization" to "Bearer " + it) }
                    } else {
                        null
                    }
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
}

private fun headerValue(`value`: String): String? =
    value.takeIf { candidate ->
        candidate.isNotBlank() &&
            candidate.none { character -> character.code < 32 || character.code == 127 }
    }

/**
 * Turns this requirement and caller credentials into request headers. Alternatives are tried in
 * order; a null credential, a credential that mismatches its scheme, a blank token, an api-key
 * value containing control characters, or two credentials on the same header name
 * (case-insensitively) with different values make that alternative incomplete. The first
 * complete alternative's headers are returned, otherwise an empty map.
 */
public fun SecurityRequirement.resolve(
    schemes: Map<String, SecurityScheme>,
    provider: CredentialProvider,
): Map<String, String> {
    for (alternative in alternatives) {
        if (alternative.isEmpty()) continue
        val headers = linkedMapOf<String, String>()
        var complete = true
        for ((schemeName, scopes) in alternative) {
            val scheme = schemes[schemeName]
            val resolved = if (scheme == null) null else credentialHeaders(scheme, provider.provide(schemeName, scopes))
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
}

/**
 * Like [resolve], but throws when this requirement has alternatives, none is anonymous, and
 * none complete. security: [] and requirements with an anonymous {} alternative return an
 * empty map rather than throwing.
 */
public fun SecurityRequirement.resolveOrThrow(
    schemes: Map<String, SecurityScheme>,
    provider: CredentialProvider,
): Map<String, String> {
    val headers = resolve(schemes, provider)
    check(headers.isNotEmpty() || alternatives.isEmpty() || alternatives.any { it.isEmpty() }) {
        "Credentials for a complete security alternative are required for this operation"
    }
    return headers
}
