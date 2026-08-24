package examples.pathLevelParameters.client

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import kotlin.String

public fun <T> withCircuitBreaker(
    circuitBreakerRegistry: CircuitBreakerRegistry,
    apiClientName: String,
    apiCall: () -> ApiResponse<T>,
): ApiResponse<T> {
    val circuitBreaker = circuitBreakerRegistry.circuitBreaker(apiClientName)
    return CircuitBreaker.decorateSupplier(circuitBreaker, apiCall).get()
}
