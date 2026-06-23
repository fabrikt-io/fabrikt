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


class ApiUploadControllerTest {
    private val objectMapper: ObjectMapper = ObjectMapper()
        .registerModule(KotlinModule.Builder().build())

    @Test
    fun uploadFile() {
        val fileContent = "bar".toByteArray(StandardCharsets.UTF_8)
        val file = MockMultipartFile("file", "image.jpg", "image/jpeg", fileContent)

        val metadata = objectMapper.writeValueAsString(
            FileMetadata(
                name = "name",
                category = FileMetadataCategory.OTHER,
                contentType = "image/jpeg",
                size = 123,
            )
        )
        val metadataPart =
            MockMultipartFile("metadata", "metadata", "application/json", metadata.toByteArray(StandardCharsets.UTF_8))

        val tags = """["foo", "bar"]"""
        val tagsPart =
            MockMultipartFile("tags", "tags", "application/json", tags.toByteArray(StandardCharsets.UTF_8))

        standaloneSetup(ApiUploadControllerImpl()).build()
            .perform(
                multipart("/api/upload")
                    .file(file)
                    .file(metadataPart)
                    .file(tagsPart)
                    .param("version", "1.3")
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value("id"))
            .andExpect(jsonPath("$.originalName").value(file.originalFilename))
            .andExpect(jsonPath("$.storedName").value(file.name))
            .andExpect(jsonPath("$.url").value("url"))
            .andExpect(jsonPath("$.uploadedAt").isNotEmpty())
            .andExpect(jsonPath("$.size").value(3))
    }
}
