package com.cjbooms.fabrikt.clients

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
import feign.Feign
import feign.form.FormEncoder
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.net.ServerSocket

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OpenFeignMultipartTest {
    private val port: Int = ServerSocket(0).use { socket -> socket.localPort }
    private val wiremock: WireMockServer = WireMockServer(options().port(port).notifier(ConsoleNotifier(true)))
    private val mapper = ObjectMapper().registerKotlinModule()

    private lateinit var uploadClient: FilesUploadClient
    private lateinit var uploadMultipleClient: FilesUploadMultipleClient

    @BeforeEach
    fun setUp() {
        wiremock.start()

        uploadClient = Feign.builder()
            .encoder(FormEncoder(JacksonEncoder(mapper)))
            .decoder(JacksonDecoder(mapper))
            .target(FilesUploadClient::class.java, "http://localhost:$port")

        uploadMultipleClient = Feign.builder()
            .encoder(FormEncoder(JacksonEncoder(mapper)))
            .decoder(JacksonDecoder(mapper))
            .target(FilesUploadMultipleClient::class.java, "http://localhost:$port")
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
            file = fileContent,
            description = "Test file"
        )

        assertThat(result).isEqualTo(expectedResponse)

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
            files = listOf(file1Content, file2Content),
            category = "documents"
        )

        assertThat(result).isEqualTo(expectedResponse)

        wiremock.verify(
            postRequestedFor(urlEqualTo("/files/upload-multiple"))
                .withHeader("Content-Type", containing("multipart/form-data"))
        )
    }
}
