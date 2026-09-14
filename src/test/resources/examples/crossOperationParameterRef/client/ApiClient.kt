package examples.crossOperationParameterRef.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import examples.crossOperationParameterRef.models.Item
import examples.crossOperationParameterRef.models.ItemPatch
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Map
import kotlin.jvm.Throws

@Suppress("unused")
public class ItemsClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     *
     *
     * @param itemId
     */
    @Throws(ApiException::class)
    public fun getItem(
        itemId: String,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Item> {
        val httpUrl: HttpUrl =
            "$baseUrl/items/{itemId}"
                .pathParam("{itemId}" to itemId)
                .toHttpUrl()
                .newBuilder()
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

    /**
     *
     *
     * @param itemPatch
     * @param itemId
     */
    @Throws(ApiException::class)
    public fun patchItem(
        itemPatch: ItemPatch,
        itemId: String,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Item> {
        val httpUrl: HttpUrl =
            "$baseUrl/items/{itemId}"
                .pathParam("{itemId}" to itemId)
                .toHttpUrl()
                .newBuilder()
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
                .patch(objectMapper.writeValueAsString(itemPatch).toRequestBody("application/json".toMediaType()))
                .build()

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }
}
