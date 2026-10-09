package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.cli.ClientCodeGenTargetType
import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.CodeGenerator
import com.cjbooms.fabrikt.cli.ControllerCodeGenTargetType
import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType
import com.cjbooms.fabrikt.cli.SerializationLibrary
import com.cjbooms.fabrikt.cli.ValidationLibrary
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.client.OkHttpClientGenerator
import com.cjbooms.fabrikt.generators.client.OpenFeignInterfaceGenerator
import com.cjbooms.fabrikt.generators.client.SpringHttpInterfaceGenerator
import com.cjbooms.fabrikt.model.KotlinSourceSet
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.GeneratedCodeAsserter.Companion.assertThatGenerated
import com.cjbooms.fabrikt.util.Linter
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.ResourceHelper.getFileNamesInFolder
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import java.nio.file.Path

class DirectionalEndpointGeneratorTest {
    @AfterEach
    fun reset() {
        MutableSettings.updateSettings()
        ModelNameRegistry.clear()
    }

    @ParameterizedTest
    @EnumSource(ClientCodeGenTargetType::class, names = ["OK_HTTP", "OPEN_FEIGN", "SPRING_HTTP_INTERFACE"])
    fun `typed response representations refer to generated response models`(target: ClientCodeGenTargetType) {
        listOf(false, true).forEach { multipleRequestTypes ->
            val options = setOf(ClientCodeGenOptionType.RESPONSE_MEDIA_TYPE_FUNCTIONS)
            MutableSettings.updateSettings(
                genTypes = setOf(CodeGenerationType.CLIENT),
                modelOptions = setOf(ModelCodeGenOptionType.REQUEST_RESPONSE_MODELS),
                clientTarget = target,
                clientOptions = options,
            )
            val source =
                SourceApi(
                    """
                    openapi: 3.0.3
                    info:
                      title: Directional response representations
                      version: '1.0'
                    paths:
                      /details:
                        post:
                          operationId: createDetails
                          requestBody:
                            required: true
                            content:
                              application/json:
                                schema:
                                  ${'$'}ref: '#/components/schemas/First'
                          responses:
                            '200':
                              description: OK
                              content:
                                application/json:
                                  schema:
                                    ${'$'}ref: '#/components/schemas/First'
                                application/problem+json:
                                  schema:
                                    ${'$'}ref: '#/components/schemas/Second'
                    components:
                      schemas:
                        First:
                          type: object
                          required: [name, secret]
                          properties:
                            name:
                              type: string
                              readOnly: true
                            secret:
                              type: string
                              writeOnly: true
                        Second:
                          type: object
                          properties:
                            message:
                              type: string
                              readOnly: true
                    """.trimIndent().let { spec ->
                        if (multipleRequestTypes) {
                            spec.replace(
                                "      responses:",
                                "          text/json:\n            schema:\n              ${'$'}ref: '#/components/schemas/Second'\n      responses:",
                            )
                        } else {
                            spec
                        }
                    },
                )
            val packages = Packages("examples.directionalResponses")
            val files =
                CodeGenerator(packages, source, Path.of(""), Path.of(""))
                    .generate()
                    .filterIsInstance<KotlinSourceSet>()
                    .flatMap { it.files }
            val modelNames = files.filter { it.packageName == packages.models }.map { it.name }
            assertThat(
                modelNames,
            ).contains("FirstRequest", "FirstResponse", "SecondRequest", "SecondResponse").doesNotContain("First", "Second")
            val generator =
                when (target) {
                    ClientCodeGenTargetType.OK_HTTP -> OkHttpClientGenerator(packages, source, Path.of("src/main/kotlin"))
                    ClientCodeGenTargetType.OPEN_FEIGN -> OpenFeignInterfaceGenerator(packages, source)
                    ClientCodeGenTargetType.SPRING_HTTP_INTERFACE -> SpringHttpInterfaceGenerator(packages, source)
                    else -> error("Unsupported target")
                }
            generator.generate(options).clients.forEach { client ->
                val functions = client.spec.funSpecs.associateBy { it.name }
                val requestTypes = mutableMapOf("createDetails" to "FirstRequest")
                if (multipleRequestTypes) requestTypes["createDetailsTextJson"] = "SecondRequest"
                requestTypes.forEach { (operation, requestModel) ->
                    mapOf("Json" to "FirstResponse", "ProblemJson" to "SecondResponse").forEach { (suffix, model) ->
                        val function = functions.getValue("$operation$suffix")
                        assertThat(function.returnType.toString()).contains("${packages.models}.$model")
                        assertThat(function.parameters.map { it.type.toString() }).contains("${packages.models}.$requestModel")
                    }
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["3.0.3", "3.1.2", "3.2.0"])
    fun `all endpoint targets use directional models`(version: String) {
        val targets =
            ClientCodeGenTargetType.entries.map { "client_${it.name.lowercase()}" } +
                ControllerCodeGenTargetType.entries.map { "controller_${it.name.lowercase()}" }
        targets.forEach { target ->
            ModelNameRegistry.clear()
            val controller = target.startsWith("controller_")
            val controllerTarget =
                if (controller) {
                    ControllerCodeGenTargetType.valueOf(target.removePrefix("controller_").uppercase())
                } else {
                    ControllerCodeGenTargetType.default
                }
            val clientTarget =
                if (controller) {
                    ClientCodeGenTargetType.default
                } else {
                    ClientCodeGenTargetType.valueOf(target.removePrefix("client_").uppercase())
                }
            MutableSettings.updateSettings(
                genTypes = setOf(if (controller) CodeGenerationType.CONTROLLERS else CodeGenerationType.CLIENT),
                modelOptions = setOf(ModelCodeGenOptionType.REQUEST_RESPONSE_MODELS),
                validationLibrary = ValidationLibrary.NO_VALIDATION,
                serializationLibrary = SerializationLibrary.JACKSON_2,
                controllerTarget = controllerTarget,
                clientTarget = clientTarget,
            )
            val spec = readTextResource("/examples/directionalEndpoints/api.yaml").replace("3.0.3", version)
            // Micronaut controllers do not support multipart parameters.
            val supportedSpec =
                if (controller && controllerTarget == ControllerCodeGenTargetType.MICRONAUT) {
                    spec.replace(Regex("(?s)  /pets/upload:.*?(?=components:)"), "")
                } else {
                    spec
                }
            val source = SourceApi(supportedSpec)
            val files =
                CodeGenerator(Packages("examples.directionalEndpoints"), source, Path.of(""), Path.of(""))
                    .generate()
                    .filterIsInstance<KotlinSourceSet>()
                    .flatMap { it.files }
                    .distinct()
            assertThat(files.filter { it.packageName.endsWith(".models") }.map { it.name })
                .contains("PetRequest", "PetResponse", "PetStatusRequest")
            val contracts = files.filter { it.name.startsWith("Pets") }
            assertThat(contracts).isNotEmpty()
            val text = contracts.joinToString("\n") { it.toString() }
            assertThat(text)
                .contains("PetRequest", "PetResponse", "StateRequest", "List<PetResponse>", "Map<String, PetResponse")
                .doesNotContain("created:")
            contracts.forEach { file ->
                assertThatGenerated(Linter.lintString(file.toString())).isEqualTo("/examples/directionalEndpoints/$target/${file.name}.kt")
            }
            assertThat(contracts.map { "${it.name}.kt" }).containsExactlyInAnyOrderElementsOf(
                getFileNamesInFolder(Path.of("src/test/resources/examples/directionalEndpoints/$target")),
            )
        }
    }
}
