package examples.multipartFormData.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import examples.multipartFormData.models.UploadResponse
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.jvm.Throws

@Suppress("unused")
public class FilesUploadClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     * Upload a single file
     *
     * @param file The file to upload
     * @param description Optional description of the file
     */
    @Throws(ApiException::class)
    public fun uploadFile(
        `file`: FileUpload,
        description: String? = null,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<UploadResponse> {
        val httpUrl: HttpUrl =
            "$baseUrl/files/upload"
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
                .post(
                    MultipartBody
                        .Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart(
                            "file",
                            `file`.filename ?: "file",
                            `file`.content.toRequestBody("application/octet-stream".toMediaType()),
                        ).also { builder ->
                            description?.let {
                                builder.addFormDataPart(
                                    "description",
                                    objectMapper.writeValueAsString(it),
                                )
                            }
                        }.build(),
                ).build()

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }
}

@Suppress("unused")
public class FilesUploadMultipleClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     * Upload multiple files
     *
     * @param files The files to upload
     * @param category Category for the uploaded files
     */
    @Throws(ApiException::class)
    public fun uploadMultipleFiles(
        files: List<FileUpload>,
        category: String,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<UploadResponse> {
        val httpUrl: HttpUrl =
            "$baseUrl/files/upload-multiple"
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
                .post(
                    MultipartBody
                        .Builder()
                        .setType(MultipartBody.FORM)
                        .also { builder ->
                            files.forEachIndexed { index, fileUpload ->
                                builder.addFormDataPart(
                                    "files",
                                    fileUpload.filename ?: "files_$index",
                                    fileUpload.content.toRequestBody("application/octet-stream".toMediaType()),
                                )
                            }
                        }.addFormDataPart("category", objectMapper.writeValueAsString(category))
                        .build(),
                ).build()

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }
}
