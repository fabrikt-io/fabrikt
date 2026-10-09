package com.cjbooms.fabrikt.generators.model

import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.MutableSettings
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class SharedCompositionIntegrationTest {
    @BeforeEach
    fun reset() {
        MutableSettings.updateSettings()
        ModelNameRegistry.clear()
    }

    @ParameterizedTest
    @ValueSource(strings = ["3.0.4", "3.1.2", "3.2.0"])
    fun `contracts use the same resolved composition across supported specification versions`(version: String) {
        val spec = readTextResource("/examples/sharedCompositionContracts/api.yaml").replace("3.0.4", version)
        MutableSettings.updateSettings(modelSuffix = "Dto", modelOptions = setOf(ModelCodeGenOptionType.SHARED_COMPOSITION_CONTRACTS))
        val files = ModelGenerator(Packages("example"), SourceApi(spec)).generate().files.associate { it.name to it.toString() }
        assertThat(files.keys).contains("ADtoComposite")
        listOf("ADto", "BDto", "DDto").forEach { name ->
            assertThat(files.getValue(name)).contains("ADtoComposite")
        }
        assertThat(files.getValue("OtherDto")).doesNotContain("ADtoComposite")
    }

    @ParameterizedTest
    @ValueSource(strings = ["sharedCompositionContracts", "sharedCompositionUnions", "sharedCompositionRefinements"])
    fun `disabled contracts retain exactly the concrete generated files`(example: String) {
        val spec = readTextResource("/examples/$example/api.yaml")

        fun generate(enabled: Boolean): Map<String, String> {
            ModelNameRegistry.clear()
            MutableSettings.updateSettings(
                modelOptions = if (enabled) setOf(ModelCodeGenOptionType.SHARED_COMPOSITION_CONTRACTS) else emptySet(),
            )
            return ModelGenerator(Packages("example"), SourceApi(spec)).generate().files.associate { it.name to it.toString() }
        }
        val before = generate(false)
        val enabled = generate(true)
        val after = generate(false)
        assertThat(after).isEqualTo(before)
        assertThat(enabled.keys).containsAll(before.keys)
        assertThat(enabled.size).isGreaterThan(before.size)
    }
}
