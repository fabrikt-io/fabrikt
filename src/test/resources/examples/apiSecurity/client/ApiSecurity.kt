package examples.apiSecurity.client

import java.util.Base64
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.Set

public sealed interface SecurityScheme {
    public data class Http(
        public val scheme: String,
        public val bearerFormat: String? = null,
    ) : SecurityScheme

    public data class ApiKey(
        public val location: Location,
        public val name: String,
    ) : SecurityScheme

    public data class OAuth2(
        public val flows: Map<Flow, OAuth2Flow>,
    ) : SecurityScheme

    public data class OpenIdConnect(
        public val openIdConnectUrl: String,
    ) : SecurityScheme
}

public enum class Location {
    HEADER,
    QUERY,
    COOKIE,
}

public enum class Flow {
    CLIENT_CREDENTIALS,
    AUTHORIZATION_CODE,
    IMPLICIT,
    PASSWORD,
}

public data class OAuth2Flow(
    public val authorizationUrl: String?,
    public val tokenUrl: String?,
    public val refreshUrl: String?,
    public val scopes: Map<String, String>,
)

public data class SecurityRequirement(
    public val alternatives: List<Map<String, Set<String>>>,
)

public sealed interface Credential {
    public data class Token(
        public val token: String,
    ) : Credential

    public data class Basic(
        public val username: String,
        public val password: String,
    ) : Credential

    public data class ApiKey(
        public val key: String,
    ) : Credential
}

public object ApiSecurity {
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

    public fun resolveFor(
        method: String,
        path: String,
        provider: (scheme: String, scopes: Set<String>) -> Credential?,
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
                mapOf(scheme.name to credential.key)
            } else {
                null
            }
        is SecurityScheme.Http ->
            when (scheme.scheme.lowercase()) {
                "bearer" ->
                    if (credential is Credential.Token) mapOf("Authorization" to "Bearer " + credential.token) else null
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
            if (credential is Credential.Token) mapOf("Authorization" to "Bearer " + credential.token) else null
        is SecurityScheme.OpenIdConnect ->
            if (credential is Credential.Token) mapOf("Authorization" to "Bearer " + credential.token) else null
    }
}

public fun SecurityRequirement.resolve(
    schemes: Map<String, SecurityScheme>,
    provider: (scheme: String, scopes: Set<String>) -> Credential?,
): Map<String, String> {
    for (alternative in alternatives) {
        if (alternative.isEmpty()) continue
        val headers = linkedMapOf<String, String>()
        var complete = true
        for ((schemeName, scopes) in alternative) {
            val resolved = credentialHeaders(schemes[schemeName], provider(schemeName, scopes))
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
