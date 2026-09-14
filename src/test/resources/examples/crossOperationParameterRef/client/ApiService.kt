package examples.crossOperationParameterRef.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import examples.crossOperationParameterRef.models.Item
import examples.crossOperationParameterRef.models.ItemPatch
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
public class ItemsService(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    objectMapper: ObjectMapper,
    baseUrl: String,
    okHttpClient: OkHttpClient,
) {
    public var circuitBreakerName: String = "itemsClient"

    private val apiClient: ItemsClient = ItemsClient(objectMapper, baseUrl, okHttpClient)

    @Throws(ApiException::class)
    public fun getItem(
        itemId: String,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<Item> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.getItem(itemId, additionalHeaders)
        }

    @Throws(ApiException::class)
    public fun patchItem(
        itemPatch: ItemPatch,
        itemId: String,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<Item> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.patchItem(itemPatch, itemId, additionalHeaders)
        }
}
