package examples.deleteRequestBody.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import examples.deleteRequestBody.models.DeleteResourcesRequest
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.MultipartBody.Builder
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.Map
import kotlin.jvm.Throws

@Suppress("unused")
public class ResourcesClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     *
     *
     * @param resourceId
     */
    @Throws(ApiException::class)
    public fun deleteResource(
        resourceId: String,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/resources/{resourceId}"
                .pathParam("{resourceId}" to resourceId)
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

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }
}

@Suppress("unused")
public class ResourcesSearchClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     *
     *
     * @param deleteResourcesRequest
     */
    @Throws(ApiException::class)
    public fun deleteResources(
        deleteResourcesRequest: DeleteResourcesRequest,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/resources/search"
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
                .delete(objectMapper.writeValueAsString(deleteResourcesRequest).toRequestBody("application/json".toMediaType()))
                .build()

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }
}

@Suppress("unused")
public class ResourcesAttachmentsClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     *
     *
     * @param attachment
     * @param reason
     */
    @Throws(ApiException::class)
    public fun deleteResourceAttachments(
        attachment: RequestBodyWithFilename,
        reason: String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/resources/attachments"
                .toHttpUrl()
                .newBuilder()
                .also { builder -> additionalQueryParameters.forEach { builder.queryParam(it.key, it.value) } }
                .build()

        val headerBuilder = Headers.Builder()
        additionalHeaders.forEach { headerBuilder.header(it.key, it.value) }
        val httpHeaders: Headers = headerBuilder.build()

        val multipartBuilder =
            MultipartBody
                .Builder()
                .setType(MultipartBody.FORM)
        multipartBuilder.addFormDataPart("attachment", attachment.filename, attachment.requestBody)
        reason?.let {
            multipartBuilder.addFormDataPart("reason", reason.toString())
        }
        val multipartBody = multipartBuilder.build()
        val request: Request =
            Request
                .Builder()
                .url(httpUrl)
                .headers(httpHeaders)
                .delete(multipartBody)
                .build()

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }
}
