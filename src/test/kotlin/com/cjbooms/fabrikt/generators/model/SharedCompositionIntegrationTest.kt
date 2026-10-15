package com.cjbooms.fabrikt.generators.model

import com.cjbooms.fabrikt.cli.CustomTypeMapping
import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.MutableSettings
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.nio.file.Files
import java.nio.file.Path

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

    @ParameterizedTest
    @ValueSource(strings = ["3.0.4", "3.1.2", "3.2.0"])
    fun `custom object mappings retain schema contracts without generating the external class`(version: String) {
        val spec = readTextResource("/examples/sharedCompositionMappedContracts/api.yaml").replace("3.0.4", version)
        MutableSettings.updateSettings(
            modelSuffix = "Dto",
            modelOptions = setOf(ModelCodeGenOptionType.SHARED_COMPOSITION_CONTRACTS, ModelCodeGenOptionType.EXCLUDE_READ_ONLY),
            customTypeMappings = listOf(CustomTypeMapping.parse("object:custom-record=external.CustomRecord")),
        )
        val files = ModelGenerator(Packages("example"), SourceApi(spec)).generate().files.associate { it.name to it.toString() }

        assertThat(files.keys).contains("ADtoComposite", "BDto").doesNotContain("ADto", "CustomRecord", "UnrelatedDtoComposite")
        assertThat(files.keys.none { it.startsWith("Unrelated") }).isTrue()
        assertThat(files.getValue("ADtoComposite")).contains("val id: String", "val secret: String?").doesNotContain("serverValue", "@")
        assertThat(files.getValue("BDto")).contains("ADtoComposite", "override val id: String")
        assertThat(files.getValue("OtherDto")).doesNotContain("ADtoComposite")
    }

    @Test
    fun `a contract is generated even when all composed concrete classes are externally mapped`() {
        val spec =
            readTextResource("/examples/sharedCompositionMappedContracts/api.yaml")
                .replace("    B:", "    B:\n      format: custom-record")
                .replace(
                    "${'$'}ref: '#/components/schemas/Details'",
                    "type: object\n          properties:\n            value: {type: integer}",
                )
        MutableSettings.updateSettings(
            modelOptions = setOf(ModelCodeGenOptionType.SHARED_COMPOSITION_CONTRACTS, ModelCodeGenOptionType.EXCLUDE_WRITE_ONLY),
            customTypeMappings = listOf(CustomTypeMapping.parse("object:custom-record=external.CustomRecord")),
        )
        val files = ModelGenerator(Packages("example"), SourceApi(spec)).generate().files.associate { it.name to it.toString() }

        assertThat(files.keys).contains("AComposite", "ADetails").doesNotContain("A", "B", "CustomRecord")
        assertThat(files.getValue("AComposite")).contains("val id: String", "val serverValue: String?").doesNotContain("secret")
    }

    @Test
    fun `a mapped schema contract does not coerce independently generated inline property types`() {
        val spec =
            readTextResource("/examples/sharedCompositionMappedContracts/api.yaml")
                .replace(
                    "${'$'}ref: '#/components/schemas/Details'",
                    "type: object\n          properties:\n            value: {type: integer}",
                )
        MutableSettings.updateSettings(
            modelOptions = setOf(ModelCodeGenOptionType.SHARED_COMPOSITION_CONTRACTS),
            customTypeMappings = listOf(CustomTypeMapping.parse("object:custom-record=external.CustomRecord")),
        )
        val files = ModelGenerator(Packages("example"), SourceApi(spec)).generate().files.associate { it.name to it.toString() }

        assertThat(files.keys).contains("AComposite", "ADetails", "BDetails")
        assertThat(files.getValue("AComposite")).contains("val details: ADetails")
        assertThat(files.getValue("B")).contains("val details: BDetails").doesNotContain("AComposite", "override val")
    }

    @Test
    fun `a mapped standalone schema retains its contract across external references`() {
        val directory = Files.createTempDirectory(Path.of("build"), "mapped-composition-").toAbsolutePath()
        Files.writeString(
            directory.resolve("mapped.yaml"),
            """
            type: object
            format: custom-record
            required: [id]
            properties:
              id: {type: string}
            """.trimIndent(),
        )
        val spec =
            """
            openapi: 3.0.4
            info: {title: External mapped composition, version: '1'}
            paths: {}
            components:
              schemas:
                B:
                  type: object
                  allOf:
                    - ${'$'}ref: './mapped.yaml'
                    - type: object
                      properties:
                        label: {type: string}
            """.trimIndent()
        MutableSettings.updateSettings(
            modelOptions = setOf(ModelCodeGenOptionType.SHARED_COMPOSITION_CONTRACTS),
            customTypeMappings = listOf(CustomTypeMapping.parse("object:custom-record=external.CustomRecord")),
        )
        val files =
            ModelGenerator(Packages("example"), SourceApi(spec, directory.resolve("api.yaml").toUri()))
                .generate()
                .files
                .associate { it.name to it.toString() }

        assertThat(files.keys).contains("MappedComposite", "B").doesNotContain("Mapped", "CustomRecord")
        assertThat(files.getValue("MappedComposite")).contains("val id: String")
        assertThat(files.getValue("B")).contains("MappedComposite", "override val id: String")
    }
}
