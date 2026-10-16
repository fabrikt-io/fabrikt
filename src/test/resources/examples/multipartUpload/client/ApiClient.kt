package examples.multipartUpload.client

import examples.multipartUpload.models.FileMetadata
import examples.multipartUpload.models.JsonPayload
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
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.jacksonTypeRef
import kotlin.Double
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.jvm.Throws

@Suppress("unused")
public class JsonClient(
    private val objectMapper: JsonMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     *
     *
     * @param request
     * @param details
     * @param optionalRequest
     * @param template
     */
    @Throws(ApiException::class)
    public fun uploadJson(
        request: JsonPayload,
        details: JsonPayload?,
        optionalRequest: JsonPayload?,
        template: RequestBodyWithFilename,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/json"
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
        multipartBuilder.addFormDataPart(
            "request",
            null,
            objectMapper.writeValueAsString(request).toRequestBody("application/json".toMediaType()),
        )
        details?.let {
            multipartBuilder.addFormDataPart(
                "details",
                null,
                objectMapper.writeValueAsString(details).toRequestBody("application/json".toMediaType()),
            )
        }
        optionalRequest?.let {
            multipartBuilder.addFormDataPart(
                "optionalRequest",
                null,
                objectMapper.writeValueAsString(optionalRequest).toRequestBody("application/json".toMediaType()),
            )
        }
        multipartBuilder.addFormDataPart("template", template.filename, template.requestBody)
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
public class ApiUploadClient(
    private val objectMapper: JsonMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     * Upload a file with metadata
     *
     * @param file The file to upload
     * @param metadata
     * @param tags Optional tags for the file
     * @param version
     */
    @Throws(ApiException::class)
    public fun uploadFile(
        `file`: RequestBodyWithFilename,
        metadata: FileMetadata,
        tags: List<String>?,
        version: Double?,
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
        multipartBuilder.addFormDataPart("file", `file`.filename, `file`.requestBody)
        multipartBuilder.addFormDataPart(
            "metadata",
            null,
            objectMapper.writeValueAsString(metadata).toRequestBody("application/json".toMediaType()),
        )
        tags?.let {
            multipartBuilder.addFormDataPart(
                "tags",
                null,
                objectMapper.writeValueAsString(tags).toRequestBody("application/json".toMediaType()),
            )
        }
        version?.let {
            multipartBuilder.addFormDataPart("version", version.toString())
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
public class ApiUploadSimpleClient(
    private val objectMapper: JsonMapper,
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
        `file`: RequestBodyWithFilename,
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
        multipartBuilder.addFormDataPart("file", `file`.filename, `file`.requestBody)
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
    private val objectMapper: JsonMapper,
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
        files: List<RequestBodyWithFilename>,
        commonMetadata: FileMetadata,
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
            multipartBuilder.addFormDataPart("files", fileData.filename, fileData.requestBody)
        }
        multipartBuilder.addFormDataPart(
            "commonMetadata",
            null,
            objectMapper.writeValueAsString(commonMetadata).toRequestBody("application/json".toMediaType()),
        )
        description?.let {
            multipartBuilder.addFormDataPart("description", description.toString())
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
