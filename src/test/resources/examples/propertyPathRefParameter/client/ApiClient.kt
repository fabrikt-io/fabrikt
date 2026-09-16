package examples.propertyPathRefParameter.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import examples.propertyPathRefParameter.models.DictValue
import examples.propertyPathRefParameter.models.Product
import examples.propertyPathRefParameter.models.ProductMeta
import examples.propertyPathRefParameter.models.ProductState
import examples.propertyPathRefParameter.models.ProductTags
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.jvm.Throws

@Suppress("unused")
public class ProductsClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     *
     *
     * @param filtersState
     * @param filtersMeta
     * @param filtersTags
     * @param filtersDict
     * @param filtersTitle
     */
    @Throws(ApiException::class)
    public fun getProducts(
        filtersState: ProductState? = null,
        filtersMeta: ProductMeta? = null,
        filtersTags: List<ProductTags>? = null,
        filtersDict: Map<String, DictValue?>? = null,
        filtersTitle: String? = null,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<List<Product>> {
        val httpUrl: HttpUrl =
            "$baseUrl/products"
                .toHttpUrl()
                .newBuilder()
                .queryParam("filters[state]", filtersState)
                .queryParam("filters[meta]", filtersMeta)
                .queryParam("filters[tags]", filtersTags, true)
                .queryParam("filters[dict]", filtersDict)
                .queryParam("filters[title]", filtersTitle)
                .also { builder -> additionalQueryParameters.forEach { builder.queryParam(it.key, it.value) } }
                .build()

        val headerBuilder = Headers.Builder()
        additionalHeaders.forEach { headerBuilder.header(it.key, it.value) }
        val httpHeaders: Headers = headerBuilder.build()

        val request: Request =
            Request
                .Builder()
                .url(httpUrl)
                .headers(httpHeaders)
                .get()
                .build()

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }
}
