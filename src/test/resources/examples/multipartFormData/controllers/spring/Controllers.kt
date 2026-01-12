package examples.multipartFormData.controllers

import examples.multipartFormData.models.UploadResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import org.springframework.web.bind.`annotation`.RequestPart
import org.springframework.web.multipart.MultipartFile
import kotlin.String
import kotlin.collections.List

@Controller
@Validated
@RequestMapping("")
public interface FilesUploadController {
    /**
     * Upload a single file
     *
     * @param file The file to upload
     * @param description Optional description of the file
     */
    @RequestMapping(
        value = ["/files/upload"],
        produces = ["application/json"],
        method = [RequestMethod.POST],
        consumes = ["multipart/form-data"],
    )
    public fun uploadFile(
        @RequestPart(value = "file", required = true) `file`: MultipartFile,
        @RequestPart(value = "description", required = false) description: String?,
    ): ResponseEntity<UploadResponse>
}

@Controller
@Validated
@RequestMapping("")
public interface FilesUploadMultipleController {
    /**
     * Upload multiple files
     *
     * @param files The files to upload
     * @param category Category for the uploaded files
     */
    @RequestMapping(
        value = ["/files/upload-multiple"],
        produces = ["application/json"],
        method = [RequestMethod.POST],
        consumes = ["multipart/form-data"],
    )
    public fun uploadMultipleFiles(
        @RequestPart(value = "files", required = true)
        files: List<MultipartFile>,
        @RequestPart(value = "category", required = true)
        category: String,
    ): ResponseEntity<UploadResponse>
}
