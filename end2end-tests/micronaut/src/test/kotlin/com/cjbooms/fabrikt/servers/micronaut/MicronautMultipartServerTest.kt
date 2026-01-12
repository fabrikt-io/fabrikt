package com.cjbooms.fabrikt.servers.micronaut

import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.multipart.MultipartBody
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@MicronautTest
class MicronautMultipartServerTest {

    @Inject
    @field:Client("/")
    lateinit var client: HttpClient

    @Inject
    lateinit var fileCapture: FileCapture

    @BeforeEach
    fun setUp() {
        fileCapture.reset()
    }

    @Test
    fun `single file upload is parsed correctly with file metadata`() {
        val fileContent = "Hello, World!".toByteArray()

        val body = MultipartBody.builder()
            .addPart("file", "test.txt", MediaType.TEXT_PLAIN_TYPE, fileContent)
            .addPart("description", "Test description")
            .build()

        val request = HttpRequest.POST("/files/upload", body)
            .contentType(MediaType.MULTIPART_FORM_DATA_TYPE)

        val response = client.toBlocking().retrieve(request, String::class.java)

        assertThat(response).contains("file-123")
        assertThat(fileCapture.capturedFile).isNotNull
        assertThat(fileCapture.capturedFile!!.bytes).isEqualTo(fileContent)
        assertThat(fileCapture.capturedFile!!.filename).isEqualTo("test.txt")
        assertThat(fileCapture.capturedFile!!.contentType.get().toString()).isEqualTo(MediaType.TEXT_PLAIN)
        assertThat(fileCapture.capturedDescription).isEqualTo("Test description")
    }

    @Test
    fun `single file upload works without optional description`() {
        val fileContent = "Content without description".toByteArray()

        val body = MultipartBody.builder()
            .addPart("file", "nodesc.txt", MediaType.TEXT_PLAIN_TYPE, fileContent)
            .build()

        val request = HttpRequest.POST("/files/upload", body)
            .contentType(MediaType.MULTIPART_FORM_DATA_TYPE)

        val response = client.toBlocking().retrieve(request, String::class.java)

        assertThat(response).contains("file-123")
        assertThat(fileCapture.capturedFile).isNotNull
        assertThat(fileCapture.capturedFile!!.bytes).isEqualTo(fileContent)
        assertThat(fileCapture.capturedDescription).isNull()
    }

    @Test
    @Disabled("Micronaut requires reactive types (Publisher/Flux) for multi-file uploads with same part name")
    fun `multiple files upload is parsed correctly with file metadata`() {
        val file1Content = "File 1 content".toByteArray()
        val file2Content = "File 2 content".toByteArray()

        val body = MultipartBody.builder()
            .addPart("files", "file1.txt", MediaType.TEXT_PLAIN_TYPE, file1Content)
            .addPart("files", "file2.pdf", MediaType.APPLICATION_PDF_TYPE, file2Content)
            .addPart("category", "documents")
            .build()

        val request = HttpRequest.POST("/files/upload-multiple", body)
            .contentType(MediaType.MULTIPART_FORM_DATA_TYPE)

        val response = client.toBlocking().retrieve(request, String::class.java)

        assertThat(response).contains("file-multi")
        assertThat(fileCapture.capturedFiles).hasSize(2)
        assertThat(fileCapture.capturedFiles!![0].bytes).isEqualTo(file1Content)
        assertThat(fileCapture.capturedFiles!![0].filename).isEqualTo("file1.txt")
        assertThat(fileCapture.capturedFiles!![0].contentType.get().toString()).isEqualTo(MediaType.TEXT_PLAIN)
        assertThat(fileCapture.capturedFiles!![1].bytes).isEqualTo(file2Content)
        assertThat(fileCapture.capturedFiles!![1].filename).isEqualTo("file2.pdf")
        assertThat(fileCapture.capturedFiles!![1].contentType.get().toString()).isEqualTo(MediaType.APPLICATION_PDF)
        assertThat(fileCapture.capturedCategory).isEqualTo("documents")
    }
}
