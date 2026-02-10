package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.CodeGenTypeOverride
import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType
import com.cjbooms.fabrikt.cli.SerializationLibrary
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.model.ModelGenerator
import com.cjbooms.fabrikt.model.KotlinSourceSet
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.GeneratedCodeAsserter.Companion.assertThatExpectedFiles
import com.cjbooms.fabrikt.util.GeneratedCodeAsserter.Companion.assertThatGenerated
import com.cjbooms.fabrikt.util.GeneratedCodeAsserter.Companion.failGenerated
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.ResourceHelper.getFileNamesInFolder
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
    private fun testCases(): Stream<String> = Stream.of(
        "discriminatedOneOf",
        "primitiveTypes",
    )

    @BeforeEach
    fun init() {
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.HTTP_MODELS),
            serializationLibrary = SerializationLibrary.KOTLINX_SERIALIZATION
        )
        ModelNameRegistry.clear()
    }

    // @Test
    // fun `debug single test`() = `correct models are generated for different OpenApi Specifications`("insert test case")

    @ParameterizedTest
    @MethodSource("testCases")
    fun `correct models are generated for different OpenApi Specifications`(testCaseName: String) {
        print("Testcase: $testCaseName")
        if (testCaseName == "discriminatedOneOf" || testCaseName == "oneOfMarkerInterface") {
            MutableSettings.addOption(ModelCodeGenOptionType.SEALED_INTERFACES_FOR_ONE_OF)
        }
        val basePackage = "examples.${testCaseName.replace("/", ".")}"
        val apiLocation = javaClass.getResource("/examples/$testCaseName/api.yaml")!!
        val sourceApi = SourceApi(apiLocation.readText(), baseDir = Paths.get(apiLocation.toURI()))
        val expectedModelsPath = "/examples/$testCaseName/models/kotlinx/"
        val expectedModels = getFileNamesInFolder(Path.of("src/test/resources$expectedModelsPath"))

        val models = ModelGenerator(
            Packages(basePackage),
            sourceApi,
        ).generate()

        val sourceSet = setOf(KotlinSourceSet(models.files, Paths.get("")))
        val tempDirectory = Files.createTempDirectory("model_generator_test_${testCaseName.replace("/", ".")}")
        sourceSet.forEach {
            it.writeFileTo(tempDirectory.toFile())
        }

        val tempFolderContents =
            readFolder(tempDirectory.resolve(basePackage.replace(".", File.separator)).resolve("models"))
        tempFolderContents.forEach {
            if (expectedModels.contains(it.key)) {
                assertThatGenerated(it.value)
                    .isEqualTo( "$expectedModelsPath${it.key}")
            } else {
                failGenerated(it.value).asFileNotFound("$expectedModelsPath${it.key}", "File not found in expected models")
            }
        }
        assertThatExpectedFiles(Path.of("src/test/resources$expectedModelsPath"))
            .areContainedInGenerated(tempFolderContents)

        tempDirectory.toFile().deleteRecursively()
    }

    @Test
    fun `schemas configured with additionalProperties results in UnsupportedOperationException`() {
        val basePackage = "examples.additionalProperties"
        val apiLocation = javaClass.getResource("/examples/additionalProperties/api.yaml")!!
        val sourceApi = SourceApi(apiLocation.readText(), baseDir = Paths.get(apiLocation.toURI()))

        val e = assertThrows<UnsupportedOperationException> {
            ModelGenerator(Packages(basePackage), sourceApi,).generate()
        }
        assertThat(e.message).isEqualTo("Additional properties not supported by selected serialization library")
    }

    @Test
    fun `schemas without properties result in UnsupportedOperationException`() {
        val basePackage = "examples.untypedObject"
        val apiLocation = javaClass.getResource("/examples/untypedObject/api.yaml")!!
        val sourceApi = SourceApi(apiLocation.readText(), baseDir = Paths.get(apiLocation.toURI()))

        val e = assertThrows<UnsupportedOperationException> {
            val models = ModelGenerator(Packages(basePackage), sourceApi,).generate()
            val sourceSet = setOf(KotlinSourceSet(models.files, Paths.get("")))
            println(sourceSet)
        }
        assertThat(e.message).isEqualTo("Untyped objects not supported by selected serialization library (data: {\"type\":\"object\",\"description\":\"Any data. Object has no schema.\"})")
    }

    @Test
    fun `DATETIME_AS_INSTANT override is respected with KOTLINX_SERIALIZATION`() {
        MutableSettings.addOption(CodeGenTypeOverride.DATETIME_AS_INSTANT)
        val basePackage = "examples.kotlinxDateTimeOverrides"
        val apiLocation = javaClass.getResource("/examples/kotlinxDateTimeOverrides/api.yaml")!!
        val sourceApi = SourceApi(apiLocation.readText(), baseDir = Paths.get(apiLocation.toURI()))
        val expectedModels = readFolder(Path.of("src/test/resources/examples/kotlinxDateTimeOverrides/models/instant/"))

        val models = ModelGenerator(
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
        val sourceApi = SourceApi(apiLocation.readText(), baseDir = Paths.get(apiLocation.toURI()))
        val expectedModels = readFolder(Path.of("src/test/resources/examples/kotlinxDateTimeOverrides/models/localdatetime/"))

        val models = ModelGenerator(
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
    fun `all AS_STRING overrides are respected with KOTLINX_SERIALIZATION`() {
        MutableSettings.addOption(CodeGenTypeOverride.DATE_AS_STRING)
        MutableSettings.addOption(CodeGenTypeOverride.DATETIME_AS_STRING)
        MutableSettings.addOption(CodeGenTypeOverride.UUID_AS_STRING)
        MutableSettings.addOption(CodeGenTypeOverride.URI_AS_STRING)
        MutableSettings.addOption(CodeGenTypeOverride.BYTE_AS_STRING)
        MutableSettings.addOption(CodeGenTypeOverride.BINARY_AS_STRING)
        val basePackage = "examples.primitiveTypes"
        val apiLocation = javaClass.getResource("/examples/primitiveTypes/api.yaml")!!
        val sourceApi = SourceApi(apiLocation.readText(), baseDir = Paths.get(apiLocation.toURI()))
        val expectedModels = readFolder(Path.of("src/test/resources/examples/primitiveTypes/models/kotlinxAsStringOverrides/"))

        val models = ModelGenerator(
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
