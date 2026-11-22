package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.CodeGenTypeOverride
import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.CustomTypeMapping
import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType
import com.cjbooms.fabrikt.cli.SerializationLibrary
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.model.ModelGenerator
import com.cjbooms.fabrikt.model.KotlinSourceSet
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.GeneratedCodeAsserter.Companion.assertThatGenerated
import com.cjbooms.fabrikt.util.GeneratedCodeAsserter.Companion.failGenerated
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.ResourceHelper.readFolder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KotlinSerializationModelGeneratorTest {
    @Suppress("unused")
    private fun testCases(): Stream<String> =
        Stream.of(
            "discriminatedOneOf",
            "discriminatorMappingSuffix",
            "primitiveTypes",
            "normalizedNameConflation",
            "openEnum",
            "customTypeMapping",
            "mixedTypeUnion",
            "arrays",
            "untypedObject",
            "additionalProperties",
            "allOfParentOrder",
        )

    private fun readRelativeFiles(root: Path): Map<String, String> =
        Files.walk(root).use { paths ->
            paths
                .filter { Files.isRegularFile(it) }
                .toList()
                .associate { root.relativize(it).joinToString("/") to Files.readString(it) }
        }

    private fun assertDirectoryMatches(
        generatedRoot: Path,
        expectedResourcePath: String,
    ) {
        val expectedRoot = Path.of("src/test/resources$expectedResourcePath")
        val generated = if (Files.exists(generatedRoot)) readRelativeFiles(generatedRoot) else emptyMap()
        val expected = if (Files.exists(expectedRoot)) readRelativeFiles(expectedRoot) else emptyMap()
        generated.forEach { (name, code) ->
            if (expected.containsKey(name)) {
                assertThatGenerated(code).isEqualTo("$expectedResourcePath$name")
            } else {
                failGenerated(code).asFileNotFound("$expectedResourcePath$name", "File not found in expected files")
            }
        }
        assertThat(expected.keys - generated.keys)
            .`as`("Expected files missing from generated output in $expectedResourcePath")
            .isEmpty()
    }

    @BeforeEach
    fun init() {
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.HTTP_MODELS),
            serializationLibrary = SerializationLibrary.KOTLINX_SERIALIZATION,
        )
        ModelNameRegistry.clear()
    }

    // @Test
    // fun `debug single test`() = `correct models are generated for different OpenApi Specifications`("insert test case")

    @ParameterizedTest
    @MethodSource("testCases")
    fun `correct models are generated for different OpenApi Specifications`(testCaseName: String) {
        print("Testcase: $testCaseName")
        if (testCaseName == "openEnum") {
            MutableSettings.addOption(ModelCodeGenOptionType.FAULT_TOLERANT_OPEN_ENUMS)
        }
        if (testCaseName == "primitiveTypes") {
            // Explicitly select the default types; the goldens must remain unchanged.
            MutableSettings.addOption(CodeGenTypeOverride.BYTE_AS_BYTEARRAY)
            MutableSettings.addOption(CodeGenTypeOverride.BINARY_AS_BYTEARRAY)
            MutableSettings.addOption(CodeGenTypeOverride.UUID_AS_UUID)
            MutableSettings.addOption(CodeGenTypeOverride.URI_AS_URI)
            MutableSettings.addOption(CodeGenTypeOverride.DATE_AS_KOTLINXLOCALDATE)
            MutableSettings.addOption(CodeGenTypeOverride.ANY_AS_ANY)
        }
        if (testCaseName == "customTypeMapping") {
            MutableSettings.updateSettings(
                genTypes = setOf(CodeGenerationType.HTTP_MODELS),
                serializationLibrary = SerializationLibrary.KOTLINX_SERIALIZATION,
                customTypeMappings =
                    listOf(
                        CustomTypeMapping.parse(
                            "string:duration=java.time.Duration;kotlinx=examples.customTypeMapping.DurationAsIsoStringSerializer",
                        ),
                    ),
            )
        }
        val basePackage = "examples.${testCaseName.replace("/", ".")}"
        val apiLocation = javaClass.getResource("/examples/$testCaseName/api.yaml")!!
        val sourceApi = SourceApi(apiLocation.readText(), baseUri = apiLocation.toURI())

        val models =
            ModelGenerator(
                Packages(basePackage),
                sourceApi,
            ).generate()

        val sourceSet = setOf(KotlinSourceSet(models.files, Paths.get("")))
        val tempDirectory = Files.createTempDirectory("model_generator_test_${testCaseName.replace("/", ".")}")
        sourceSet.forEach {
            it.writeFileTo(tempDirectory.toFile())
        }

        val baseDirectory = tempDirectory.resolve(basePackage.replace(".", File.separator))
        assertDirectoryMatches(baseDirectory.resolve("models"), "/examples/$testCaseName/models/kotlinx/")

        tempDirectory.toFile().deleteRecursively()
    }

    @Test
    fun `ANY_AS_ANY restores the unsupported untyped object failure`() {
        MutableSettings.addOption(CodeGenTypeOverride.ANY_AS_ANY)
        val basePackage = "examples.untypedObject"
        val apiLocation = javaClass.getResource("/examples/untypedObject/api.yaml")!!
        val sourceApi = SourceApi(apiLocation.readText(), baseUri = apiLocation.toURI())

        val e =
            assertThrows<UnsupportedOperationException> {
                ModelGenerator(Packages(basePackage), sourceApi).generate()
            }
        assertThat(
            e.message,
        ).isEqualTo(
            "Untyped objects not supported by selected serialization library (data: {\"type\":\"object\",\"description\":\"Any data. Object has no schema.\"})",
        )
    }

    @Test
    fun `DATETIME_AS_INSTANT override is respected with KOTLINX_SERIALIZATION`() {
        MutableSettings.addOption(CodeGenTypeOverride.DATETIME_AS_INSTANT)
        val basePackage = "examples.kotlinxDateTimeOverrides"
        val apiLocation = javaClass.getResource("/examples/kotlinxDateTimeOverrides/api.yaml")!!
        val sourceApi = SourceApi(apiLocation.readText(), baseUri = apiLocation.toURI())
        val expectedModels = readFolder(Path.of("src/test/resources/examples/kotlinxDateTimeOverrides/models/instant/"))

        val models =
            ModelGenerator(
                Packages(basePackage),
                sourceApi,
            ).generate()

        val sourceSet = setOf(KotlinSourceSet(models.files, Paths.get("")))
        val tempDirectory = Files.createTempDirectory("model_generator_test_kotlinx_instant")
        sourceSet.forEach {
            it.writeFileTo(tempDirectory.toFile())
        }

        val tempFolderContents =
            readFolder(tempDirectory.resolve(basePackage.replace(".", File.separator)).resolve("models"))
        tempFolderContents.forEach {
            if (expectedModels.containsKey(it.key)) {
                assertThat((it.value)).isEqualTo(expectedModels[it.key])
            } else {
                assertThat(it.value).isEqualTo("File not found in expected models")
            }
        }

        tempDirectory.toFile().deleteRecursively()
    }

    @Test
    fun `DATETIME_AS_LOCALDATETIME override is respected with KOTLINX_SERIALIZATION`() {
        MutableSettings.addOption(CodeGenTypeOverride.DATETIME_AS_LOCALDATETIME)
        val basePackage = "examples.kotlinxDateTimeOverrides"
        val apiLocation = javaClass.getResource("/examples/kotlinxDateTimeOverrides/api.yaml")!!
        val sourceApi = SourceApi(apiLocation.readText(), baseUri = apiLocation.toURI())
        val expectedModels = readFolder(Path.of("src/test/resources/examples/kotlinxDateTimeOverrides/models/localdatetime/"))

        val models =
            ModelGenerator(
                Packages(basePackage),
                sourceApi,
            ).generate()

        val sourceSet = setOf(KotlinSourceSet(models.files, Paths.get("")))
        val tempDirectory = Files.createTempDirectory("model_generator_test_kotlinx_localdatetime")
        sourceSet.forEach {
            it.writeFileTo(tempDirectory.toFile())
        }

        val tempFolderContents =
            readFolder(tempDirectory.resolve(basePackage.replace(".", File.separator)).resolve("models"))
        tempFolderContents.forEach {
            if (expectedModels.containsKey(it.key)) {
                assertThat((it.value)).isEqualTo(expectedModels[it.key])
            } else {
                assertThat(it.value).isEqualTo("File not found in expected models")
            }
        }

        tempDirectory.toFile().deleteRecursively()
    }

    @Test
    fun `ANY_AS_JSONELEMENT override is respected with KOTLINX_SERIALIZATION`() {
        MutableSettings.addOption(CodeGenTypeOverride.ANY_AS_JSONELEMENT)
        val basePackage = "examples.anyAsJsonElement"
        val apiLocation = javaClass.getResource("/examples/anyAsJsonElement/api.yaml")!!
        val sourceApi = SourceApi(apiLocation.readText(), baseUri = apiLocation.toURI())
        val expectedModels = readFolder(Path.of("src/test/resources/examples/anyAsJsonElement/models/kotlinx/"))

        val models =
            ModelGenerator(
                Packages(basePackage),
                sourceApi,
            ).generate()

        val sourceSet = setOf(KotlinSourceSet(models.files, Paths.get("")))
        val tempDirectory = Files.createTempDirectory("model_generator_test_kotlinx_jsonelement")
        sourceSet.forEach {
            it.writeFileTo(tempDirectory.toFile())
        }

        val tempFolderContents =
            readFolder(tempDirectory.resolve(basePackage.replace(".", File.separator)).resolve("models"))
        tempFolderContents.forEach {
            if (expectedModels.containsKey(it.key)) {
                assertThat((it.value)).isEqualTo(expectedModels[it.key])
            } else {
                assertThat(it.value).isEqualTo("File not found in expected models")
            }
        }

        tempDirectory.toFile().deleteRecursively()
    }

    @Test
    fun `all AS_STRING overrides are respected with KOTLINX_SERIALIZATION`() {
        MutableSettings.addOption(CodeGenTypeOverride.DATE_AS_STRING)
        MutableSettings.addOption(CodeGenTypeOverride.DATETIME_AS_STRING)
        MutableSettings.addOption(CodeGenTypeOverride.UUID_AS_STRING)
        MutableSettings.addOption(CodeGenTypeOverride.URI_AS_STRING)
        MutableSettings.addOption(CodeGenTypeOverride.BYTE_AS_STRING)
        MutableSettings.addOption(CodeGenTypeOverride.BINARY_AS_STRING)
        val basePackage = "examples.primitiveTypes"
        val apiLocation = javaClass.getResource("/examples/primitiveTypes/api.yaml")!!
        val sourceApi = SourceApi(apiLocation.readText(), baseUri = apiLocation.toURI())
        val expectedModels = readFolder(Path.of("src/test/resources/examples/primitiveTypes/models/kotlinxAsStringOverrides/"))

        val models =
            ModelGenerator(
                Packages(basePackage),
                sourceApi,
            ).generate()

        val sourceSet = setOf(KotlinSourceSet(models.files, Paths.get("")))
        val tempDirectory = Files.createTempDirectory("model_generator_test_kotlinx_string_overrides")
        sourceSet.forEach {
            it.writeFileTo(tempDirectory.toFile())
        }

        val tempFolderContents =
            readFolder(tempDirectory.resolve(basePackage.replace(".", File.separator)).resolve("models"))
        tempFolderContents.forEach {
            if (expectedModels.containsKey(it.key)) {
                assertThat((it.value)).isEqualTo(expectedModels[it.key])
            } else {
                assertThat(it.value).isEqualTo("File not found in expected models")
            }
        }

        tempDirectory.toFile().deleteRecursively()
    }
}
