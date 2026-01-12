package com.cjbooms.fabrikt.servers.micronaut

import com.example.multipart.controllers.FilesUploadController
import com.example.multipart.controllers.FilesUploadMultipleController
import com.example.multipart.models.UploadResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.multipart.CompletedFileUpload
import jakarta.inject.Singleton

@Controller
class FilesUploadControllerImpl(
    private val fileCapture: FileCapture
) : FilesUploadController {
    override fun uploadFile(file: CompletedFileUpload, description: String?): HttpResponse<UploadResponse> {
        fileCapture.capturedFile = file
        fileCapture.capturedDescription = description
        return HttpResponse.ok(UploadResponse(id = "file-123"))
    }
}

@Controller
class FilesUploadMultipleControllerImpl(
    private val fileCapture: FileCapture
) : FilesUploadMultipleController {
    override fun uploadMultipleFiles(files: List<CompletedFileUpload>, category: String): HttpResponse<UploadResponse> {
        fileCapture.capturedFiles = files
        fileCapture.capturedCategory = category
        return HttpResponse.ok(UploadResponse(id = "file-multi"))
    }
}

@Singleton
class FileCapture {
    var capturedFile: CompletedFileUpload? = null
    var capturedFiles: List<CompletedFileUpload>? = null
    var capturedDescription: String? = null
    var capturedCategory: String? = null

    fun reset() {
        capturedFile = null
        capturedFiles = null
        capturedDescription = null
        capturedCategory = null
    }
}
