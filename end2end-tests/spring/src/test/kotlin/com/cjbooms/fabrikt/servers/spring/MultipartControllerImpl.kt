package com.cjbooms.fabrikt.servers.spring

import com.example.multipart.controllers.FilesUploadController
import com.example.multipart.controllers.FilesUploadMultipleController
import com.example.multipart.models.UploadResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class FilesUploadControllerImpl(
    private val fileCapture: FileCapture
) : FilesUploadController {
    override fun uploadFile(file: MultipartFile, description: String?): ResponseEntity<UploadResponse> {
        fileCapture.capturedFile = file
        fileCapture.capturedDescription = description
        return ResponseEntity.ok(UploadResponse(id = "file-123"))
    }
}

@RestController
class FilesUploadMultipleControllerImpl(
    private val fileCapture: FileCapture
) : FilesUploadMultipleController {
    override fun uploadMultipleFiles(files: List<MultipartFile>, category: String): ResponseEntity<UploadResponse> {
        fileCapture.capturedFiles = files
        fileCapture.capturedCategory = category
        return ResponseEntity.ok(UploadResponse(id = "file-multi"))
    }
}

class FileCapture {
    var capturedFile: MultipartFile? = null
    var capturedFiles: List<MultipartFile>? = null
    var capturedDescription: String? = null
    var capturedCategory: String? = null

    fun reset() {
        capturedFile = null
        capturedFiles = null
        capturedDescription = null
        capturedCategory = null
    }
}
