package com.cjbooms.fabrikt.servers.spring

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest
@ContextConfiguration(classes = [TestConfig::class])
class SpringMultipartServerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var fileCapture: FileCapture

    @BeforeEach
    fun setUp() {
        fileCapture.reset()
    }

    @Test
    fun `single file upload is parsed correctly with file metadata`() {
        val fileContent = "Hello, World!".toByteArray()
        val mockFile = MockMultipartFile(
            "file",
            "test.txt",
            MediaType.TEXT_PLAIN_VALUE,
            fileContent
        )

        val descriptionPart = MockMultipartFile(
            "description",
            "",
            MediaType.TEXT_PLAIN_VALUE,
            "Test description".toByteArray()
        )

        mockMvc.perform(
            multipart("/files/upload")
                .file(mockFile)
                .file(descriptionPart)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value("file-123"))

        assertThat(fileCapture.capturedFile).isNotNull
        assertThat(fileCapture.capturedFile!!.bytes).isEqualTo(fileContent)
        assertThat(fileCapture.capturedFile!!.originalFilename).isEqualTo("test.txt")
        assertThat(fileCapture.capturedFile!!.contentType).isEqualTo(MediaType.TEXT_PLAIN_VALUE)
        assertThat(fileCapture.capturedDescription).isEqualTo("Test description")
    }

    @Test
    fun `single file upload works without optional description`() {
        val fileContent = "Content without description".toByteArray()
        val mockFile = MockMultipartFile(
            "file",
            "nodesc.txt",
            MediaType.TEXT_PLAIN_VALUE,
            fileContent
        )

        mockMvc.perform(
            multipart("/files/upload")
                .file(mockFile)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value("file-123"))

        assertThat(fileCapture.capturedFile).isNotNull
        assertThat(fileCapture.capturedFile!!.bytes).isEqualTo(fileContent)
        assertThat(fileCapture.capturedDescription).isNull()
    }

    @Test
    fun `multiple files upload is parsed correctly with file metadata`() {
        val file1Content = "File 1 content".toByteArray()
        val file2Content = "File 2 content".toByteArray()

        val mockFile1 = MockMultipartFile(
            "files",
            "file1.txt",
            MediaType.TEXT_PLAIN_VALUE,
            file1Content
        )
        val mockFile2 = MockMultipartFile(
            "files",
            "file2.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            file2Content
        )

        val categoryPart = MockMultipartFile(
            "category",
            "",
            MediaType.TEXT_PLAIN_VALUE,
            "documents".toByteArray()
        )

        mockMvc.perform(
            multipart("/files/upload-multiple")
                .file(mockFile1)
                .file(mockFile2)
                .file(categoryPart)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value("file-multi"))

        assertThat(fileCapture.capturedFiles).hasSize(2)
        assertThat(fileCapture.capturedFiles!![0].bytes).isEqualTo(file1Content)
        assertThat(fileCapture.capturedFiles!![0].originalFilename).isEqualTo("file1.txt")
        assertThat(fileCapture.capturedFiles!![0].contentType).isEqualTo(MediaType.TEXT_PLAIN_VALUE)
        assertThat(fileCapture.capturedFiles!![1].bytes).isEqualTo(file2Content)
        assertThat(fileCapture.capturedFiles!![1].originalFilename).isEqualTo("file2.pdf")
        assertThat(fileCapture.capturedFiles!![1].contentType).isEqualTo(MediaType.APPLICATION_PDF_VALUE)
        assertThat(fileCapture.capturedCategory).isEqualTo("documents")
    }
}

@Configuration
open class TestConfig {
    @Bean
    open fun fileCapture() = FileCapture()

    @Bean
    open fun filesUploadController(fileCapture: FileCapture) = FilesUploadControllerImpl(fileCapture)

    @Bean
    open fun filesUploadMultipleController(fileCapture: FileCapture) = FilesUploadMultipleControllerImpl(fileCapture)
}
