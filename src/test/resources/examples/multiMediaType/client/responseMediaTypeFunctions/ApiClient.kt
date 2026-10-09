package examples.multiMediaType.client

import examples.multiMediaType.models.ContentType
import examples.multiMediaType.models.OtherQueryResult
import examples.multiMediaType.models.QueryResult
import examples.multiMediaType.models.SuccessResponse
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.jacksonTypeRef
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.jvm.Throws

@Suppress("unused")
public class ExamplePath1Client(
    private val objectMapper: JsonMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     * GET example path 1
     *
     * @param explodeListQueryParam
     * @param queryParam2
     * @param acceptHeader
     */
    @Throws(ApiException::class)
    public fun getExamplePath1(
        explodeListQueryParam: List<String>? = null,
        queryParam2: Int? = null,
        acceptHeader: String = "application/vnd.custom.media+xml",
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<QueryResult> {
        val httpUrl: HttpUrl =
            "$baseUrl/example-path-1"
                .toHttpUrl()
                .newBuilder()
                .queryParam("explode_list_query_param", explodeListQueryParam, true)
                .queryParam("query_param2", queryParam2)
                .also { builder -> additionalQueryParameters.forEach { builder.queryParam(it.key, it.value) } }
                .build()

        val headerBuilder =
            Headers
                .Builder()
                .`header`("Accept", acceptHeader)
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

@Suppress("unused")
public class ExamplePath2Client(
    private val objectMapper: JsonMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     * GET example path 1
     *
     * @param explodeListQueryParam
     * @param queryParam2
     * @param accept the content type accepted by the client
     */
    @Throws(ApiException::class)
    public fun getExamplePath2(
        explodeListQueryParam: List<String>? = null,
        queryParam2: Int? = null,
        accept: ContentType? = null,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<QueryResult> {
        val httpUrl: HttpUrl =
            "$baseUrl/example-path-2"
                .toHttpUrl()
                .newBuilder()
                .queryParam("explode_list_query_param", explodeListQueryParam, true)
                .queryParam("query_param2", queryParam2)
                .also { builder -> additionalQueryParameters.forEach { builder.queryParam(it.key, it.value) } }
                .build()

        val headerBuilder =
            Headers
                .Builder()
                .`header`("Accept", accept?.value)
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

@Suppress("unused")
public class MultipleResponseSchemasClient(
    private val objectMapper: JsonMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     * GET with multiple response content schemas
     *
     * @param accept the content type accepted by the client
     */
    @Throws(ApiException::class)
    public fun getMultipleResponseSchemas(
        accept: ContentType? = null,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<JsonNode> {
        val httpUrl: HttpUrl =
            "$baseUrl/multiple-response-schemas"
                .toHttpUrl()
                .newBuilder()
                .also { builder -> additionalQueryParameters.forEach { builder.queryParam(it.key, it.value) } }
                .build()

        val headerBuilder =
            Headers
                .Builder()
                .`header`("Accept", accept?.value)
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
     * GET with multiple response content schemas
     */
    @Throws(ApiException::class)
    public fun getMultipleResponseSchemasJson(
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<QueryResult> {
        val httpUrl: HttpUrl =
            "$baseUrl/multiple-response-schemas"
                .toHttpUrl()
                .newBuilder()
                .also { builder -> additionalQueryParameters.forEach { builder.queryParam(it.key, it.value) } }
                .build()

        val headerBuilder = Headers.Builder()
        additionalHeaders.forEach { headerBuilder.header(it.key, it.value) }
        headerBuilder.set("Accept", "application/json")
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
     * GET with multiple response content schemas
     */
    @Throws(ApiException::class)
    public fun getMultipleResponseSchemasVndCustomMediaJson(
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<OtherQueryResult> {
        val httpUrl: HttpUrl =
            "$baseUrl/multiple-response-schemas"
                .toHttpUrl()
                .newBuilder()
                .also { builder -> additionalQueryParameters.forEach { builder.queryParam(it.key, it.value) } }
                .build()

        val headerBuilder = Headers.Builder()
        additionalHeaders.forEach { headerBuilder.header(it.key, it.value) }
        headerBuilder.set("Accept", "application/vnd.custom.media+json")
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

@Suppress("unused")
public class DifferentSuccessAndErrorResponseSchemaClient(
    private val objectMapper: JsonMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     *
     */
    @Throws(ApiException::class)
    public fun getDifferentSuccessAndErrorResponseSchema(
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<SuccessResponse> {
        val httpUrl: HttpUrl =
            "$baseUrl/different-success-and-error-response-schema"
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
