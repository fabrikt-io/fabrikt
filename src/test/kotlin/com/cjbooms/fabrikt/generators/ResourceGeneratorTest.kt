package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.model.ModelGenerator
import com.cjbooms.fabrikt.generators.model.QuarkusReflectionModelGenerator
import com.cjbooms.fabrikt.model.ResourceFile
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.ModelNameRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Paths
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ResourceGeneratorTest {

    private fun testCases(): Stream<String> = Stream.of(
        "githubApi",
    )

    @BeforeEach
    fun init() {
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.HTTP_MODELS),
        )
        ModelNameRegistry.clear()
    }

    @ParameterizedTest
    @MethodSource("testCases")
    fun `correct quarkus reflection resource is generated for different OpenApi Specifications`(testCaseName: String) {
        print("Testcase: $testCaseName")
        val basePackage = "examples.$testCaseName"
        val apiLocation = javaClass.getResource("/examples/$testCaseName/api.yaml")
        val sourceApi = SourceApi(apiLocation!!.readText(), baseDir = Paths.get(apiLocation.toURI()))
        val expectedResource =
            javaClass.getResource("/examples/$testCaseName/resources/reflection-config.json")!!.readText()
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.QUARKUS_REFLECTION_CONFIG),
        )

        val models = ModelGenerator(Packages(basePackage), sourceApi).generate()

        val resources = QuarkusReflectionModelGenerator(models).generate()?.toSingleFile()

        assertThat(resources).isEqualTo(expectedResource)
    }

    private fun ResourceFile.toSingleFile(): String = String(inputStream.readAllBytes())
}
