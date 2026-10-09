package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.client.OkHttpSimpleClientGenerator
import com.cjbooms.fabrikt.generators.client.OpenFeignInterfaceGenerator
import com.cjbooms.fabrikt.generators.client.SpringHttpInterfaceGenerator
import com.cjbooms.fabrikt.generators.controller.KtorClientGenerator
import com.cjbooms.fabrikt.generators.controller.KtorControllerInterfaceGenerator
import com.cjbooms.fabrikt.generators.controller.MicronautControllerInterfaceGenerator
import com.cjbooms.fabrikt.generators.controller.SpringControllerInterfaceGenerator
import com.cjbooms.fabrikt.generators.model.ModelGenerator
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.ModelNameRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class InlineErrorResponseGenerationTest {
    private val packages = Packages("examples.inlineErrorResponses")
    private val spec by lazy { javaClass.getResource("/examples/inlineErrorResponses/api.yaml")!!.readText() }

    @BeforeEach
    fun init() {
        MutableSettings.updateSettings()
        ModelNameRegistry.clear()
    }

    @ParameterizedTest
    @ValueSource(strings = ["3.0.3", "3.1.2", "3.2.0"])
    fun `inline error models are available for every client and controller target`(version: String) {
        val sourceApi = SourceApi(spec.replace("3.0.3", version))
        val models = ModelGenerator(packages, sourceApi).generate()
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

        assertThat(models.files.map { it.name })
            .containsExactlyInAnyOrder(
                "GetWidgetResponse",
                "GetWidgetResponse404",
                "GetWidgetResponse422Item",
                "FallbackProblem",
            )
        generatedTypes.forEach { (target, types) ->
            assertThat(types.flatMap { it.spec.funSpecs }.map { it.name })
                .`as`(target)
                .contains("getWidget")
        }
    }
}
