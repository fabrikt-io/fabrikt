package examples.multipartUpload.controllers

import examples.multipartUpload.models.FileMetadata
import examples.multipartUpload.models.SimpleUploadResult
import examples.multipartUpload.models.UploadResult
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import org.springframework.web.bind.`annotation`.RequestParam
import org.springframework.web.bind.`annotation`.RequestPart
import org.springframework.web.multipart.MultipartFile
import javax.validation.Valid
import kotlin.Double
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
     * @param version
     */
    @RequestMapping(
        value = ["/api/upload"],
        produces = ["application/json"],
        method = [RequestMethod.POST],
        consumes = ["multipart/form-data"],
    )
    public fun uploadFile(
        @RequestPart(value = "file", required = true) @Valid `file`: MultipartFile,
        @RequestPart(value = "metadata", required = true) @Valid metadata: FileMetadata,
        @RequestPart(value = "tags", required = false) @Valid tags: List<String>?,
        @RequestParam(value = "version", required = false) @Valid version: Double?,
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
        @RequestPart(value = "file", required = true) @Valid
        `file`: MultipartFile,
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
        @RequestPart(value = "files", required = true) @Valid files: List<MultipartFile>,
        @RequestPart(value = "commonMetadata", required = true) @Valid commonMetadata: FileMetadata,
        @RequestParam(value = "description", required = false) @Valid description: String?,
    ): ResponseEntity<List<UploadResult>>
}
