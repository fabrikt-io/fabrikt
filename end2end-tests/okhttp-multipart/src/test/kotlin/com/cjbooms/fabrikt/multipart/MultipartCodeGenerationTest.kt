package com.cjbooms.fabrikt.multipart

import com.example.multipart.client.ApiUploadClient
import com.example.multipart.client.ApiUploadMultipleClient
import com.example.multipart.models.FileMetadata
import com.example.multipart.models.FileMetadataCategory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import okhttp3.OkHttpClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * End-to-end test verifying that multipart/form-data client generation works correctly.
 * This test validates that:
 * 1. Multipart clients are generated successfully from OpenAPI spec
 * 2. Generated clients can be instantiated
 * 3. Generated models have correct structure
 * 4. Method signatures are correct for multipart parameters
 */
class MultipartCodeGenerationTest {

    private val mapper = ObjectMapper().registerModule(JavaTimeModule())
    private val httpClient = OkHttpClient.Builder().build()

    @Test
    fun `multipart client classes are generated and instantiable`() {
        // Verify that the multipart clients can be instantiated
        val uploadClient = ApiUploadClient(mapper, "http://localhost:8080", httpClient)
        val uploadMultipleClient = ApiUploadMultipleClient(mapper, "http://localhost:8080", httpClient)

        assertThat(uploadClient).isNotNull
        assertThat(uploadMultipleClient).isNotNull
    }

    @Test
    fun `generated models have correct structure`() {
        // Verify that the generated models have the expected structure
        val metadata = FileMetadata(
            name = "test.pdf",
            category = FileMetadataCategory.DOCUMENT,
            contentType = "application/pdf",
            size = 1024L
        )

        assertThat(metadata.name).isEqualTo("test.pdf")
        assertThat(metadata.category).isEqualTo(FileMetadataCategory.DOCUMENT)
        assertThat(metadata.contentType).isEqualTo("application/pdf")
        assertThat(metadata.size).isEqualTo(1024L)
    }

    @Test
    fun `enum values are correctly generated`() {
        // Verify that the category enum has all expected values
        val categories = FileMetadataCategory.entries

        assertThat(categories).hasSize(4)
        assertThat(categories).contains(
            FileMetadataCategory.DOCUMENT,
            FileMetadataCategory.IMAGE,
            FileMetadataCategory.VIDEO,
            FileMetadataCategory.OTHER
        )

        // Verify enum values
        assertThat(FileMetadataCategory.DOCUMENT.value).isEqualTo("document")
        assertThat(FileMetadataCategory.IMAGE.value).isEqualTo("image")
        assertThat(FileMetadataCategory.VIDEO.value).isEqualTo("video")
        assertThat(FileMetadataCategory.OTHER.value).isEqualTo("other")
    }

    @Test
    fun `multipart client methods have correct signatures`() {
        val uploadClient = ApiUploadClient(mapper, "http://localhost:8080", httpClient)
        val uploadMultipleClient = ApiUploadMultipleClient(mapper, "http://localhost:8080", httpClient)

        // These would cause compilation errors if the signatures were wrong
        // We're not executing them, just verifying they can be compiled
        val fileContent = "test content".toByteArray()
        val metadata = FileMetadata(
            name = "test.txt",
            category = FileMetadataCategory.DOCUMENT
        )

        // Verify single file upload method signature
        val uploadMethodExists = try {
            uploadClient::class.java.getMethod(
                "uploadFile",
                ByteArray::class.java,
                FileMetadata::class.java,
                List::class.java,
                Map::class.java,
                Map::class.java
            )
            true
        } catch (e: NoSuchMethodException) {
            false
        }
        assertThat(uploadMethodExists).isTrue()

        // Verify multiple files upload method signature
        val uploadMultipleMethodExists = try {
            uploadMultipleClient::class.java.getMethod(
                "uploadMultipleFiles",
                List::class.java,
                FileMetadata::class.java,
                String::class.java,
                Map::class.java,
                Map::class.java
            )
            true
        } catch (e: NoSuchMethodException) {
            false
        }
        assertThat(uploadMultipleMethodExists).isTrue()
    }
}