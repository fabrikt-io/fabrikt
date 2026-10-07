package examples.pathLevelParameters.client

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import okhttp3.OkHttpClient
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.jacksonTypeRef
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
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
public class ExampleService(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    objectMapper: JsonMapper,
    baseUrl: String,
    okHttpClient: OkHttpClient,
) {
    public var circuitBreakerName: String = "exampleClient"

    private val apiClient: ExampleClient = ExampleClient(objectMapper, baseUrl, okHttpClient)

    @Throws(ApiException::class)
    public fun getExample(
        a: String,
        b: String,
        xJsonEncodedHeader: String? = null,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.getExample(a, b, xJsonEncodedHeader, additionalHeaders)
        }
}
