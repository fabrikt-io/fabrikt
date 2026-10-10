package com.cjbooms.fabrikt.cli

import com.cjbooms.fabrikt.generators.MutableSettings
import com.cjbooms.fabrikt.util.GeneratedCodeAsserter.Companion.assertThatGenerated
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText

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

    @ParameterizedTest
    @EnumSource(DependenciesGenerationMode::class)
    fun `dependencies file is generated based on dependencies generation mode`(
        generationMode: DependenciesGenerationMode,
        @TempDir tempDir: Path,
    ) {
        MutableSettings.updateSettings()
        CodeGen.generateCodeDependencies(tempDir, generationMode)
        val testCaseSuffix =
            when (generationMode) {
                DependenciesGenerationMode.NONE -> return
                DependenciesGenerationMode.GRADLE_NOTATION -> "Gradle"
                DependenciesGenerationMode.MAVEN_NOTATION -> "Maven"
            }
        assertThatGenerated(tempDir.resolve("dependencies").readText())
            .isEqualTo("/examples/dependencies$testCaseSuffix/dependencies")
    }
}
