package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.cli.ClientCodeGenTargetType
import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.SerializationLibrary
import com.cjbooms.fabrikt.cli.ValidationLibrary
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.controller.KtorClientGenerator
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.TestFileUtils.toSingleFile
import com.cjbooms.fabrikt.util.GeneratedCodeAsserter.Companion.assertThatGenerated
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.model.SimpleFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Paths
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KtorClientGeneratorTest {

    @Suppress("unused")
    private fun fullApiTestCases(): Stream<String> = Stream.of(
        "ktorClient",
        "parameterNameClash",
    )

    @Suppress("unused")
    private fun groupedClientTestCases(): Stream<String> = Stream.concat(fullApiTestCases(), Stream.of("tagGrouping"))

    @BeforeEach
    fun init() {
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.CLIENT),
            clientTarget = ClientCodeGenTargetType.KTOR,
            serializationLibrary = SerializationLibrary.KOTLINX_SERIALIZATION,
            validationLibrary = ValidationLibrary.NO_VALIDATION,
        )
        ModelNameRegistry.clear()
    }

    @ParameterizedTest
    @MethodSource("groupedClientTestCases")
    fun `correct Ktor client code is generated`(testCaseName: String) {
        val packages = Packages("examples.$testCaseName")
        val apiLocation = javaClass.getResource("/examples/$testCaseName/api.yaml")!!
        val sourceApi = SourceApi(apiLocation.readText(), baseDir = Paths.get(apiLocation.toURI()))

        val expectedClient = expectedClientPath(testCaseName, "KtorClient.kt")

        val clientCode = KtorClientGenerator(
            packages,
            sourceApi
        )
            .generate(optionsFor(testCaseName))
            .clients
            .toSingleFile()

        assertThatGenerated(clientCode).isEqualTo(expectedClient)
    }

    @ParameterizedTest
    @MethodSource("fullApiTestCases")
    fun `correct Ktor API models are generated`(testCaseName: String) {
        val packages = Packages("examples.$testCaseName")
        val apiLocation = javaClass.getResource("/examples/$testCaseName/api.yaml")!!
        val sourceApi = SourceApi(apiLocation.readText(), baseDir = Paths.get(apiLocation.toURI()))

        val expectedApiModels = "/examples/$testCaseName/client/ktor/KtorApiModels.kt"

        val apiModels = KtorClientGenerator(
            packages,
            sourceApi
        )
            .generateLibrary(emptySet())
            .filterIsInstance<SimpleFile>()
            .first { it.path.fileName.toString() == "KtorApiModels.kt" }

        assertThatGenerated(apiModels.content).isEqualTo(expectedApiModels)
    }

    private fun optionsFor(testCaseName: String): Set<ClientCodeGenOptionType> =
        if (testCaseName == "tagGrouping") setOf(ClientCodeGenOptionType.GROUP_BY_TAG) else emptySet()

    private fun expectedClientPath(testCaseName: String, fileName: String): String =
        if (testCaseName == "tagGrouping") {
            "/examples/$testCaseName/client/ktor/grouped/$fileName"
        } else {
            "/examples/$testCaseName/client/ktor/$fileName"
        }

    @Test
    fun `operationId with dots is sanitized to valid Kotlin function name`() {
        val spec = """
            openapi: "3.0.0"
            info:
              title: Test API
              version: "1.0"
            paths:
              /usage:
                get:
                  operationId: app.GetApplicationApiUsage
                  responses:
                    '200':
                      description: Success
                      content:
                        application/json:
                          schema:
                            type: string
        """.trimIndent()

        val packages = Packages("com.test")
        val sourceApi = SourceApi(spec)

        val clientCode = KtorClientGenerator(packages, sourceApi)
            .generate(emptySet())
            .clients
            .toSingleFile()

        assertThat(clientCode).contains("fun appGetApplicationApiUsage(")
        assertThat(clientCode).doesNotContain("app.GetApplicationApiUsage")
    }
}