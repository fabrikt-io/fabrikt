package com.example.client

import com.example.models.Pet
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import okhttp3.OkHttpClient
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
    date = "2001-07-04T19:08:56.235Z",
    comments = "Generated with Fabrikt v27.0.1",
)
public class PetsService(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    objectMapper: ObjectMapper,
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
