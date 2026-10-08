package examples.oauth2Security.client

import feign.HeaderMap
import feign.QueryMap
import feign.RequestLine
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Map
import kotlin.collections.Set

@Suppress("unused")
public interface ProtectedClient {
    /**
     *
     */
    @RequestLine("GET /protected")
    public fun getProtected(
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap
        additionalQueryParameters: Map<String, String> = emptyMap(),
    )

    public fun getProtectedWithOAuth2Token(
        oauth2TokenProvider: (String, Set<String>) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ) {
        val oauth2Token =
            listOf("OAuth2" to setOf("read:pets")).firstNotNullOfOrNull {
                (
                    scheme,
                    scopes,
                ),
                ->
                oauth2TokenProvider(scheme, scopes)?.takeIf { it.isNotBlank() }
            }
        checkNotNull(oauth2Token) { "An OAuth2 access token is required for this operation" }
        val oauth2Headers =
            oauth2Token?.let { additionalHeaders + ("Authorization" to "Bearer $it") }
                ?: additionalHeaders
        return getProtected(
            additionalHeaders = oauth2Headers,
            additionalQueryParameters = additionalQueryParameters,
        )
    }
}

@Suppress("unused")
public interface AnonymousClient {
    /**
     *
     */
    @RequestLine("GET /anonymous")
    public fun getAnonymous(
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap
        additionalQueryParameters: Map<String, String> = emptyMap(),
    )
}

@Suppress("unused")
public interface OptionalClient {
    /**
     *
     */
    @RequestLine("GET /optional")
    public fun getOptional(
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap
        additionalQueryParameters: Map<String, String> = emptyMap(),
    )

    public fun getOptionalWithOAuth2Token(
        oauth2TokenProvider: (String, Set<String>) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ) {
        val oauth2Token =
            listOf("OAuth2" to setOf("read:pets")).firstNotNullOfOrNull {
                (
                    scheme,
                    scopes,
                ),
                ->
                oauth2TokenProvider(scheme, scopes)?.takeIf { it.isNotBlank() }
            }
        val oauth2Headers =
            oauth2Token?.let { additionalHeaders + ("Authorization" to "Bearer $it") }
                ?: additionalHeaders
        return getOptional(
            additionalHeaders = oauth2Headers,
            additionalQueryParameters = additionalQueryParameters,
        )
    }
}

@Suppress("unused")
public interface CombinedClient {
    /**
     *
     */
    @RequestLine("GET /combined")
    public fun getCombined(
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap
        additionalQueryParameters: Map<String, String> = emptyMap(),
    )
}

@Suppress("unused")
public interface AlternativeClient {
    /**
     *
     */
    @RequestLine("GET /alternative")
    public fun getAlternative(
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    )

    public fun getAlternativeWithOAuth2Token(
        oauth2TokenProvider: (String, Set<String>) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ) {
        val oauth2Token =
            listOf("OAuth2" to setOf("read:pets")).firstNotNullOfOrNull {
                (
                    scheme,
                    scopes,
                ),
                ->
                oauth2TokenProvider(scheme, scopes)?.takeIf { it.isNotBlank() }
            }
        val oauth2Headers =
            oauth2Token?.let { additionalHeaders + ("Authorization" to "Bearer $it") }
                ?: additionalHeaders
        return getAlternative(
            additionalHeaders = oauth2Headers,
            additionalQueryParameters = additionalQueryParameters,
        )
    }
}

@Suppress("unused")
public interface ScopeAlternativesClient {
    /**
     *
     */
    @RequestLine("GET /scope-alternatives")
    public fun getScopeAlternatives(
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    )

    public fun getScopeAlternativesWithOAuth2Token(
        oauth2TokenProvider: (String, Set<String>) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ) {
        val oauth2Token =
            listOf(
                "OAuth2" to setOf("read:pets"),
                "OAuth2" to
                    setOf("write:pets"),
            ).firstNotNullOfOrNull { (scheme, scopes) ->
                oauth2TokenProvider(
                    scheme,
                    scopes,
                )?.takeIf { it.isNotBlank() }
            }
        checkNotNull(oauth2Token) { "An OAuth2 access token is required for this operation" }
        val oauth2Headers =
            oauth2Token?.let { additionalHeaders + ("Authorization" to "Bearer $it") }
                ?: additionalHeaders
        return getScopeAlternatives(
            additionalHeaders = oauth2Headers,
            additionalQueryParameters = additionalQueryParameters,
        )
    }
}

@Suppress("unused")
public interface UnscopedClient {
    /**
     *
     */
    @RequestLine("GET /unscoped")
    public fun getUnscoped(
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap
        additionalQueryParameters: Map<String, String> = emptyMap(),
    )

    public fun getUnscopedWithOAuth2Token(
        oauth2TokenProvider: (String, Set<String>) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ) {
        val oauth2Token =
            listOf("OAuth2" to setOf<String>()).firstNotNullOfOrNull { (scheme, scopes) ->
                oauth2TokenProvider(scheme, scopes)?.takeIf { it.isNotBlank() }
            }
        checkNotNull(oauth2Token) { "An OAuth2 access token is required for this operation" }
        val oauth2Headers =
            oauth2Token?.let { additionalHeaders + ("Authorization" to "Bearer $it") }
                ?: additionalHeaders
        return getUnscoped(
            additionalHeaders = oauth2Headers,
            additionalQueryParameters = additionalQueryParameters,
        )
    }
}

@Suppress("unused")
public interface SchemesClient {
    /**
     *
     */
    @RequestLine("GET /schemes")
    public fun getSchemes(
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap
        additionalQueryParameters: Map<String, String> = emptyMap(),
    )

    public fun getSchemesWithOAuth2Token(
        oauth2TokenProvider: (String, Set<String>) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ) {
        val oauth2Token =
            listOf(
                "OAuth2" to setOf("read:pets"),
                "OtherOAuth2" to
                    setOf("write:pets"),
            ).firstNotNullOfOrNull { (scheme, scopes) ->
                oauth2TokenProvider(
                    scheme,
                    scopes,
                )?.takeIf { it.isNotBlank() }
            }
        checkNotNull(oauth2Token) { "An OAuth2 access token is required for this operation" }
        val oauth2Headers =
            oauth2Token?.let { additionalHeaders + ("Authorization" to "Bearer $it") }
                ?: additionalHeaders
        return getSchemes(
            additionalHeaders = oauth2Headers,
            additionalQueryParameters = additionalQueryParameters,
        )
    }
}
