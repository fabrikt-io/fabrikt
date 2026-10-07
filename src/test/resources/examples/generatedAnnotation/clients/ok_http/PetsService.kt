package com.example.client

import com.example.models.Pet
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import okhttp3.OkHttpClient
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.jacksonTypeRef
import javax.`annotation`.processing.Generated
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
@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    comments = "Generated with Fabrikt v27.0.1",
)
public class PetsService(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    objectMapper: JsonMapper,
    baseUrl: String,
    okHttpClient: OkHttpClient,
) {
    public var circuitBreakerName: String = "petsClient"

    private val apiClient: PetsClient = PetsClient(objectMapper, baseUrl, okHttpClient)

    @Throws(ApiException::class)
    public fun listPets(additionalHeaders: Map<String, String> = emptyMap()): ApiResponse<List<Pet>> =

        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.listPets(additionalHeaders)
        }
}
