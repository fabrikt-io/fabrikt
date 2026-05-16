package examples.multipartUpload.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import examples.multipartUpload.models.FileMetadata
import examples.multipartUpload.models.SimpleUploadResult
import examples.multipartUpload.models.UploadResult
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.MultipartBody.Builder
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
public class ApiUploadClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     * Upload a file with metadata
     *
     * @param file The file to upload
     * @param metadata
     * @param tags Optional tags for the file
     */
    @Throws(ApiException::class)
    public fun uploadFile(
        `file`: ByteArray?,
        metadata: FileMetadata?,
        tags: List<String>?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<UploadResult> {
        val httpUrl: HttpUrl =
            "$baseUrl/api/upload"
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
        `file`?.let {
            multipartBuilder.addFormDataPart(
                "file",
                "file",
                it.toRequestBody("application/octet-stream".toMediaType()),
            )
        }
        multipartBuilder.addFormDataPart("metadata", objectMapper.writeValueAsString(metadata))
        multipartBuilder.addFormDataPart("tags", objectMapper.writeValueAsString(tags))
        val multipartBody = multipartBuilder.build()
        val request: Request =
            Request
                .Builder()
                .url(httpUrl)
                .headers(httpHeaders)
                .post(multipartBody)
                .build()

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }
}

@Suppress("unused")
public class ApiUploadSimpleClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     * Upload a single file without metadata
     *
     * @param file The file to upload
     */
    @Throws(ApiException::class)
    public fun uploadSingleFile(
        `file`: ByteArray?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<SimpleUploadResult> {
        val httpUrl: HttpUrl =
            "$baseUrl/api/upload-simple"
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
        `file`?.let {
            multipartBuilder.addFormDataPart(
                "file",
                "file",
                it.toRequestBody("application/octet-stream".toMediaType()),
            )
        }
        val multipartBody = multipartBuilder.build()
        val request: Request =
            Request
                .Builder()
                .url(httpUrl)
                .headers(httpHeaders)
                .post(multipartBody)
                .build()

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }
}

@Suppress("unused")
public class ApiUploadMultipleClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     * Upload multiple files with shared metadata
     *
     * @param files Multiple files to upload
     * @param commonMetadata
     * @param description Description for all files
     */
    @Throws(ApiException::class)
    public fun uploadMultipleFiles(
        files: List<ByteArray>?,
        commonMetadata: FileMetadata?,
        description: String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<List<UploadResult>> {
        val httpUrl: HttpUrl =
            "$baseUrl/api/upload-multiple"
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
        files?.forEachIndexed { index, fileData ->
            multipartBuilder.addFormDataPart(
                "files",
                "file$index",
                fileData.toRequestBody("application/octet-stream".toMediaType()),
            )
        }
        multipartBuilder.addFormDataPart(
            "commonMetadata",
            objectMapper.writeValueAsString(commonMetadata),
        )
        multipartBuilder.addFormDataPart("description", description.toString())
        val multipartBody = multipartBuilder.build()
        val request: Request =
            Request
                .Builder()
                .url(httpUrl)
                .headers(httpHeaders)
                .post(multipartBody)
                .build()

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }
}
