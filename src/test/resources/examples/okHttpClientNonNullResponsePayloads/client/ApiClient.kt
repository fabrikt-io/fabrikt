package examples.okHttpClientNonNullResponsePayloads.client

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import examples.okHttpClientNonNullResponsePayloads.models.Widget
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.ByteArray
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.Map
import kotlin.jvm.Throws

@Suppress("unused")
public class WidgetsClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     *
     *
     * @param id
     */
    @Throws(ApiException::class)
    public fun getWidget(
        id: String,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Widget> {
        val httpUrl: HttpUrl =
            "$baseUrl/widgets/{id}"
                .pathParam("{id}" to id)
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
     * @param id
     */
    @Throws(ApiException::class)
    public fun deleteWidget(
        id: String,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/widgets/{id}"
                .pathParam("{id}" to id)
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
                .delete()
                .build()

        return request.executeWithoutResponseBody(okHttpClient)
    }
}

@Suppress("unused")
public class WidgetsImageClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     *
     *
     * @param id
     */
    @Throws(ApiException::class)
    public fun getWidgetImage(
        id: String,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<ByteArray> {
        val httpUrl: HttpUrl =
            "$baseUrl/widgets/{id}/image"
                .pathParam("{id}" to id)
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

        return request.execute(okHttpClient)
    }
}

@Suppress("unused")
public class WidgetsSummaryClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     *
     *
     * @param id
     */
    @Throws(ApiException::class)
    public fun getWidgetSummary(
        id: String,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<JsonNode> {
        val httpUrl: HttpUrl =
            "$baseUrl/widgets/{id}/summary"
                .pathParam("{id}" to id)
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
}
