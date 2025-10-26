package examples.jsonEncodedHeaders.client

import com.fasterxml.jackson.databind.ObjectMapper
import examples.jsonEncodedHeaders.models.JsonEncodedHeader
import examples.jsonEncodedHeaders.models.QueryParamsResult
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import okhttp3.OkHttpClient
import kotlin.String
import kotlin.Suppress
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
public class JsonHeaderService(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    objectMapper: ObjectMapper,
    baseUrl: String,
    okHttpClient: OkHttpClient,
) {
    public var circuitBreakerName: String = "jsonHeaderClient"

    private val apiClient: JsonHeaderClient = JsonHeaderClient(objectMapper, baseUrl, okHttpClient)

    @Throws(ApiException::class)
    public fun getJsonHeader(
        xJsonEncodedHeader: JsonEncodedHeader? = null,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<QueryParamsResult> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.getJsonHeader(xJsonEncodedHeader, additionalHeaders)
        }
}
