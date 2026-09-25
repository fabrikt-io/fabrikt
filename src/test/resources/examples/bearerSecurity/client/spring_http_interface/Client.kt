package examples.bearerSecurity.client

import org.springframework.web.bind.`annotation`.RequestHeader
import org.springframework.web.bind.`annotation`.RequestParam
import org.springframework.web.service.`annotation`.HttpExchange
import kotlin.Any
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Map

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

    public fun getProtectedWithBearerToken(
        bearerTokenProvider: (String) -> String?,
        additionalHeaders: Map<String, Any> = emptyMap(),
        additionalQueryParameters: Map<String, Any> = emptyMap(),
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

    public fun getOptionalWithBearerToken(
        bearerTokenProvider: (String) -> String?,
        additionalHeaders: Map<String, Any> = emptyMap(),
        additionalQueryParameters: Map<String, Any> = emptyMap(),
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

    public fun getAlternativeWithBearerToken(
        bearerTokenProvider: (String) -> String?,
        additionalHeaders: Map<String, Any> = emptyMap(),
        additionalQueryParameters: Map<String, Any> = emptyMap(),
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
