package com.cjbooms.fabrikt.server.spring

import com.example.controllers.ApiUploadSimpleController
import com.example.models.SimpleUploadResult
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.multipart.MultipartFile
import java.time.OffsetDateTime

@Controller
class ApiUploadSimpleControllerImpl : ApiUploadSimpleController {
    override fun uploadSingleFile(file: MultipartFile): ResponseEntity<SimpleUploadResult> {
        require(file.contentType == "image/jpeg") { "wrong file contentType" }
        require(file.originalFilename == "image.jpg") { "wrong file name" }
        require(file.bytes.contentEquals("bar".toByteArray())) { "wrong file content" }

        return ResponseEntity.ok(
            SimpleUploadResult(
                id = "id",
                uploadedAt = OffsetDateTime.now(),
                filename = file.originalFilename ?: "",
                size = file.size,
            )
        )
    }
}