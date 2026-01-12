package examples.multipartFormData.client

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry

fun <T> withCircuitBreaker(
    circuitBreakerRegistry: CircuitBreakerRegistry,
    apiClientName: String,
    apiCall: () -> ApiResponse<T>
): ApiResponse<T> {
    val circuitBreaker = circuitBreakerRegistry.circuitBreaker(apiClientName)
    return CircuitBreaker.decorateSupplier(circuitBreaker, apiCall).get()
}
