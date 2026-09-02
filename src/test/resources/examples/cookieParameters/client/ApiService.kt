package examples.cookieParameters.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import examples.cookieParameters.models.CookiePreferences
import examples.cookieParameters.models.DisplayMode
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import okhttp3.OkHttpClient
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.jvm.Throws

/**
 * The circuit breaker registry should have the proper configuration to correctly action on circuit
 * breaker transitions based on the client exceptions [ApiClientException], [ApiServerException] and
 * [IOException].
 *
 * @see ApiClientException
 * @see ApiServerException
 */
@Suppress("unused")
public class CookiesService(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    objectMapper: ObjectMapper,
    baseUrl: String,
    okHttpClient: OkHttpClient,
) {
    public var circuitBreakerName: String = "cookiesClient"

    private val apiClient: CookiesClient = CookiesClient(objectMapper, baseUrl, okHttpClient)

    @Throws(ApiException::class)
    public fun getCookiePreferences(
        id: String,
        sessionId: String,
        displayMode: DisplayMode,
        features: List<String>,
        locale: String? = null,
        scopes: List<String>? = null,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<CookiePreferences> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.getCookiePreferences(id, sessionId, displayMode, features, locale, scopes, additionalHeaders)
        }
}
