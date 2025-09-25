package examples.multipartUpload.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import examples.multipartUpload.models.MediaFileDto
import examples.multipartUpload.models.UploadResponse
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.ByteArray
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.jvm.Throws

@Suppress("unused")
public class ApiImportClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     *
     *
     * @param metadata
     * @param mediaFile
     */
    @Throws(ApiException::class)
    public fun importMedia(
        metadata: MediaFileDto,
        mediaFile: List<ByteArray>,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<UploadResponse> {
        val httpUrl: HttpUrl = "$baseUrl/api/import"
            .toHttpUrl()
            .newBuilder()
            .also { builder -> additionalQueryParameters.forEach { builder.queryParam(it.key, it.value) } }
            .build()

        val headerBuilder = Headers.Builder()
        additionalHeaders.forEach { headerBuilder.header(it.key, it.value) }
        val httpHeaders: Headers = headerBuilder.build()

        val multipartBuilder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
        mediaFile.forEachIndexed { index, fileData ->
            multipartBuilder.addFormDataPart(
                "mediaFile",
                "file$index",
                fileData.toRequestBody("application/octet-stream".toMediaType()),
            )
        }
        multipartBuilder.addFormDataPart("metadata", objectMapper.writeValueAsString(metadata))
        val multipartBody = multipartBuilder.build()
        val request: Request = Request.Builder()
            .url(httpUrl)
            .headers(httpHeaders)
            .post(multipartBody)
            .build()

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }
}
