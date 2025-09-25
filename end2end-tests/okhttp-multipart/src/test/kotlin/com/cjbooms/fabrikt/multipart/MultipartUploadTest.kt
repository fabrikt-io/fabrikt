package com.cjbooms.fabrikt.multipart

import com.example.multipart.client.ApiUploadClient
import com.example.multipart.client.ApiUploadMultipleClient
import com.example.multipart.models.FileMetadata
import com.example.multipart.models.FileMetadataCategory
import com.example.multipart.models.UploadResult
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.marcinziolo.kotlin.wiremock.contains
import com.marcinziolo.kotlin.wiremock.like
import com.marcinziolo.kotlin.wiremock.post
import com.marcinziolo.kotlin.wiremock.returns
import okhttp3.OkHttpClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.net.ServerSocket
import java.time.OffsetDateTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MultipartUploadTest {
    private val port: Int = ServerSocket(0).use { socket -> socket.localPort }
    private val wiremock: WireMockServer = WireMockServer(options().port(port).notifier(ConsoleNotifier(true)))
    private val mapper = ObjectMapper().registerModule(JavaTimeModule())
    private val httpClient = OkHttpClient.Builder().build()
    private val uploadClient = ApiUploadClient(mapper, "http://localhost:$port", httpClient)
    private val uploadMultipleClient = ApiUploadMultipleClient(mapper, "http://localhost:$port", httpClient)

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
    fun `single file upload with multipart form data works correctly`() {
        // Arrange
        val now = OffsetDateTime.now()
        val expectedResponse = UploadResult(
            id = "file-123",
            originalName = "test.txt",
            storedName = "stored-test.txt",
            url = "http://example.com/files/stored-test.txt",
            uploadedAt = now,
            size = 1024L
        )

        wiremock.post {
            urlPath like "/api/upload"
            headers contains "content-type" like "multipart/form-data.*"
        } returns {
            statusCode = 200
            header = "Content-Type" to "application/json"
            body = mapper.writeValueAsString(expectedResponse)
        }

        val fileContent = "This is test file content".toByteArray()
        val metadata = FileMetadata(
            name = "test.txt",
            category = FileMetadataCategory.DOCUMENT,
            contentType = "text/plain",
            size = fileContent.size.toLong()
        )

        // Act
        val result = uploadClient.uploadFile(
            file = fileContent,
            metadata = metadata,
            tags = listOf("test", "example")
        )

        // Assert
        assertThat(result.data).isNotNull
        assertThat(result.data?.id).isEqualTo("file-123")
        assertThat(result.data?.originalName).isEqualTo("test.txt")
        assertThat(result.data?.storedName).isEqualTo("stored-test.txt")
        assertThat(result.data?.url).isEqualTo("http://example.com/files/stored-test.txt")
        assertThat(result.data?.size).isEqualTo(1024L)
        assertThat(result.data?.uploadedAt).isNotNull

        // Verify that the request was made with multipart content
        wiremock.allServeEvents.forEach { event ->
            val request = event.request
            assertThat(request.getHeader("Content-Type")).startsWith("multipart/form-data")
            assertThat(request.bodyAsString).contains("Content-Disposition: form-data; name=\"file\"")
            assertThat(request.bodyAsString).contains("Content-Disposition: form-data; name=\"metadata\"")
            assertThat(request.bodyAsString).contains("Content-Disposition: form-data; name=\"tags\"")
        }
    }

    @Test
    fun `multiple files upload with shared metadata works correctly`() {
        // Arrange
        val now = OffsetDateTime.now()
        val expectedResponse = listOf(
            UploadResult(
                id = "file-1",
                originalName = "file1.txt",
                storedName = "stored-file1.txt",
                url = "http://example.com/files/stored-file1.txt",
                uploadedAt = now,
                size = 512L
            ),
            UploadResult(
                id = "file-2",
                originalName = "file2.txt",
                storedName = "stored-file2.txt",
                url = "http://example.com/files/stored-file2.txt",
                uploadedAt = now,
                size = 1024L
            )
        )

        wiremock.post {
            urlPath like "/api/upload-multiple"
            headers contains "content-type" like "multipart/form-data.*"
        } returns {
            statusCode = 200
            header = "Content-Type" to "application/json"
            body = mapper.writeValueAsString(expectedResponse)
        }

        val files = listOf(
            "Content of file 1".toByteArray(),
            "Content of file 2 is longer".toByteArray()
        )
        val commonMetadata = FileMetadata(
            name = "batch-upload",
            category = FileMetadataCategory.DOCUMENT,
            contentType = "text/plain",
            size = 100L
        )

        // Act
        val result = uploadMultipleClient.uploadMultipleFiles(
            files = files,
            commonMetadata = commonMetadata,
            description = "Batch upload test"
        )

        // Assert
        assertThat(result.data).isNotNull
        assertThat(result.data).hasSize(2)
        assertThat(result.data?.get(0)?.id).isEqualTo("file-1")
        assertThat(result.data?.get(0)?.originalName).isEqualTo("file1.txt")
        assertThat(result.data?.get(0)?.size).isEqualTo(512L)
        assertThat(result.data?.get(1)?.id).isEqualTo("file-2")
        assertThat(result.data?.get(1)?.originalName).isEqualTo("file2.txt")
        assertThat(result.data?.get(1)?.size).isEqualTo(1024L)

        // Verify that the request was made with multipart content
        wiremock.allServeEvents.forEach { event ->
            val request = event.request
            assertThat(request.getHeader("Content-Type")).startsWith("multipart/form-data")
            assertThat(request.bodyAsString).contains("Content-Disposition: form-data; name=\"files\"")
            assertThat(request.bodyAsString).contains("Content-Disposition: form-data; name=\"commonMetadata\"")
            assertThat(request.bodyAsString).contains("Content-Disposition: form-data; name=\"description\"")
        }
    }

    @Test
    fun `multipart request contains proper boundary and content disposition headers`() {
        // Arrange
        val now = OffsetDateTime.now()
        val expectedResponse = UploadResult(
            id = "boundary-test",
            originalName = "boundary.txt",
            storedName = "stored-boundary.txt",
            url = "http://example.com/files/stored-boundary.txt",
            uploadedAt = now,
            size = 20L
        )

        wiremock.post {
            urlPath like "/api/upload"
        } returns {
            statusCode = 200
            header = "Content-Type" to "application/json"
            body = mapper.writeValueAsString(expectedResponse)
        }

        val fileContent = "boundary test content".toByteArray()
        val metadata = FileMetadata(
            name = "boundary.txt",
            category = FileMetadataCategory.OTHER
        )

        // Act
        val result = uploadClient.uploadFile(
            file = fileContent,
            metadata = metadata,
            tags = emptyList()
        )

        // Assert
        assertThat(result.data).isNotNull
        assertThat(result.data?.id).isEqualTo("boundary-test")
        assertThat(result.data?.originalName).isEqualTo("boundary.txt")
        assertThat(result.data?.size).isEqualTo(20L)

        // Verify multipart structure in detail
        val request = wiremock.allServeEvents.first().request
        val contentType = request.getHeader("Content-Type")
        val body = request.bodyAsString

        // Verify Content-Type header has boundary
        assertThat(contentType).startsWith("multipart/form-data")
        assertThat(contentType).contains("boundary=")

        // Verify multipart structure
        assertThat(body).contains("Content-Disposition: form-data; name=\"file\"")
        assertThat(body).contains("Content-Disposition: form-data; name=\"metadata\"")
        assertThat(body).contains("boundary test content")
        assertThat(body).contains("\"name\":\"boundary.txt\"")
        assertThat(body).contains("\"category\":\"other\"")

        // Verify proper boundary formatting
        val boundary = contentType.substringAfter("boundary=")
        assertThat(body).contains("--$boundary")
        // The multipart body should end with the boundary terminator
        // Some OkHttp versions may add additional whitespace or line breaks
        assertThat(body.trim()).endsWith("--$boundary--")
    }
}