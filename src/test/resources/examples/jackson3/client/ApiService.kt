package examples.jackson3.client

import examples.jackson3.models.Widget
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import okhttp3.OkHttpClient
import tools.jackson.databind.JsonNode
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
public class WidgetsService(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    objectMapper: JsonMapper,
    baseUrl: String,
    okHttpClient: OkHttpClient,
) {
    public var circuitBreakerName: String = "widgetsClient"

    private val apiClient: WidgetsClient = WidgetsClient(objectMapper, baseUrl, okHttpClient)

    @Throws(ApiException::class)
    public fun getWidgets(additionalHeaders: Map<String, String> = emptyMap()): ApiResponse<JsonNode> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.getWidgets(additionalHeaders)
        }

    @Throws(ApiException::class)
    public fun createWidget(
        widget: Widget,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.createWidget(widget, additionalHeaders)
        }
}
