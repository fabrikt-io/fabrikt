package com.cjbooms.fabrikt.server.spring

import com.example.controllers.ApiUploadController
import com.example.models.FileMetadata
import com.example.models.FileMetadataCategory
import com.example.models.UploadResult
import org.springframework.http.ResponseEntity
import org.springframework.web.multipart.MultipartFile
import java.time.OffsetDateTime

class ApiUploadControllerImpl : ApiUploadController {
    override fun uploadFile(
        file: MultipartFile,
        metadata: FileMetadata,
        tags: List<String>?,
        version: Double?,
    ): ResponseEntity<UploadResult> {
        require(file.contentType == "image/jpeg") { "wrong file contentType" }
        require(file.originalFilename == "image.jpg") { "wrong file name" }
        require(file.bytes.contentEquals("bar".toByteArray())) { "wrong file content" }

        require(
            metadata == FileMetadata(
                name = "name",
                category = FileMetadataCategory.OTHER,
                contentType = "image/jpeg",
                size = 123,
            )
        ) { "metadata is invalid" }

        requireNotNull(tags)
        require(tags == listOf("foo", "bar")) { "tags invalid" }

        require(version == 1.3) { "version invalid" }

        return ResponseEntity.ok(
            UploadResult(
                id = "id",
                originalName = file.originalFilename ?: "",
                storedName = file.name,
                url = "url",
                uploadedAt = OffsetDateTime.now(),
                size = file.size,
            )
        )
    }
}