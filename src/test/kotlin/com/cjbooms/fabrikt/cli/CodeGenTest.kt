package com.cjbooms.fabrikt.cli

import com.cjbooms.fabrikt.generators.MutableSettings
import com.cjbooms.fabrikt.util.GeneratedCodeAsserter.Companion.assertThatGenerated
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText

class CodeGenTest {
    @AfterEach
    fun resetSettings() = MutableSettings.updateSettings()

    @Test
    fun `internal API compiles within its module and serializes but is inaccessible to another module`(
        @TempDir directory: Path,
    ) {
        val spec = directory.resolve("api.yaml")
        Files.writeString(spec, readTextResource("/examples/generatedAnnotation/api.yaml"))
        ModelNameRegistry.clear()
        CodeGen.main(
            arrayOf(
                "--base-package",
                "com.example",
                "--api-file",
                spec.toString(),
                "--output-directory",
                directory.toString(),
                "--targets",
                "CLIENT",
                "--visibility",
                "INTERNAL",
                "--serialization-library",
                "JACKSON_2",
                "--validation-library",
                "NO_VALIDATION",
            ),
        )
        val consumer = directory.resolve("Consumer.kt")
        Files.writeString(
            consumer,
            """
            package consumer
            import com.example.models.Pet
            import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
            fun roundTrip(): String {
                val mapper = jacksonObjectMapper()
                val pet = Pet(name = "Alice", tag = "test")
                return mapper.readValue(mapper.writeValueAsString(pet), Pet::class.java).name
            }
            """.trimIndent(),
        )
        val classpath =
            listOf(
                Unit::class.java,
                com.fasterxml.jackson.annotation.JsonProperty::class.java,
                com.fasterxml.jackson.databind.ObjectMapper::class.java,
                com.fasterxml.jackson.core.JsonFactory::class.java,
                com.fasterxml.jackson.module.kotlin.KotlinModule::class.java,
                okhttp3.OkHttpClient::class.java,
                okio.Buffer::class.java,
            ).map {
                Path
                    .of(
                        it.protectionDomain.codeSource.location
                            .toURI(),
                    ).toString()
            }.distinct()
                .joinToString(File.pathSeparator)
        val output = directory.resolve("classes")
        val sources =
            Files.walk(directory.resolve("src/main/kotlin")).use { paths ->
                paths.filter { it.toString().endsWith(".kt") }.map { it.toString() }.toList()
            }
        val diagnostics = ByteArrayOutputStream()

        fun compile(
            files: List<String>,
            destination: Path,
            dependencies: String,
        ): ExitCode =
            K2JVMCompiler().exec(
                PrintStream(diagnostics),
                "-no-stdlib",
                "-no-reflect",
                "-jvm-target",
                "17",
                "-classpath",
                dependencies,
                "-d",
                destination.toString(),
                *files.toTypedArray(),
            )
        assertThat(compile(sources + consumer.toString(), output, classpath))
            .withFailMessage(diagnostics.toString())
            .isEqualTo(ExitCode.OK)
        URLClassLoader(arrayOf(output.toUri().toURL()), javaClass.classLoader).use { loader ->
            assertThat(loader.loadClass("consumer.ConsumerKt").getMethod("roundTrip").invoke(null)).isEqualTo("Alice")
        }
        Files.writeString(
            consumer,
            """
            import com.example.models.Pet
            import com.example.client.ApiResponse
            import com.example.client.PetsClient
            internal fun useApi(client: PetsClient): ApiResponse<Pet> = error("unused")
            """.trimIndent(),
        )
        diagnostics.reset()
        assertThat(compile(listOf(consumer.toString()), directory.resolve("other-module"), "$classpath${File.pathSeparator}$output"))
            .isEqualTo(ExitCode.COMPILATION_ERROR)
        assertThat(diagnostics.toString()).contains("internal", "Pet", "PetsClient", "ApiResponse")
    }

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
