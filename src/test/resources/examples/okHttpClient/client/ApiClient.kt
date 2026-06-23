package examples.okHttpClient.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import examples.okHttpClient.models.Content
import examples.okHttpClient.models.EnumQueryParam
import examples.okHttpClient.models.FirstModel
import examples.okHttpClient.models.QueryResult
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.jvm.Throws

@Suppress("unused")
public class ExamplePath1Client(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     * GET example path 1
     *
     * @param explodeListQueryParam
     * @param queryParam2
     * @param intListQueryParam
     * @param xJsonEncodedHeader Json Encoded header
     * @param enumQueryParam
     */
    @Throws(ApiException::class)
    public fun getExamplePath1(
        explodeListQueryParam: List<String>? = null,
        queryParam2: Int? = null,
        intListQueryParam: List<Int>? = null,
        xJsonEncodedHeader: String? = null,
        enumQueryParam: EnumQueryParam? = null,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<QueryResult> {
        val httpUrl: HttpUrl =
            "$baseUrl/example-path-1"
                .toHttpUrl()
                .newBuilder()
                .queryParam("explode_list_query_param", explodeListQueryParam, true)
                .queryParam("query_param2", queryParam2)
                .queryParam("int_list_query_param", intListQueryParam, true)
                .queryParam("enum_query_param", enumQueryParam)
                .also { builder -> additionalQueryParameters.forEach { builder.queryParam(it.key, it.value) } }
                .build()

        val headerBuilder =
            Headers
                .Builder()
                .`header`("X-Json-Encoded-Header", xJsonEncodedHeader)
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
     * POST example path 1
     *
     * @param content
     * @param explodeListQueryParam
     */
    @Throws(ApiException::class)
    public fun postExamplePath1(
        content: Content,
        explodeListQueryParam: List<String>? = null,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/example-path-1"
                .toHttpUrl()
                .newBuilder()
                .queryParam("explode_list_query_param", explodeListQueryParam, true)
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
                .post(objectMapper.writeValueAsString(content).toRequestBody("application/json".toMediaType()))
                .build()

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }
}

@Suppress("unused")
public class ExamplePath2Client(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     * GET example path 2
     *
     * @param pathParam The resource id
     * @param limit
     * @param queryParam2
     * @param ifNoneMatch The RFC7232 If-None-Match header field
     */
    @Throws(ApiException::class)
    public fun getExamplePath2PathParam(
        pathParam: String,
        limit: Int = 500,
        queryParam2: Int? = null,
        ifNoneMatch: String? = null,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Content> {
        val httpUrl: HttpUrl =
            "$baseUrl/example-path-2/{path_param}"
                .pathParam("{path_param}" to pathParam)
                .toHttpUrl()
                .newBuilder()
                .queryParam("limit", limit)
                .queryParam("query_param2", queryParam2)
                .also { builder -> additionalQueryParameters.forEach { builder.queryParam(it.key, it.value) } }
                .build()

        val headerBuilder =
            Headers
                .Builder()
                .`header`("If-None-Match", ifNoneMatch)
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
     * HEAD example path 2
     *
     * @param pathParam The resource id
     * @param queryParam3
     * @param ifNoneMatch The RFC7232 If-None-Match header field
     */
    @Throws(ApiException::class)
    public fun headOperationIdExample(
        pathParam: String,
        queryParam3: Boolean? = null,
        ifNoneMatch: String? = null,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/example-path-2/{path_param}"
                .pathParam("{path_param}" to pathParam)
                .toHttpUrl()
                .newBuilder()
                .queryParam("query_param3", queryParam3)
                .also { builder -> additionalQueryParameters.forEach { builder.queryParam(it.key, it.value) } }
                .build()

        val headerBuilder =
            Headers
                .Builder()
                .`header`("If-None-Match", ifNoneMatch)
        additionalHeaders.forEach { headerBuilder.header(it.key, it.value) }
        val httpHeaders: Headers = headerBuilder.build()

        val request: Request =
            Request
                .Builder()
                .url(httpUrl)
                .headers(httpHeaders)
                .head()
                .build()

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }

    /**
     * PUT example path 2
     *
     * @param firstModel
     * @param pathParam The resource id
     * @param ifMatch The RFC7232 If-Match header field
     */
    @Throws(ApiException::class)
    public fun putExamplePath2PathParam(
        firstModel: FirstModel,
        pathParam: String,
        ifMatch: String,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/example-path-2/{path_param}"
                .pathParam("{path_param}" to pathParam)
                .toHttpUrl()
                .newBuilder()
                .also { builder -> additionalQueryParameters.forEach { builder.queryParam(it.key, it.value) } }
                .build()

        val headerBuilder =
            Headers
                .Builder()
                .`header`("If-Match", ifMatch)
        additionalHeaders.forEach { headerBuilder.header(it.key, it.value) }
        val httpHeaders: Headers = headerBuilder.build()

        val request: Request =
            Request
                .Builder()
                .url(httpUrl)
                .headers(httpHeaders)
                .put(objectMapper.writeValueAsString(firstModel).toRequestBody("application/json".toMediaType()))
                .build()

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }
}

@Suppress("unused")
public class ExamplePath3SubresourceClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     * PUT example path 3
     *
     * @param firstModel
     * @param pathParam The resource id
     * @param ifMatch The RFC7232 If-Match header field
     * @param csvListQueryParam
     */
    @Throws(ApiException::class)
    public fun putExamplePath3PathParamSubresource(
        firstModel: FirstModel,
        pathParam: String,
        ifMatch: String,
        csvListQueryParam: List<String>? = null,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/example-path-3/{path_param}/subresource"
                .pathParam("{path_param}" to pathParam)
                .toHttpUrl()
                .newBuilder()
                .queryParam("csv_list_query_param", csvListQueryParam, false)
                .also { builder -> additionalQueryParameters.forEach { builder.queryParam(it.key, it.value) } }
                .build()

        val headerBuilder =
            Headers
                .Builder()
                .`header`("If-Match", ifMatch)
        additionalHeaders.forEach { headerBuilder.header(it.key, it.value) }
        val httpHeaders: Headers = headerBuilder.build()

        val request: Request =
            Request
                .Builder()
                .url(httpUrl)
                .headers(httpHeaders)
                .put(objectMapper.writeValueAsString(firstModel).toRequestBody("application/json".toMediaType()))
                .build()

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }
}

@Suppress("unused")
public class ExamplePath4OnlyFailureResponseClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     * POST example path 4
     */
    @Throws(ApiException::class)
    public fun postExamplePath4OnlyFailureResponse(
        additionalHeaders: Map<String, String> =
            emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/example-path-4/only-failure-response"
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
                .post(ByteArray(0).toRequestBody())
                .build()

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }
}
