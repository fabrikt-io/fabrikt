package com.cjbooms.fabrikt.clients

import com.example.multipart.client.FileUpload
import com.example.multipart.client.FilesUploadClient
import com.example.multipart.client.FilesUploadMultipleClient
import com.example.multipart.client.NetworkResult
import com.example.multipart.models.UploadResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.jackson.jackson
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertInstanceOf
import java.net.ServerSocket

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KtorClientMultipartTest {
    private val port: Int = ServerSocket(0).use { socket -> socket.localPort }
    private val wiremock: WireMockServer = WireMockServer(options().port(port))
    private val mapper = ObjectMapper().registerKotlinModule()

    private fun createHttpClient() = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson()
        }
        defaultRequest {
            url(wiremock.baseUrl())
        }
    }

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
    fun `single file upload sends multipart request correctly`() = runBlocking {
        val expectedResponse = UploadResponse(id = "file-123")

        wiremock.stubFor(
            post(urlEqualTo("/files/upload"))
                .willReturn(
                    ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(expectedResponse))
                )
        )

        val client = FilesUploadClient(createHttpClient())
        val fileContent = "Hello, World!".toByteArray()
        val result = client.uploadFile(
            file = FileUpload(fileContent),
            description = "Test file"
        )

        assertInstanceOf<NetworkResult.Success<UploadResponse>>(result)

        wiremock.verify(
            postRequestedFor(urlEqualTo("/files/upload"))
                .withHeader("Content-Type", containing("multipart/form-data"))
        )
    }

    @Test
    fun `multiple files upload sends multipart request correctly`() = runBlocking {
        val expectedResponse = UploadResponse(id = "file-multi")

        wiremock.stubFor(
            post(urlEqualTo("/files/upload-multiple"))
                .willReturn(
                    ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(expectedResponse))
                )
        )

        val client = FilesUploadMultipleClient(createHttpClient())
        val file1Content = "File 1 content".toByteArray()
        val file2Content = "File 2 content".toByteArray()

        val result = client.uploadMultipleFiles(
            files = listOf(FileUpload(file1Content), FileUpload(file2Content)),
            category = "documents"
        )

        assertInstanceOf<NetworkResult.Success<UploadResponse>>(result)

        wiremock.verify(
            postRequestedFor(urlEqualTo("/files/upload-multiple"))
                .withHeader("Content-Type", containing("multipart/form-data"))
        )
    }

    @Test
    fun `single file upload uses custom filename when provided`() = runBlocking {
        val expectedResponse = UploadResponse(id = "file-custom")

        wiremock.stubFor(
            post(urlEqualTo("/files/upload"))
                .willReturn(
                    ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(expectedResponse))
                )
        )

        val client = FilesUploadClient(createHttpClient())
        val fileContent = "PNG image bytes".toByteArray()
        val result = client.uploadFile(
            file = FileUpload(fileContent, "my-image.png"),
            description = "An image file"
        )

        assertInstanceOf<NetworkResult.Success<UploadResponse>>(result)

        wiremock.verify(
            postRequestedFor(urlEqualTo("/files/upload"))
                .withHeader("Content-Type", containing("multipart/form-data"))
                .withRequestBody(containing("""filename="my-image.png""""))
        )
    }

    @Test
    fun `multiple files upload uses custom filenames when provided`() = runBlocking {
        val expectedResponse = UploadResponse(id = "file-multi-custom")

        wiremock.stubFor(
            post(urlEqualTo("/files/upload-multiple"))
                .willReturn(
                    ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(expectedResponse))
                )
        )

        val client = FilesUploadMultipleClient(createHttpClient())
        val file1Content = "PDF content 1".toByteArray()
        val file2Content = "PDF content 2".toByteArray()

        val result = client.uploadMultipleFiles(
            files = listOf(
                FileUpload(file1Content, "doc1.pdf"),
                FileUpload(file2Content, "doc2.pdf")
            ),
            category = "documents"
        )

        assertInstanceOf<NetworkResult.Success<UploadResponse>>(result)

        wiremock.verify(
            postRequestedFor(urlEqualTo("/files/upload-multiple"))
                .withHeader("Content-Type", containing("multipart/form-data"))
                .withRequestBody(containing("""filename="doc1.pdf""""))
                .withRequestBody(containing("""filename="doc2.pdf""""))
        )
    }

    @Test
    fun `multiple files upload allows partial filename specification`() = runBlocking {
        val expectedResponse = UploadResponse(id = "file-partial")

        wiremock.stubFor(
            post(urlEqualTo("/files/upload-multiple"))
                .willReturn(
                    ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(expectedResponse))
                )
        )

        val client = FilesUploadMultipleClient(createHttpClient())
        val file1Content = "File 1".toByteArray()
        val file2Content = "File 2".toByteArray()

        // Only provide filename for first file, second uses default
        val result = client.uploadMultipleFiles(
            files = listOf(
                FileUpload(file1Content, "custom.txt"),
                FileUpload(file2Content)  // Uses default filename
            ),
            category = "mixed"
        )

        assertInstanceOf<NetworkResult.Success<UploadResponse>>(result)

        wiremock.verify(
            postRequestedFor(urlEqualTo("/files/upload-multiple"))
                .withHeader("Content-Type", containing("multipart/form-data"))
                .withRequestBody(containing("""filename="custom.txt""""))
        )
    }
}
