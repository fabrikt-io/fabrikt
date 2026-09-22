package com.cjbooms.fabrikt.util

import com.cjbooms.fabrikt.cli.OutputOptionType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.MutableSettings
import com.cjbooms.fabrikt.generators.client.OkHttpClientLibraryFiles
import com.cjbooms.fabrikt.model.GenerationMetadata
import com.cjbooms.fabrikt.util.GeneratedAnnotations.addGeneratedAnnotations
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.Instant

class GeneratedAnnotationsTest {
    @AfterEach
    fun resetSettings() = MutableSettings.updateSettings()

    @ParameterizedTest
    @MethodSource("helperFiles")
    fun `every top-level helper function is annotated exactly once`(file: FileSpec) {
        MutableSettings.updateSettings(outputOptions = setOf(OutputOptionType.ADD_GENERATED_ANNOTATION))
        val annotated = file.addGeneratedAnnotations()
        val functions = annotated.members.filterIsInstance<FunSpec>()
        assertThat(functions).isNotEmpty()
        functions.forEach { function ->
            assertThat(function.annotations.filter { it.typeName == ClassName("javax.annotation.processing", "Generated") })
                .describedAs("${file.name}.${function.name}")
                .hasSize(1)
        }
        assertThat(annotated.addGeneratedAnnotations().toString()).isEqualTo(annotated.toString())
    }

    @ParameterizedTest
    @MethodSource("helperFiles")
    fun `helper files are unchanged when annotation is disabled`(file: FileSpec) {
        MutableSettings.updateSettings()
        assertThat(file.addGeneratedAnnotations()).isSameAs(file)
    }

    @Test
    fun `annotations include the public generator name and generation metadata`() {
        MutableSettings.updateSettings(
            outputOptions = setOf(OutputOptionType.ADD_GENERATED_ANNOTATION),
            generationMetadata = GenerationMetadata(Instant.parse("2001-07-04T19:08:56.235Z"), "27.0.1"),
        )
        val code = OkHttpClientLibraryFiles.httpResilience4jUtil(Packages("example")).addGeneratedAnnotations().toString()
        assertThat(code).contains(
            "value = [\"io.fabrikt.cli.CodeGen\"]",
            "date = \"2001-07-04T19:08:56.235Z\"",
            "comments = \"Generated with Fabrikt v27.0.1\"",
        )
    }

    @Test
    fun `generation metadata defaults to the current time and bundled version`() {
        val before = Instant.now()
        val metadata = GenerationMetadata()
        val version = javaClass.getResource("/META-INF/fabrikt-version.txt")!!.readText().trim()
        assertThat(metadata.date).isBetween(before, Instant.now())
        assertThat(metadata.version).isEqualTo(version).isNotBlank()
    }

    companion object {
        @JvmStatic
        fun helperFiles(): List<FileSpec> =
            listOf(
                OkHttpClientLibraryFiles.httpUtil(Packages("example"), false),
                OkHttpClientLibraryFiles.httpResilience4jUtil(Packages("example")),
            )
    }
}
