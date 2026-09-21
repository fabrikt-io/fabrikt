package com.cjbooms.fabrikt.cli

import com.cjbooms.fabrikt.generators.MutableSettings
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class CodeGenTest {
    @Test
    fun `json-schema-file with no fragment converts the whole file as a bare JSON Schema document`(
        @TempDir tempDir: Path,
    ) {
        MutableSettings.updateSettings(genTypes = setOf(CodeGenerationType.HTTP_MODELS))
        val schemaFile = tempDir.resolve("bare-schema.yaml")
        Files.writeString(
            schemaFile,
            """
            title: BareSchema
            type: object
            properties:
              name:
                type: string
            required: [name]
            """.trimIndent(),
        )

        CodeGen.generate(
            basePackage = "examples.bareschema",
            apiFile = CodeGenArgs.DEFAULT_API_FILE,
            jsonSchemaFile = schemaFile.toString(),
            outputDir = tempDir,
            srcPath = Path.of("src/main/kotlin"),
            resourcesPath = Path.of("src/main/resources"),
        )

        val generated =
            Files.readString(tempDir.resolve("src/main/kotlin/examples/bareschema/models/BareSchema.kt"))
        assertThat(generated).contains("public data class BareSchema(")
        assertThat(generated).contains("public val name: String,")
    }
}
