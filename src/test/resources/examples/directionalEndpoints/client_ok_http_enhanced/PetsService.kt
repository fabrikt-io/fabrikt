package examples.directionalEndpoints.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import examples.directionalEndpoints.models.PetRequest
import examples.directionalEndpoints.models.PetResponse
import examples.directionalEndpoints.models.PetStatusRequest
import examples.directionalEndpoints.models.StateRequest
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
public class PetsService(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    objectMapper: ObjectMapper,
    baseUrl: String,
    okHttpClient: OkHttpClient,
) {
    public var circuitBreakerName: String = "petsClient"

    private val apiClient: PetsClient = PetsClient(objectMapper, baseUrl, okHttpClient)

    @Throws(ApiException::class)
    public fun createPet(
        pet: PetRequest,
        status: PetStatusRequest? = null,
        state: StateRequest? = null,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<PetResponse> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.createPet(pet, status, state, additionalHeaders)
        }
}
