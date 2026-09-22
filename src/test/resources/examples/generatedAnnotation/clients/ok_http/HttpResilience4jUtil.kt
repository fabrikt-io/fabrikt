package com.example.client

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import javax.`annotation`.processing.Generated
import kotlin.String

@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    date = "2001-07-04T19:08:56.235Z",
    comments = "Generated with Fabrikt v27.0.1",
)
public fun <T> withCircuitBreaker(
    circuitBreakerRegistry: CircuitBreakerRegistry,
    apiClientName: String,
    apiCall: () -> ApiResponse<T>,
): ApiResponse<T> {
    val circuitBreaker = circuitBreakerRegistry.circuitBreaker(apiClientName)
    return CircuitBreaker.decorateSupplier(circuitBreaker, apiCall).get()
}
