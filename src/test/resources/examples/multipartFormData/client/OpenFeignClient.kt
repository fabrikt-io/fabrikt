package examples.multipartFormData.client

import examples.multipartFormData.models.UploadResponse
import feign.HeaderMap
import feign.Headers
import feign.QueryMap
import feign.RequestLine
import org.springframework.web.bind.`annotation`.RequestPart
import kotlin.ByteArray
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map

@Suppress("unused")
public interface FilesUploadClient {
    /**
     * Upload a single file
     *
     * @param file The file to upload
     * @param description Optional description of the file
     */
    @RequestLine("POST /files/upload")
    @Headers("Accept: application/json")
    public fun uploadFile(
        @RequestPart(value = "file") `file`: ByteArray,
        @RequestPart(value = "description") description: String? = null,
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    ): UploadResponse
}

@Suppress("unused")
public interface FilesUploadMultipleClient {
    /**
     * Upload multiple files
     *
     * @param files The files to upload
     * @param category Category for the uploaded files
     */
    @RequestLine("POST /files/upload-multiple")
    @Headers("Accept: application/json")
    public fun uploadMultipleFiles(
        @RequestPart(value = "files") files: List<ByteArray>,
        @RequestPart(value = "category") category: String,
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    ): UploadResponse
}
