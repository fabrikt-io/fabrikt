package examples.tagGrouping.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import examples.tagGrouping.models.Owner
import examples.tagGrouping.models.Pet
import examples.tagGrouping.models.Vehicle
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import okhttp3.OkHttpClient
import java.util.UUID
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
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
public class PetService(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    objectMapper: ObjectMapper,
    baseUrl: String,
    okHttpClient: OkHttpClient,
) {
    public var circuitBreakerName: String = "petClient"

    private val apiClient: PetClient = PetClient(objectMapper, baseUrl, okHttpClient)

    @Throws(ApiException::class)
    public fun listPets(
        limit: Int? = null,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<List<Pet>> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.listPets(limit, additionalHeaders)
        }

    @Throws(ApiException::class)
    public fun createPet(
        pet: Pet,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.createPet(pet, additionalHeaders)
        }

    @Throws(ApiException::class)
    public fun getPetById(
        petId: UUID,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<Pet> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.getPetById(petId, additionalHeaders)
        }

    @Throws(ApiException::class)
    public fun deletePet(
        petId: UUID,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.deletePet(petId, additionalHeaders)
        }
}

/**
 * The circuit breaker registry should have the proper configuration to correctly action on circuit
 * breaker transitions based on the client exceptions [ApiClientException], [ApiServerException] and
 * [IOException].
 *
 * @see ApiClientException
 * @see ApiServerException
 */
@Suppress("unused")
public class OwnerService(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    objectMapper: ObjectMapper,
    baseUrl: String,
    okHttpClient: OkHttpClient,
) {
    public var circuitBreakerName: String = "ownerClient"

    private val apiClient: OwnerClient = OwnerClient(objectMapper, baseUrl, okHttpClient)

    @Throws(ApiException::class)
    public fun listOwners(additionalHeaders: Map<String, String> = emptyMap()): ApiResponse<List<Owner>> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.listOwners(additionalHeaders)
        }

    @Throws(ApiException::class)
    public fun createOwner(
        owner: Owner,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.createOwner(owner, additionalHeaders)
        }

    @Throws(ApiException::class)
    public fun listPetsByOwner(
        ownerId: UUID,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<List<Pet>> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.listPetsByOwner(ownerId, additionalHeaders)
        }
}

/**
 * The circuit breaker registry should have the proper configuration to correctly action on circuit
 * breaker transitions based on the client exceptions [ApiClientException], [ApiServerException] and
 * [IOException].
 *
 * @see ApiClientException
 * @see ApiServerException
 */
@Suppress("unused")
public class VehicleService(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    objectMapper: ObjectMapper,
    baseUrl: String,
    okHttpClient: OkHttpClient,
) {
    public var circuitBreakerName: String = "vehicleClient"

    private val apiClient: VehicleClient = VehicleClient(objectMapper, baseUrl, okHttpClient)

    @Throws(ApiException::class)
    public fun listVehicles(additionalHeaders: Map<String, String> = emptyMap()): ApiResponse<List<Vehicle>> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.listVehicles(additionalHeaders)
        }

    @Throws(ApiException::class)
    public fun createVehicle(
        vehicle: Vehicle,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.createVehicle(vehicle, additionalHeaders)
        }
}
