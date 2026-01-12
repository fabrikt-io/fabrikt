package examples.multipartFormData.controllers

import examples.multipartFormData.models.UploadResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.`annotation`.Consumes
import io.micronaut.http.`annotation`.Controller
import io.micronaut.http.`annotation`.Part
import io.micronaut.http.`annotation`.Post
import io.micronaut.http.`annotation`.Produces
import io.micronaut.http.multipart.CompletedFileUpload
import io.micronaut.security.rules.SecurityRule
import kotlin.String
import kotlin.collections.List

@Controller
public interface FilesUploadController {
    /**
     * Upload a single file
     *
     * @param file The file to upload
     * @param description Optional description of the file
     */
    @Post(uri = "/files/upload")
    @Consumes(value = ["multipart/form-data"])
    @Produces(value = ["application/json"])
    public fun uploadFile(
        @Part(value = "file") `file`: CompletedFileUpload,
        @Part(
            value =
                "description",
        ) description: String?,
    ): HttpResponse<UploadResponse>
}

@Controller
public interface FilesUploadMultipleController {
    /**
     * Upload multiple files
     *
     * @param files The files to upload
     * @param category Category for the uploaded files
     */
    @Post(uri = "/files/upload-multiple")
    @Consumes(value = ["multipart/form-data"])
    @Produces(value = ["application/json"])
    public fun uploadMultipleFiles(
        @Part(value = "files") files: List<CompletedFileUpload>,
        @Part(value = "category") category: String,
    ): HttpResponse<UploadResponse>
}