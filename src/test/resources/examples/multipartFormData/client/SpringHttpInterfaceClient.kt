package examples.multipartFormData.client

import examples.multipartFormData.models.UploadResponse
import org.springframework.web.bind.`annotation`.RequestHeader
import org.springframework.web.bind.`annotation`.RequestParam
import org.springframework.web.bind.`annotation`.RequestPart
import org.springframework.web.service.`annotation`.HttpExchange
import kotlin.Any
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
    @HttpExchange(
        url = "/files/upload",
        method = "POST",
        accept = ["application/json"],
    )
    public fun uploadFile(
        @RequestPart(value = "file") `file`: ByteArray,
        @RequestPart(value = "description") description: String? = null,
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
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
    @HttpExchange(
        url = "/files/upload-multiple",
        method = "POST",
        accept = ["application/json"],
    )
    public fun uploadMultipleFiles(
        @RequestPart(value = "files") files: List<ByteArray>,
        @RequestPart(value = "category") category: String,
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    ): UploadResponse
}
