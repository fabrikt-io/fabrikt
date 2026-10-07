package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.cli.ClientCodeGenTargetType
import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.client.OpenFeignInterfaceGenerator
import com.cjbooms.fabrikt.generators.model.ModelGenerator
import com.cjbooms.fabrikt.model.ClientType
import com.cjbooms.fabrikt.model.Models
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.GeneratedCodeAsserter.Companion.assertThatGenerated
import com.cjbooms.fabrikt.util.Linter
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import com.squareup.kotlinpoet.FileSpec
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OpenFeignClientGeneratorTest {
    @Suppress("unused")
    private fun fullApiTestCases(): Stream<String> =
        Stream.of(
            "openFeignClient",
            "multiMediaType",
            "pathLevelParameters",
            "parameterNameClash",
            "tagGrouping",
            "cookieParameters",
        )

    @BeforeEach
    fun init() {
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.CLIENT),
            clientTarget = ClientCodeGenTargetType.OPEN_FEIGN,
            modelOptions = setOf(ModelCodeGenOptionType.X_EXTENSIBLE_ENUMS, ModelCodeGenOptionType.DISABLE_SEALED_INTERFACES_FOR_ONE_OF),
            openfeignClientName = "test-feign-client-name",
        )
        ModelNameRegistry.clear()
    }

    @ParameterizedTest
    @MethodSource("fullApiTestCases")
    fun `correct Open Feign interfaces are generated from a full API definition`(testCaseName: String) {
        runTestCase(testCaseName, options = optionsFor(testCaseName))
    }

    @Test
    fun `correct Open Feign interfaces are generated with suspend modifier`() {
        runTestCase(
            testCaseName = "openFeignClient",
            clientFileName = "SuspendableOpenFeignClient.kt",
            options = setOf(ClientCodeGenOptionType.SUSPEND_MODIFIER),
        )
    }

    @Test
    fun `correct Open Feign interfaces are generated with response entity wrapper and spring annotation`() {
        runTestCase(
            testCaseName = "openFeignClient",
            clientFileName = "OpenFeignClientWithResponseEntity.kt",
            options =
                setOf(
                    ClientCodeGenOptionType.SPRING_RESPONSE_ENTITY_WRAPPER,
                    ClientCodeGenOptionType.SPRING_CLOUD_OPENFEIGN_STARTER_ANNOTATION,
                ),
        )
    }

    @Test
    fun `one typed function per response media type is generated for the Open Feign client`() {
        val packages = Packages("examples.multiMediaType")
        val sourceApi = SourceApi(readTextResource("/examples/multiMediaType/api.yaml"))

        val clientCode =
            OpenFeignInterfaceGenerator(packages, sourceApi)
                .generate(setOf(ClientCodeGenOptionType.RESPONSE_MEDIA_TYPE_FUNCTIONS))
                .clients
                .toSingleFile()

        assertThatGenerated(clientCode).isEqualTo("/examples/multiMediaType/client/responseMediaTypeFunctions/OpenFeignClient.kt")
    }

    @Test
    fun `a cookie wrapper and its request helper are dropped together when the helper's name collides`() {
        val spec =
            """
            openapi: "3.0.0"
            info:
              title: Test API
              version: "1.0"
            paths:
              /items:
                get:
                  parameters:
                    - name: trackingId
                      in: cookie
                      required: true
                      schema:
                        type: string
                  responses:
                    '200':
                      description: Success
                      content:
                        application/json:
                          schema:
                            ${'$'}ref: '#/components/schemas/A'
                        application/vnd.custom+json:
                          schema:
                            ${'$'}ref: '#/components/schemas/B'
                post:
                  operationId: getItemsJsonWithCookieHeader
                  responses:
                    '204':
                      description: No content
            components:
              schemas:
                A:
                  type: object
                  properties:
                    a:
                      type: string
                B:
                  type: object
                  properties:
                    b:
                      type: string
            """.trimIndent()

        val content =
            OpenFeignInterfaceGenerator(Packages("com.test"), SourceApi(spec))
                .generate(setOf(ClientCodeGenOptionType.RESPONSE_MEDIA_TYPE_FUNCTIONS))
                .clients
                .toSingleFile()

        assertThat(content.split("fun getItemsJsonWithCookieHeader(")).hasSize(2)
        assertThat(content).doesNotContain("fun getItemsJson(")
        assertThat(content).contains("fun getItemsVndCustomJson(")
    }

    private fun runTestCase(
        testCaseName: String,
        clientFileName: String = "OpenFeignClient.kt",
        options: Set<ClientCodeGenOptionType> = emptySet(),
    ) {
        val packages = Packages("examples.$testCaseName")
        val sourceApi = SourceApi(readTextResource("/examples/$testCaseName/api.yaml"))

        val expectedModel = "/examples/$testCaseName/models/ClientModels.kt"
        val expectedClient = expectedClientPath(testCaseName, clientFileName)

        val models =
            ModelGenerator(
                packages,
                sourceApi,
            ).generate().toSingleFile()
        val clientCode =
            OpenFeignInterfaceGenerator(
                packages,
                sourceApi,
            ).generate(options)
                .clients
                .toSingleFile()

        assertThatGenerated(clientCode).isEqualTo(expectedClient)
        if (testCaseName != "tagGrouping") {
            assertThatGenerated(models).isEqualTo(expectedModel)
        }
    }

    private fun optionsFor(testCaseName: String): Set<ClientCodeGenOptionType> =
        if (testCaseName == "tagGrouping") setOf(ClientCodeGenOptionType.GROUP_BY_TAG) else emptySet()

    private fun expectedClientPath(
        testCaseName: String,
        fileName: String,
    ): String =
        if (testCaseName == "tagGrouping") {
            "/examples/$testCaseName/client/grouped/$fileName"
        } else {
            "/examples/$testCaseName/client/$fileName"
        }

    private fun Collection<ClientType>.toSingleFile(): String {
        val destPackage = if (this.isNotEmpty()) first().destinationPackage else ""
        val singleFileBuilder = FileSpec.builder(destPackage, "dummyFilename")
        this.forEach {
            val builder =
                singleFileBuilder
                    .addType(it.spec)
            builder.build()
        }
        return Linter.lintString(singleFileBuilder.build().toString())
    }

    private fun Models.toSingleFile(): String {
        val destPackage = if (models.isNotEmpty()) models.first().destinationPackage else ""
        val singleFileBuilder = FileSpec.builder(destPackage, "dummyFilename")
        models
            .sortedBy { it.spec.name }
            .forEach {
                val builder =
                    singleFileBuilder
                        .addType(it.spec)
                builder.build()
            }
        return Linter.lintString(singleFileBuilder.build().toString())
    }
}
