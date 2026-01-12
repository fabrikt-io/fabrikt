package com.cjbooms.fabrikt.clients

import com.example.multipart.client.FileUpload
import com.example.multipart.client.FilesUploadClient
import com.example.multipart.client.FilesUploadMultipleClient
import com.example.multipart.models.UploadResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aMultipart
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import okhttp3.OkHttpClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.net.ServerSocket

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MultipartFormDataTest {
    private val port: Int = ServerSocket(0).use { socket -> socket.localPort }
    private val wiremock: WireMockServer = WireMockServer(options().port(port).notifier(ConsoleNotifier(true)))
    private val mapper = ObjectMapper().registerKotlinModule()
    private val httpClient = OkHttpClient.Builder().build()

    private val uploadClient = FilesUploadClient(mapper, "http://localhost:$port", httpClient)
    private val uploadMultipleClient = FilesUploadMultipleClient(mapper, "http://localhost:$port", httpClient)

    @BeforeEach
    fun setUp() {
        wiremock.start()
    }

    @AfterEach
    fun afterEach() {
        wiremock.resetAll()
        wiremock.stop()
    }

    @Test
    fun `single file upload sends multipart request correctly`() {
        val expectedResponse = UploadResponse(id = "file-123")

        wiremock.stubFor(
            post(urlEqualTo("/files/upload"))
                .withMultipartRequestBody(
                    aMultipart()
                        .withName("file")
                )
                .willReturn(
                    ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(expectedResponse))
                )
        )

        val fileContent = "Hello, World!".toByteArray()
        val result = uploadClient.uploadFile(
            file = FileUpload(fileContent),
            description = "Test file"
        )

        assertThat(result.statusCode).isEqualTo(200)
        assertThat(result.data).isEqualTo(expectedResponse)

        wiremock.verify(
            postRequestedFor(urlEqualTo("/files/upload"))
                .withHeader("Content-Type", containing("multipart/form-data"))
        )
    }

    @Test
    fun `multiple files upload sends multipart request correctly`() {
        val expectedResponse = UploadResponse(id = "file-multi")

        wiremock.stubFor(
            post(urlEqualTo("/files/upload-multiple"))
                .withMultipartRequestBody(
                    aMultipart()
                        .withName("files")
                )
                .willReturn(
                    ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(expectedResponse))
                )
        )

        val file1Content = "File 1 content".toByteArray()
        val file2Content = "File 2 content".toByteArray()

        val result = uploadMultipleClient.uploadMultipleFiles(
            files = listOf(FileUpload(file1Content), FileUpload(file2Content)),
            category = "documents"
        )

        assertThat(result.statusCode).isEqualTo(200)
        assertThat(result.data).isEqualTo(expectedResponse)

        wiremock.verify(
            postRequestedFor(urlEqualTo("/files/upload-multiple"))
                .withHeader("Content-Type", containing("multipart/form-data"))
        )
    }

    @Test
    fun `single file upload uses custom filename when provided`() {
        val expectedResponse = UploadResponse(id = "file-custom")

        wiremock.stubFor(
            post(urlEqualTo("/files/upload"))
                .withMultipartRequestBody(
                    aMultipart()
                        .withName("file")
                        .withHeader("Content-Disposition", containing("filename=\"my-image.png\""))
                )
                .willReturn(
                    ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(expectedResponse))
                )
        )

        val fileContent = "PNG image bytes".toByteArray()
        val result = uploadClient.uploadFile(
            file = FileUpload(fileContent, "my-image.png"),
            description = "An image file"
        )

        assertThat(result.statusCode).isEqualTo(200)
        assertThat(result.data).isEqualTo(expectedResponse)

        wiremock.verify(
            postRequestedFor(urlEqualTo("/files/upload"))
                .withHeader("Content-Type", containing("multipart/form-data"))
        )
    }

    @Test
    fun `multiple files upload uses custom filenames when provided`() {
        val expectedResponse = UploadResponse(id = "file-multi-custom")

        wiremock.stubFor(
            post(urlEqualTo("/files/upload-multiple"))
                .withMultipartRequestBody(
                    aMultipart()
                        .withName("files")
                        .withHeader("Content-Disposition", containing("filename=\"doc1.pdf\""))
                )
                .withMultipartRequestBody(
                    aMultipart()
                        .withName("files")
                        .withHeader("Content-Disposition", containing("filename=\"doc2.pdf\""))
                )
                .willReturn(
                    ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(expectedResponse))
                )
        )

        val file1Content = "PDF content 1".toByteArray()
        val file2Content = "PDF content 2".toByteArray()

        val result = uploadMultipleClient.uploadMultipleFiles(
            files = listOf(
                FileUpload(file1Content, "doc1.pdf"),
                FileUpload(file2Content, "doc2.pdf")
            ),
            category = "documents"
        )

        assertThat(result.statusCode).isEqualTo(200)
        assertThat(result.data).isEqualTo(expectedResponse)

        wiremock.verify(
            postRequestedFor(urlEqualTo("/files/upload-multiple"))
                .withHeader("Content-Type", containing("multipart/form-data"))
        )
    }

    @Test
    fun `multiple files upload allows partial filename specification`() {
        val expectedResponse = UploadResponse(id = "file-partial")

        wiremock.stubFor(
            post(urlEqualTo("/files/upload-multiple"))
                .withMultipartRequestBody(
                    aMultipart()
                        .withName("files")
                        .withHeader("Content-Disposition", containing("filename=\"custom.txt\""))
                )
                .willReturn(
                    ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(expectedResponse))
                )
        )

        val file1Content = "File 1".toByteArray()
        val file2Content = "File 2".toByteArray()

        // Only provide filename for first file, second uses default
        val result = uploadMultipleClient.uploadMultipleFiles(
            files = listOf(
                FileUpload(file1Content, "custom.txt"),
                FileUpload(file2Content)  // Uses default filename
            ),
            category = "mixed"
        )

        assertThat(result.statusCode).isEqualTo(200)
        assertThat(result.data).isEqualTo(expectedResponse)
    }
}
