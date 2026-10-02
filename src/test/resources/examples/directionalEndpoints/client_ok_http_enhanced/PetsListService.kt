package examples.directionalEndpoints.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import examples.directionalEndpoints.models.PetResponse
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
public class PetsListService(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    objectMapper: ObjectMapper,
    baseUrl: String,
    okHttpClient: OkHttpClient,
) {
    public var circuitBreakerName: String = "petsListClient"

    private val apiClient: PetsListClient = PetsListClient(objectMapper, baseUrl, okHttpClient)

    @Throws(ApiException::class)
    public fun getPets(additionalHeaders: Map<String, String> = emptyMap()): ApiResponse<List<PetResponse>> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.getPets(additionalHeaders)
        }
}
