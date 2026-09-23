package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.cli.ClientCodeGenTargetType
import com.cjbooms.fabrikt.cli.CodeGenArgs
import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.CodeGenerator
import com.cjbooms.fabrikt.cli.ControllerCodeGenTargetType
import com.cjbooms.fabrikt.cli.OutputOptionType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.model.Destinations
import com.cjbooms.fabrikt.model.GenerationMetadata
import com.cjbooms.fabrikt.model.KotlinSourceSet
import com.cjbooms.fabrikt.model.SimpleFile
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.GeneratedCodeAsserter.Companion.assertThatExpectedFiles
import com.cjbooms.fabrikt.util.GeneratedCodeAsserter.Companion.assertThatGenerated
import com.cjbooms.fabrikt.util.Linter
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.ResourceHelper.getFileNamesInFolder
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import java.nio.file.Path

class GeneratedAnnotationTest {
    @BeforeEach
    fun resetNames() = ModelNameRegistry.clear()

    @AfterEach
    fun resetSettings() = MutableSettings.updateSettings()

    @ParameterizedTest
    @ValueSource(strings = ["generatedAnnotation"])
    fun `models and enum companions match golden files`(example: String) {
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.HTTP_MODELS),
            outputOptions = setOf(OutputOptionType.ADD_GENERATED_ANNOTATION),
            generationMetadata = GenerationMetadata(version = "27.0.1"),
        )
        val files = generate(example)
        assertThat(files.keys).containsExactlyInAnyOrder("Pet", "PetType")
        files.forEach { (name, content) ->
            assertThatGenerated(Linter.lintString(content)).isEqualTo("/examples/$example/models/$name.kt")
        }
    }

    @ParameterizedTest
    @EnumSource(ClientCodeGenTargetType::class)
    fun `all client targets and their libraries match annotated golden files`(target: ClientCodeGenTargetType) {
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.CLIENT),
            clientTarget = target,
            clientOptions = setOf(ClientCodeGenOptionType.RESILIENCE4J),
            outputOptions = setOf(OutputOptionType.ADD_GENERATED_ANNOTATION),
            generationMetadata = GenerationMetadata(version = "27.0.1"),
        )
        assertGoldenFiles(generate(), "clients/${target.name.lowercase()}")
    }

    @ParameterizedTest
    @EnumSource(ControllerCodeGenTargetType::class)
    fun `all controller targets match annotated golden files`(target: ControllerCodeGenTargetType) {
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.CONTROLLERS),
            controllerTarget = target,
            outputOptions = setOf(OutputOptionType.ADD_GENERATED_ANNOTATION),
            generationMetadata = GenerationMetadata(version = "27.0.1"),
        )
        assertGoldenFiles(generate(), "controllers/${target.name.lowercase()}")
    }

    @Test
    fun `sealed models and their subtypes are annotated`() {
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.HTTP_MODELS),
            outputOptions = setOf(OutputOptionType.ADD_GENERATED_ANNOTATION),
            generationMetadata = GenerationMetadata(version = "27.0.1"),
        )
        val files = generate("discriminatedOneOf")
        assertThat(files.values.joinToString()).contains("sealed interface", "sealed class")
        assertGeneratedDeclarationsAnnotated(files)
    }

    @Test
    fun `annotation is opt in and CLI accepts the option`() {
        val args = CodeGenArgs.parse(arrayOf("--base-package", "example", "--output-opts", "ADD_GENERATED_ANNOTATION"))
        assertThat(args.outputOptions).containsExactly(OutputOptionType.ADD_GENERATED_ANNOTATION)
        MutableSettings.updateSettings(genTypes = setOf(CodeGenerationType.HTTP_MODELS))
        generate().values.forEach { assertThat(it).doesNotContain("@Generated", "javax.annotation.processing") }
    }

    private fun assertGoldenFiles(
        files: Map<String, String>,
        directory: String,
    ) {
        assertGeneratedDeclarationsAnnotated(files)
        val examplePath = Path.of("src/test/resources/examples/generatedAnnotation")
        val modelFileNames = getFileNamesInFolder(examplePath.resolve("models")).toSet()
        files.forEach { (name, content) ->
            val expectedDirectory = if ("$name.kt" in modelFileNames) "models" else directory
            assertThatGenerated(Linter.lintString(content))
                .isEqualTo("/examples/generatedAnnotation/$expectedDirectory/$name.kt")
        }
        val generatedFiles = files.mapKeys { "${it.key}.kt" }
        listOf("models", directory).forEach {
            assertThatExpectedFiles(examplePath.resolve(it)).areContainedInGenerated(generatedFiles)
        }
    }

    private fun assertGeneratedDeclarationsAnnotated(files: Map<String, String>) {
        assertThat(files).isNotEmpty()
        files.forEach { (name, content) ->
            val types =
                Regex(
                    "(?m)^ *public (?:data |sealed |enum |open |abstract )?(?:class|interface|object|companion object)\\b",
                ).findAll(content).count()
            val topLevelFunctions =
                Regex("(?m)^(?:public|private|internal) (?:(?:inline|suspend|operator|infix|tailrec|external) )*fun\\b")
                    .findAll(content)
                    .count()
            assertThat(content).describedAs(name).contains("@Generated")
            val annotations = Regex("@Generated\\(").findAll(content).count()
            assertThat(annotations).describedAs(name).isEqualTo(types + topLevelFunctions)
        }
    }

    private fun generate(example: String = "generatedAnnotation"): Map<String, String> =
        CodeGenerator(
            Packages("com.example"),
            SourceApi(readTextResource("/examples/$example/api.yaml")),
            Destinations.MAIN_KT_SOURCE,
            Destinations.MAIN_RESOURCES,
        ).generate()
            .flatMap {
                when (it) {
                    is KotlinSourceSet -> it.files.map { file -> file.name to file.toString() }
                    is SimpleFile ->
                        listOf(
                            it.path.fileName
                                .toString()
                                .removeSuffix(".kt") to it.content,
                        )
                    else -> emptyList()
                }
            }.toMap()
}
