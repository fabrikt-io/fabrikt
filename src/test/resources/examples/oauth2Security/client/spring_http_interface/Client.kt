package examples.oauth2Security.client

import org.springframework.web.bind.`annotation`.RequestHeader
import org.springframework.web.bind.`annotation`.RequestParam
import org.springframework.web.service.`annotation`.HttpExchange
import kotlin.Any
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Map
import kotlin.collections.Set

@Suppress("unused")
public interface ProtectedClient {
    /**
     *
     */
    @HttpExchange(
        url = "/protected",
        method = "GET",
    )
    public fun getProtected(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )

    public fun getProtectedWithOAuth2Token(
        oauth2TokenProvider: (String, Set<String>) -> String?,
        additionalHeaders: Map<String, Any> = emptyMap(),
        additionalQueryParameters: Map<String, Any> = emptyMap(),
    ) {
        val oauth2Token =
            listOf("OAuth2" to setOf("read:pets")).firstNotNullOfOrNull { (scheme, scopes) ->
                oauth2TokenProvider(scheme, scopes)?.takeIf { it.isNotBlank() }
            }
        checkNotNull(oauth2Token) { "An OAuth2 access token is required for this operation" }
        val oauth2Headers = oauth2Token?.let { additionalHeaders + ("Authorization" to "Bearer $it") } ?: additionalHeaders
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
    @HttpExchange(
        url = "/anonymous",
        method = "GET",
    )
    public fun getAnonymous(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )
}

@Suppress("unused")
public interface OptionalClient {
    /**
     *
     */
    @HttpExchange(
        url = "/optional",
        method = "GET",
    )
    public fun getOptional(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )

    public fun getOptionalWithOAuth2Token(
        oauth2TokenProvider: (String, Set<String>) -> String?,
        additionalHeaders: Map<String, Any> = emptyMap(),
        additionalQueryParameters: Map<String, Any> = emptyMap(),
    ) {
        val oauth2Token =
            listOf("OAuth2" to setOf("read:pets")).firstNotNullOfOrNull { (scheme, scopes) ->
                oauth2TokenProvider(scheme, scopes)?.takeIf { it.isNotBlank() }
            }
        val oauth2Headers = oauth2Token?.let { additionalHeaders + ("Authorization" to "Bearer $it") } ?: additionalHeaders
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
    @HttpExchange(
        url = "/combined",
        method = "GET",
    )
    public fun getCombined(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )
}

@Suppress("unused")
public interface AlternativeClient {
    /**
     *
     */
    @HttpExchange(
        url = "/alternative",
        method = "GET",
    )
    public fun getAlternative(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )

    public fun getAlternativeWithOAuth2Token(
        oauth2TokenProvider: (String, Set<String>) -> String?,
        additionalHeaders: Map<String, Any> = emptyMap(),
        additionalQueryParameters: Map<String, Any> = emptyMap(),
    ) {
        val oauth2Token =
            listOf("OAuth2" to setOf("read:pets")).firstNotNullOfOrNull { (scheme, scopes) ->
                oauth2TokenProvider(scheme, scopes)?.takeIf { it.isNotBlank() }
            }
        val oauth2Headers = oauth2Token?.let { additionalHeaders + ("Authorization" to "Bearer $it") } ?: additionalHeaders
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
    @HttpExchange(
        url = "/scope-alternatives",
        method = "GET",
    )
    public fun getScopeAlternatives(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )

    public fun getScopeAlternativesWithOAuth2Token(
        oauth2TokenProvider: (String, Set<String>) -> String?,
        additionalHeaders: Map<String, Any> = emptyMap(),
        additionalQueryParameters: Map<String, Any> = emptyMap(),
    ) {
        val oauth2Token =
            listOf("OAuth2" to setOf("read:pets"), "OAuth2" to setOf("write:pets")).firstNotNullOfOrNull { (scheme, scopes) ->
                oauth2TokenProvider(scheme, scopes)?.takeIf { it.isNotBlank() }
            }
        checkNotNull(oauth2Token) { "An OAuth2 access token is required for this operation" }
        val oauth2Headers = oauth2Token?.let { additionalHeaders + ("Authorization" to "Bearer $it") } ?: additionalHeaders
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
    @HttpExchange(
        url = "/unscoped",
        method = "GET",
    )
    public fun getUnscoped(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )

    public fun getUnscopedWithOAuth2Token(
        oauth2TokenProvider: (String, Set<String>) -> String?,
        additionalHeaders: Map<String, Any> = emptyMap(),
        additionalQueryParameters: Map<String, Any> = emptyMap(),
    ) {
        val oauth2Token =
            listOf("OAuth2" to setOf<String>()).firstNotNullOfOrNull { (scheme, scopes) ->
                oauth2TokenProvider(scheme, scopes)?.takeIf { it.isNotBlank() }
            }
        checkNotNull(oauth2Token) { "An OAuth2 access token is required for this operation" }
        val oauth2Headers = oauth2Token?.let { additionalHeaders + ("Authorization" to "Bearer $it") } ?: additionalHeaders
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
    @HttpExchange(
        url = "/schemes",
        method = "GET",
    )
    public fun getSchemes(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )

    public fun getSchemesWithOAuth2Token(
        oauth2TokenProvider: (String, Set<String>) -> String?,
        additionalHeaders: Map<String, Any> = emptyMap(),
        additionalQueryParameters: Map<String, Any> = emptyMap(),
    ) {
        val oauth2Token =
            listOf("OAuth2" to setOf("read:pets"), "OtherOAuth2" to setOf("write:pets")).firstNotNullOfOrNull { (scheme, scopes) ->
                oauth2TokenProvider(scheme, scopes)?.takeIf { it.isNotBlank() }
            }
        checkNotNull(oauth2Token) { "An OAuth2 access token is required for this operation" }
        val oauth2Headers = oauth2Token?.let { additionalHeaders + ("Authorization" to "Bearer $it") } ?: additionalHeaders
        return getSchemes(
            additionalHeaders = oauth2Headers,
            additionalQueryParameters = additionalQueryParameters,
        )
    }
}
