package examples.parameterNameClash.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import examples.parameterNameClash.models.SomeObject
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import okhttp3.OkHttpClient
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
    objectMapper: ObjectMapper,
    baseUrl: String,
    okHttpClient: OkHttpClient,
) {
    public var circuitBreakerName: String = "exampleClient"

    private val apiClient: ExampleClient = ExampleClient(objectMapper, baseUrl, okHttpClient)

    @Throws(ApiException::class)
    public fun getExampleB(
        pathB: String,
        queryB: String,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.getExampleB(pathB, queryB, additionalHeaders)
        }

    @Throws(ApiException::class)
    public fun postExample(
        bodySomeObject: SomeObject?,
        querySomeObject: String,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.postExample(bodySomeObject, querySomeObject, additionalHeaders)
        }
}
