package examples.bearerSecurity.client

import feign.HeaderMap
import feign.QueryMap
import feign.RequestLine
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Map

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

    public fun getProtectedWithBearerToken(
        bearerTokenProvider: (String) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ) {
        val bearerToken =
            listOf("BearerAuth").firstNotNullOfOrNull { scheme ->
                bearerTokenProvider(scheme)?.takeIf { it.isNotBlank() }
            }
        checkNotNull(bearerToken) { "A Bearer token is required for this operation" }
        val bearerHeaders =
            bearerToken?.let { additionalHeaders + ("Authorization" to "Bearer $it") }
                ?: additionalHeaders
        return getProtected(
            additionalHeaders = bearerHeaders,
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

    public fun getOptionalWithBearerToken(
        bearerTokenProvider: (String) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ) {
        val bearerToken =
            listOf("BearerAuth").firstNotNullOfOrNull { scheme ->
                bearerTokenProvider(scheme)?.takeIf { it.isNotBlank() }
            }
        val bearerHeaders =
            bearerToken?.let { additionalHeaders + ("Authorization" to "Bearer $it") }
                ?: additionalHeaders
        return getOptional(
            additionalHeaders = bearerHeaders,
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

    public fun getAlternativeWithBearerToken(
        bearerTokenProvider: (String) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ) {
        val bearerToken =
            listOf("BearerAuth").firstNotNullOfOrNull { scheme ->
                bearerTokenProvider(scheme)?.takeIf { it.isNotBlank() }
            }
        val bearerHeaders =
            bearerToken?.let { additionalHeaders + ("Authorization" to "Bearer $it") }
                ?: additionalHeaders
        return getAlternative(
            additionalHeaders = bearerHeaders,
            additionalQueryParameters = additionalQueryParameters,
        )
    }
}
