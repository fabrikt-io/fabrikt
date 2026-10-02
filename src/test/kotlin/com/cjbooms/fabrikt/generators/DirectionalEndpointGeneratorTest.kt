package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.cli.ClientCodeGenTargetType
import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.CodeGenerator
import com.cjbooms.fabrikt.cli.ControllerCodeGenTargetType
import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType
import com.cjbooms.fabrikt.cli.ValidationLibrary
import com.cjbooms.fabrikt.configurations.Packages
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
import org.junit.jupiter.params.provider.ValueSource
import java.nio.file.Path

class DirectionalEndpointGeneratorTest {
    @AfterEach
    fun reset() {
        MutableSettings.updateSettings()
        ModelNameRegistry.clear()
    }

    @ParameterizedTest
    @ValueSource(strings = ["3.0.3", "3.1.2", "3.2.0"])
    fun `all endpoint targets use directional models`(version: String) {
        val targets =
            ClientCodeGenTargetType.entries.map { "client_${it.name.lowercase()}" } +
                ControllerCodeGenTargetType.entries.map { "controller_${it.name.lowercase()}" } + "client_ok_http_enhanced"
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
                if (controller || target.endsWith("enhanced")) {
                    ClientCodeGenTargetType.default
                } else {
                    ClientCodeGenTargetType.valueOf(target.removePrefix("client_").uppercase())
                }
            MutableSettings.updateSettings(
                genTypes = setOf(if (controller) CodeGenerationType.CONTROLLERS else CodeGenerationType.CLIENT),
                modelOptions = setOf(ModelCodeGenOptionType.REQUEST_RESPONSE_MODELS),
                validationLibrary = ValidationLibrary.NO_VALIDATION,
                controllerTarget = controllerTarget,
                clientTarget = clientTarget,
                clientOptions = if (target.endsWith("enhanced")) setOf(ClientCodeGenOptionType.RESILIENCE4J) else emptySet(),
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
            assertThat(ModelNameRegistry.direction).isNull()
        }
    }
}
