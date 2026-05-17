package examples.multipartUpload.controllers

import examples.multipartUpload.models.FileMetadata
import examples.multipartUpload.models.SimpleUploadResult
import examples.multipartUpload.models.UploadResult
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import org.springframework.web.multipart.MultipartFile
import javax.validation.Valid
import kotlin.String
import kotlin.collections.List

@Controller
@Validated
@RequestMapping("")
public interface ApiUploadController {
    /**
     * Upload a file with metadata
     *
     * @param file The file to upload
     * @param metadata
     * @param tags Optional tags for the file
     */
    @RequestMapping(
        value = ["/api/upload"],
        produces = ["application/json"],
        method = [RequestMethod.POST],
        consumes = ["multipart/form-data"],
    )
    public fun uploadFile(
        @RequestBody @Valid `file`: MultipartFile,
        @RequestBody @Valid metadata: FileMetadata,
        @RequestBody @Valid tags: List<String>?,
    ): ResponseEntity<UploadResult>
}

@Controller
@Validated
@RequestMapping("")
public interface ApiUploadSimpleController {
    /**
     * Upload a single file without metadata
     *
     * @param file The file to upload
     */
    @RequestMapping(
        value = ["/api/upload-simple"],
        produces = ["application/json"],
        method = [RequestMethod.POST],
        consumes = ["multipart/form-data"],
    )
    public fun uploadSingleFile(
        @RequestBody @Valid `file`: MultipartFile,
    ): ResponseEntity<SimpleUploadResult>
}

@Controller
@Validated
@RequestMapping("")
public interface ApiUploadMultipleController {
    /**
     * Upload multiple files with shared metadata
     *
     * @param files Multiple files to upload
     * @param commonMetadata
     * @param description Description for all files
     */
    @RequestMapping(
        value = ["/api/upload-multiple"],
        produces = ["application/json"],
        method = [RequestMethod.POST],
        consumes = ["multipart/form-data"],
    )
    public fun uploadMultipleFiles(
        @RequestBody @Valid files: List<MultipartFile>,
        @RequestBody @Valid commonMetadata: FileMetadata,
        @RequestBody @Valid description: String?,
    ): ResponseEntity<List<UploadResult>>
}
