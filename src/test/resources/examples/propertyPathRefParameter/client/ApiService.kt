package examples.propertyPathRefParameter.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import examples.propertyPathRefParameter.models.DictValue
import examples.propertyPathRefParameter.models.Product
import examples.propertyPathRefParameter.models.ProductMeta
import examples.propertyPathRefParameter.models.ProductState
import examples.propertyPathRefParameter.models.ProductTags
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
public class ProductsService(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    objectMapper: ObjectMapper,
    baseUrl: String,
    okHttpClient: OkHttpClient,
) {
    public var circuitBreakerName: String = "productsClient"

    private val apiClient: ProductsClient = ProductsClient(objectMapper, baseUrl, okHttpClient)

    @Throws(ApiException::class)
    public fun getProducts(
        filtersState: ProductState? = null,
        filtersMeta: ProductMeta? = null,
        filtersTags: List<ProductTags>? = null,
        filtersDict: Map<String, DictValue?>? = null,
        filtersTitle: String? = null,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<List<Product>> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.getProducts(filtersState, filtersMeta, filtersTags, filtersDict, filtersTitle, additionalHeaders)
        }
}
