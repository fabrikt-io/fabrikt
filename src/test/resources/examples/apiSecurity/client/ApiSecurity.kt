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
        )

    public val requirements: Map<String, SecurityRequirement> =
        mapOf(
            "getDocuments" to
                SecurityRequirement(
                    listOf(
                        mapOf(
                            "ApiKeyAuth" to emptySet<String>(),
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
            "deleteDocument" to SecurityRequirement(emptyList()),
            "getSession" to
                SecurityRequirement(
                    listOf(
                        mapOf(
                            "OpenId" to emptySet<String>(),
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
            "getStatus" to
                SecurityRequirement(
                    listOf(
                        emptyMap(),
                        mapOf(
                            "OAuth" to setOf("read:documents"),
                        ),
                    ),
                ),
            "GET /anonymous" to
                SecurityRequirement(
                    listOf(
                        mapOf(
                            "ApiKeyAuth" to emptySet<String>(),
                        ),
                    ),
                ),
        )

    public val requirementsByPath: Map<String, SecurityRequirement> =
        mapOf(
            "GET /documents" to
                SecurityRequirement(
                    listOf(
                        mapOf(
                            "ApiKeyAuth" to emptySet<String>(),
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
            "DELETE /documents/{id}" to SecurityRequirement(emptyList()),
            "GET /session" to
                SecurityRequirement(
                    listOf(
                        mapOf(
                            "OpenId" to emptySet<String>(),
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
            "GET /status" to
                SecurityRequirement(
                    listOf(
                        emptyMap(),
                        mapOf(
                            "OAuth" to setOf("read:documents"),
                        ),
                    ),
                ),
            "GET /anonymous" to
                SecurityRequirement(
                    listOf(
                        mapOf(
                            "ApiKeyAuth" to emptySet<String>(),
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
        var bestWildcards = Int.MAX_VALUE
        for ((templateKey, requirement) in requirementsByPath) {
            val separator = templateKey.indexOf(' ')
            if (separator <= 0 || templateKey.substring(0, separator) != methodKey) continue
            val template = templateKey.substring(separator + 1).trim('/')
            val segments = if (template.isEmpty()) emptyList() else template.split('/')
            if (segments.size > concrete.size) continue
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
            if (matches && wildcards < bestWildcards) {
                best = requirement
                bestWildcards = wildcards
            }
        }
        return best?.resolve(schemes, provider) ?: emptyMap()
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
            headers.putAll(resolved)
        }
        if (complete) return headers
    }
    return emptyMap()
}
