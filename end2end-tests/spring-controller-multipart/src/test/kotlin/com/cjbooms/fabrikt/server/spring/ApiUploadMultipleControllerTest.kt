package com.cjbooms.fabrikt.server.spring

import com.example.models.FileMetadata
import com.example.models.FileMetadataCategory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup
import java.nio.charset.StandardCharsets


class ApiUploadMultipleControllerTest {
    private val objectMapper: ObjectMapper = ObjectMapper()
        .registerModule(KotlinModule.Builder().build())

    @Test
    fun uploadFile() {
        val fileContent = "bar".toByteArray(StandardCharsets.UTF_8)
        val file1 = MockMultipartFile("files", "image1.jpg", "image/jpeg", fileContent)
        val file2 = MockMultipartFile("files", "image2.jpg", "image/jpeg", fileContent)

        val metadata = objectMapper.writeValueAsString(
            FileMetadata(
                name = "name",
                category = FileMetadataCategory.OTHER,
                contentType = "image/jpeg",
                size = 123,
            )
        )
        val metadataPart =
            MockMultipartFile(
                "commonMetadata",
                "commonMetadata",
                "application/json",
                metadata.toByteArray(StandardCharsets.UTF_8)
            )

        standaloneSetup(ApiUploadMultipartControllerImpl()).build()
            .perform(
                multipart("/api/upload-multiple")
                    .file(file1)
                    .file(file2)
                    .file(metadataPart)
                    .param("description", "my description")
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value("id"))
            .andExpect(jsonPath("$[0].originalName").value(file1.originalFilename))
            .andExpect(jsonPath("$[0].storedName").value(file1.name))
            .andExpect(jsonPath("$[0].url").value("url"))
            .andExpect(jsonPath("$[0].uploadedAt").isNotEmpty())
            .andExpect(jsonPath("$[0].size").value(3))
            .andExpect(jsonPath("$[1].id").value("id"))
            .andExpect(jsonPath("$[1].originalName").value(file2.originalFilename))
            .andExpect(jsonPath("$[1].storedName").value(file2.name))
            .andExpect(jsonPath("$[1].url").value("url"))
            .andExpect(jsonPath("$[1].uploadedAt").isNotEmpty())
            .andExpect(jsonPath("$[1].size").value(3))
    }
}
