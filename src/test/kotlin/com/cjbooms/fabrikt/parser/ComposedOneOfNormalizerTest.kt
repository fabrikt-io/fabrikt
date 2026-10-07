package com.cjbooms.fabrikt.parser

import com.cjbooms.fabrikt.util.YamlObjectMapper
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ComposedOneOfNormalizerTest {
    private fun example(): JsonNode =
        YamlObjectMapper.instance.readTree(javaClass.getResource("/examples/composedOneOf/api.yaml")!!.readText())

    @ParameterizedTest
    @ValueSource(strings = ["3.0.3", "3.1.0", "3.2.0"])
    fun `both parser representations see the distributed composition`(version: String) {
        val root = example() as ObjectNode
        root.put("openapi", version)
        val parsed = OpenApiDocumentParser.parse(YamlObjectMapper.instance.writeValueAsString(root))
        val schemas = parsed.asOpenApi3Document().schemas

        for (prefix in listOf("Nested", "Named", "Sibling", "Deduced", "Tagged")) {
            val name = "Composed${prefix}Combined"
            val union = schemas.getValue(name)
            assertThat(union.allOfSchemas).isEmpty()
            assertThat(union.oneOfSchemas).hasSize(2)
            union.oneOfSchemas.forEach { variant ->
                assertThat(variant.allOfSchemas).hasSize(2)
                assertThat(variant.allOfSchemas.first().properties).containsKey("id")
                assertThat(parsed.source.componentSchemas).containsKey(variant.name!!)
            }
        }
    }

    @Test
    fun `normalization preserves existing components and allocates distinct variant names`() {
        val root = example()
        val schemas = root.path("components").path("schemas") as ObjectNode
        val collision = "ComposedNestedCombinedComposedNestedA"
        val existing = schemas.putObject(collision).put("type", "string")
        val originalA = schemas.get("ComposedNestedA").deepCopy<JsonNode>()

        assertThat(ComposedOneOfNormalizer.normalize(root)).isTrue()
        assertThat(schemas.get(collision)).isEqualTo(existing)
        assertThat(schemas.get("ComposedNestedA")).isEqualTo(originalA)
        assertThat(
            schemas
                .path("ComposedNestedCombined")
                .path("oneOf")
                .first()
                .path("\$ref")
                .asText(),
        ).isEqualTo("#/components/schemas/${collision}Extra")
        assertThat(ComposedOneOfNormalizer.normalize(root)).isFalse()
    }

    @Test
    fun `discriminator aliases requiredness and property metadata survive distribution`() {
        val root = example()
        val combined = root.path("components").path("schemas").path("ComposedTaggedCombined") as ObjectNode
        combined.putArray("required").add("id")
        combined.put("deprecated", true)
        combined.put("description", "Combined payload")

        ComposedOneOfNormalizer.normalize(root)

        val ref =
            combined
                .path("discriminator")
                .path("mapping")
                .path("a")
                .asText()
        assertThat(combined.path("deprecated").asBoolean()).isTrue()
        assertThat(combined.path("description").asText()).isEqualTo("Combined payload")
        assertThat(root.at(ref.removePrefix("#")).path("required").map { it.asText() }).contains("id")
        assertThat(ref).isEqualTo(
            combined
                .path("oneOf")
                .first()
                .path("\$ref")
                .asText(),
        )
    }

    @Test
    fun `ordinary oneOf definitions remain unchanged`() {
        val root = YamlObjectMapper.instance.readTree(javaClass.getResource("/examples/namedOneOfDeduction/api.yaml")!!.readText())
        val original = root.deepCopy<JsonNode>()
        assertThat(ComposedOneOfNormalizer.normalize(root)).isFalse()
        assertThat(root).isEqualTo(original)
    }

    @Test
    fun `references into a composition retain their original target`() {
        val root = example()
        val schemas = root.path("components").path("schemas") as ObjectNode
        schemas.putObject("InteriorReference").put("\$ref", "#/components/schemas/ComposedNestedCombined/allOf/1")
        val original = schemas.path("ComposedNestedCombined").deepCopy<JsonNode>()

        ComposedOneOfNormalizer.normalize(root)

        assertThat(schemas.path("ComposedNestedCombined")).isEqualTo(original)
    }
}
