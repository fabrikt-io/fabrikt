package com.cjbooms.fabrikt.servers.ktor

import com.example.multipart.controllers.FilesUploadController.Companion.filesUploadRoutes
import com.example.multipart.controllers.FilesUploadMultipleController.Companion.filesUploadMultipleRoutes
import com.example.multipart.controllers.ReceivedFile
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class KtorMultipartServerTest {

    @Test
    fun `single file upload is parsed correctly with file metadata`() {
        val fileSlot = slot<ReceivedFile>()
        val descriptionSlot = slot<String?>()

        testApplication {
            configure()

            routing {
                filesUploadRoutes(FilesUploadControllerImpl(fileSlot, descriptionSlot))
            }

            val fileContent = "Hello, World!".toByteArray()

            val response = client.submitFormWithBinaryData(
                url = "/files/upload",
                formData = formData {
                    append("file", fileContent, Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"test.txt\"")
                        append(HttpHeaders.ContentType, "text/plain")
                    })
                    append("description", "Test description")
                }
            )

            assertEquals(HttpStatusCode.OK, response.status)
            assertThat(fileSlot.captured.content).isEqualTo(fileContent)
            assertThat(fileSlot.captured.originalFileName).isEqualTo("test.txt")
            assertThat(fileSlot.captured.contentType).isEqualTo(ContentType.Text.Plain)
            assertThat(descriptionSlot.captured).isEqualTo("Test description")
            assertThat(response.bodyAsText()).contains("file-123")
        }
    }

    @Test
    fun `multiple files upload is parsed correctly with file metadata`() {
        val filesSlot = slot<List<ReceivedFile>>()
        val categorySlot = slot<String>()

        testApplication {
            configure()

            routing {
                filesUploadMultipleRoutes(FilesUploadMultipleControllerImpl(filesSlot, categorySlot))
            }

            val file1Content = "File 1 content".toByteArray()
            val file2Content = "File 2 content".toByteArray()

            val response = client.submitFormWithBinaryData(
                url = "/files/upload-multiple",
                formData = formData {
                    append("files", file1Content, Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"file1.txt\"")
                        append(HttpHeaders.ContentType, "text/plain")
                    })
                    append("files", file2Content, Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"file2.pdf\"")
                        append(HttpHeaders.ContentType, "application/pdf")
                    })
                    append("category", "documents")
                }
            )

            assertEquals(HttpStatusCode.OK, response.status)
            assertThat(filesSlot.captured).hasSize(2)
            assertThat(filesSlot.captured[0].content).isEqualTo(file1Content)
            assertThat(filesSlot.captured[0].originalFileName).isEqualTo("file1.txt")
            assertThat(filesSlot.captured[0].contentType).isEqualTo(ContentType.Text.Plain)
            assertThat(filesSlot.captured[1].content).isEqualTo(file2Content)
            assertThat(filesSlot.captured[1].originalFileName).isEqualTo("file2.pdf")
            assertThat(filesSlot.captured[1].contentType).isEqualTo(ContentType.Application.Pdf)
            assertThat(categorySlot.captured).isEqualTo("documents")
        }
    }

    private fun ApplicationTestBuilder.configure() {
        install(ContentNegotiation) {
            jackson()
        }
    }
}
