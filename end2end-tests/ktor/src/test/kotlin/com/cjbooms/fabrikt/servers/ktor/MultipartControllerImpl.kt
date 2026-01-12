package com.cjbooms.fabrikt.servers.ktor

import com.example.multipart.controllers.FilesUploadController
import com.example.multipart.controllers.FilesUploadMultipleController
import com.example.multipart.controllers.ReceivedFile
import com.example.multipart.controllers.TypedApplicationCall
import com.example.multipart.models.UploadResponse
import io.mockk.CapturingSlot

class FilesUploadControllerImpl(
    private val fileSlot: CapturingSlot<ReceivedFile>,
    private val descriptionSlot: CapturingSlot<String?>
) : FilesUploadController {
    override suspend fun uploadFile(file: ReceivedFile, description: String?, call: TypedApplicationCall<UploadResponse>) {
        fileSlot.captured = file
        descriptionSlot.captured = description
        call.respondTyped(
            UploadResponse(id = "file-123")
        )
    }
}

class FilesUploadMultipleControllerImpl(
    private val filesSlot: CapturingSlot<List<ReceivedFile>>,
    private val categorySlot: CapturingSlot<String>
) : FilesUploadMultipleController {
    override suspend fun uploadMultipleFiles(files: List<ReceivedFile>, category: String, call: TypedApplicationCall<UploadResponse>) {
        filesSlot.captured = files
        categorySlot.captured = category
        call.respondTyped(
            UploadResponse(id = "file-multi")
        )
    }
}
