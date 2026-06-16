package com.cjbooms.fabrikt.server.spring

import com.example.controllers.ApiUploadMultipleController
import com.example.models.FileMetadata
import com.example.models.FileMetadataCategory
import com.example.models.UploadResult
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.multipart.MultipartFile
import java.time.OffsetDateTime

@Controller
class ApiUploadMultipartControllerImpl : ApiUploadMultipleController {
    override fun uploadMultipleFiles(
        files: List<MultipartFile>,
        commonMetadata: FileMetadata,
        description: String?
    ): ResponseEntity<List<UploadResult>> {
        files.forEachIndexed { index, file ->
            require(file.contentType == "image/jpeg") { "wrong file contentType" }
            require(file.originalFilename == "image${index + 1}.jpg") { "wrong file name" }
            require(file.bytes.contentEquals("bar".toByteArray())) { "wrong file content" }
        }

        require(
            commonMetadata == FileMetadata(
                name = "name",
                category = FileMetadataCategory.OTHER,
                contentType = "image/jpeg",
                size = 123,
            )
        ) { "metadata is invalid" }

        require(description == "my description") { "wrong description" }

        return ResponseEntity.ok(
            files.map { file ->
                UploadResult(
                    id = "id",
                    originalName = file.originalFilename ?: "",
                    storedName = file.name,
                    url = "url",
                    uploadedAt = OffsetDateTime.now(),
                    size = file.size,
                )
            })
    }
}