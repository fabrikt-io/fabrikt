package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.client.OkHttpSimpleClientGenerator
import com.cjbooms.fabrikt.generators.client.OpenFeignInterfaceGenerator
import com.cjbooms.fabrikt.generators.client.SpringHttpInterfaceGenerator
import com.cjbooms.fabrikt.generators.controller.KtorClientGenerator
import com.cjbooms.fabrikt.generators.controller.KtorControllerInterfaceGenerator
import com.cjbooms.fabrikt.generators.controller.MicronautControllerInterfaceGenerator
import com.cjbooms.fabrikt.generators.controller.SpringControllerInterfaceGenerator
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.squareup.kotlinpoet.asClassName
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class DeprecatedElementGenerationTest {
    private val packages = Packages("examples.deprecatedOperations")
    private val spec by lazy { javaClass.getResource("/examples/deprecatedOperations/api.yaml")!!.readText() }

    @BeforeEach
    fun init() {
        MutableSettings.updateSettings()
        ModelNameRegistry.clear()
    }

    @ParameterizedTest
    @ValueSource(strings = ["3.0.4", "3.1.2", "3.2.0"])
    fun `deprecated operations are annotated and parameters are documented for every client and controller target`(version: String) {
        val sourceApi = SourceApi(spec.replace("3.1.0", version))
        val generatedTypes =
            listOf(
                "Spring HTTP Interface" to SpringHttpInterfaceGenerator(packages, sourceApi).generate(emptySet()).clients,
                "OpenFeign" to OpenFeignInterfaceGenerator(packages, sourceApi).generate(emptySet()).clients,
                "OkHttp simple" to OkHttpSimpleClientGenerator(packages, sourceApi).generateDynamicClientCode(),
                "Ktor client" to KtorClientGenerator(packages, sourceApi).generate(emptySet()).clients,
                "Spring controller" to
                    SpringControllerInterfaceGenerator(packages, sourceApi, JavaxValidationAnnotations).generate().controllers,
                "Micronaut controller" to
                    MicronautControllerInterfaceGenerator(packages, sourceApi, JavaxValidationAnnotations).generate().controllers,
                "Ktor controller" to KtorControllerInterfaceGenerator(packages, sourceApi).generate().controllers,
            )

        generatedTypes.forEach { (target, types) ->
            val functions = types.flatMap { it.spec.funSpecs }
            val deprecatedOperation = functions.single { it.name == "findSubject" }
            val activeOperation = functions.single { it.name == "replaceSubject" }

            assertThat(deprecatedOperation.annotations.map { it.typeName })
                .`as`(target)
                .contains(Deprecated::class.asClassName())
            assertThat(
                deprecatedOperation.parameters
                    .single { it.name == "id" }
                    .annotations
                    .map { it.typeName },
            ).`as`(target)
                .doesNotContain(Deprecated::class.asClassName())
            assertThat(deprecatedOperation.kdoc.toString())
                .`as`(target)
                .contains("@param id Deprecated.")
            assertThat(activeOperation.annotations.map { it.typeName })
                .`as`(target)
                .doesNotContain(Deprecated::class.asClassName())
            assertThat(activeOperation.kdoc.toString())
                .`as`(target)
                .doesNotContain("@param id Deprecated.")
            assertThat(
                activeOperation.parameters
                    .single { it.name == "id" }
                    .annotations
                    .map { it.typeName },
            ).`as`(target)
                .doesNotContain(Deprecated::class.asClassName())
        }
    }
}
