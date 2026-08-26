package com.cjbooms.fabrikt.parser

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class KaizenParserAdapterTest {
    @Test
    fun `parses an external OpenAPI document from its URL`(
        @TempDir tempDir: Path,
    ) {
        val document =
            """
            openapi: 3.0.0
            info:
              title: External models
              version: "1.0"
            paths: {}
            components:
              schemas:
                ExternalModel:
                  type: object
            """.trimIndent()
        val documentPath = Files.writeString(tempDir.resolve("external.yaml"), document)

        val api = KaizenParserAdapter.parse(documentPath.toUri().toURL())

        assertThat(api.schemas).containsKey("ExternalModel")
    }
}
