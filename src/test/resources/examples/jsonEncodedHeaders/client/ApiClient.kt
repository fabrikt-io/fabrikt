package examples.jsonEncodedHeaders.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import examples.jsonEncodedHeaders.models.JsonEncodedHeader
import examples.jsonEncodedHeaders.models.QueryParamsResult
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Map
import kotlin.jvm.Throws

@Suppress("unused")
public class JsonHeaderClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     * GET with json encoded header
     *
     * @param xJsonEncodedHeader Json Encoded header
     */
    @Throws(ApiException::class)
    public fun getJsonHeader(
        xJsonEncodedHeader: JsonEncodedHeader? = null,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<QueryParamsResult> {
        val httpUrl: HttpUrl = "$baseUrl/json-header"
            .toHttpUrl()
            .newBuilder()
            .also { builder -> additionalQueryParameters.forEach { builder.queryParam(it.key, it.value) } }
            .build()

        val headerBuilder = Headers.Builder()
            .`header`("X-Json-Encoded-Header", objectMapper.writeValueAsString(xJsonEncodedHeader))
        additionalHeaders.forEach { headerBuilder.header(it.key, it.value) }
        val httpHeaders: Headers = headerBuilder.build()

        val request: Request = Request.Builder()
            .url(httpUrl)
            .headers(httpHeaders)
            .get()
            .build()

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }
}
