package com.cjbooms.fabrikt.server.spring

import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup
import java.nio.charset.StandardCharsets


class ApiUploadSimpleControllerTest {

    @Test
    fun uploadFile() {
        val fileContent = "bar".toByteArray(StandardCharsets.UTF_8)
        val file = MockMultipartFile("file", "image.jpg", "image/jpeg", fileContent)

        standaloneSetup(ApiUploadSimpleControllerImpl()).build()
            .perform(
                multipart("/api/upload-simple")
                    .file(file)
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value("id"))
            .andExpect(jsonPath("$.filename").value(file.originalFilename))
            .andExpect(jsonPath("$.uploadedAt").isNotEmpty())
            .andExpect(jsonPath("$.size").value(3))
    }
}
