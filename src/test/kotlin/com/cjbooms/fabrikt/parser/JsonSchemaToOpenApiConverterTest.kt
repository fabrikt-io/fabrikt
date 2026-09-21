package com.cjbooms.fabrikt.parser

import com.beust.jcommander.ParameterException
import com.cjbooms.fabrikt.model.SchemaConversionOptions
import com.cjbooms.fabrikt.util.YamlObjectMapper
import com.fasterxml.jackson.databind.JsonNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class JsonSchemaToOpenApiConverterTest {
    private fun parse(yaml: String): JsonNode = YamlObjectMapper.instance.readTree(yaml)

    private fun convert(
        yaml: String,
        pointer: String = "",
        rootName: String? = null,
    ) = JsonSchemaToOpenApiConverter.convert(parse(yaml), SchemaConversionOptions(pointer, rootName))

    @Test
    fun `explicit root name override beats a present title`() {
        val doc =
            convert(
                """
                title: FromTitle
                properties:
                  a: { type: string }
                """.trimIndent(),
                rootName = "FromOverride",
            )
        assertThat(doc.at("/info/title").asText()).isEqualTo("FromOverride")
    }

    @Test
    fun `title is used when no override is given`() {
        val doc =
            convert(
                """
                title: FromTitle
                properties:
                  a: { type: string }
                """.trimIndent(),
            )
        assertThat(doc.at("/info/title").asText()).isEqualTo("FromTitle")
    }

    @Test
    fun `metadata name is used when neither override nor title is present`() {
        val doc =
            convert(
                """
                metadata:
                  name: FromMetadata
                spec:
                  schemaObject:
                    properties:
                      a: { type: string }
                """.trimIndent(),
                pointer = "/spec/schemaObject",
            )
        assertThat(doc.at("/info/title").asText()).isEqualTo("FromMetadata")
    }

    @Test
    fun `falls back to a default root schema name when no override, title, or metadata name is present`() {
        val doc =
            convert(
                """
                properties:
                  a: { type: string }
                """.trimIndent(),
            )
        assertThat(doc.at("/info/title").asText()).isEqualTo("Schema")
        assertThat(doc.at("/components/schemas/Schema").isMissingNode).isFalse()
    }

    @Test
    fun `throws when the pointer does not resolve to a node`() {
        val ex =
            assertThrows<ParameterException> {
                convert(
                    """
                    properties:
                      a: { type: string }
                    """.trimIndent(),
                    pointer = "/does/not/exist",
                )
            }
        assertThat(ex.message).contains("--json-schema-file")
    }

    @Test
    fun `throws when the pointer resolves to a non-object`() {
        val ex =
            assertThrows<ParameterException> {
                convert(
                    """
                    spec:
                      schemaObject: "not an object"
                    """.trimIndent(),
                    pointer = "/spec/schemaObject",
                )
            }
        assertThat(ex.message).contains("--json-schema-file")
    }

    @Test
    fun `throws when the schema has neither definitions nor properties`() {
        assertThrows<ParameterException> {
            convert(
                """
                title: Empty
                type: object
                """.trimIndent(),
            )
        }
    }

    @Test
    fun `throws when the root name collides with a definition name`() {
        val ex =
            assertThrows<ParameterException> {
                convert(
                    """
                    definitions:
                      Foo:
                        type: object
                        properties:
                          x: { type: string }
                    properties:
                      y: { type: string }
                    """.trimIndent(),
                    rootName = "Foo",
                )
            }
        assertThat(ex.message).contains("--json-schema-root-name")
    }

    @Test
    fun `throws when the same name appears in both definitions and defs`() {
        assertThrows<ParameterException> {
            convert(
                """
                definitions:
                  Foo:
                    type: object
                ${"\$"}defs:
                  Foo:
                    type: object
                properties:
                  y: { type: string }
                """.trimIndent(),
            )
        }
    }

    @Test
    fun `defs is honoured identically to definitions including ref rewriting`() {
        val doc =
            convert(
                """
                ${"\$"}defs:
                  Foo:
                    type: object
                    properties:
                      x: { type: string }
                properties:
                  foo:
                    ${"\$"}ref: '#/${"\$"}defs/Foo'
                """.trimIndent(),
                rootName = "Root",
            )
        assertThat(doc.at("/components/schemas/Foo").isMissingNode).isFalse()
        assertThat(doc.at("/components/schemas/Root/properties/foo/\$ref").asText())
            .isEqualTo("#/components/schemas/Foo")
    }

    @Test
    fun `rewrites a direct property ref from definitions to components schemas`() {
        val doc =
            convert(
                """
                definitions:
                  Foo:
                    type: object
                properties:
                  foo:
                    ${"\$"}ref: '#/definitions/Foo'
                """.trimIndent(),
                rootName = "Root",
            )
        assertThat(doc.at("/components/schemas/Root/properties/foo/\$ref").asText())
            .isEqualTo("#/components/schemas/Foo")
    }

    @Test
    fun `rewrites a ref nested inside oneOf`() {
        val doc =
            convert(
                """
                definitions:
                  Foo:
                    type: object
                    properties:
                      x: { type: string }
                  Bar:
                    type: object
                    oneOf:
                      - ${"\$"}ref: '#/definitions/Foo'
                properties:
                  bar:
                    ${"\$"}ref: '#/definitions/Bar'
                """.trimIndent(),
                rootName = "Root",
            )
        assertThat(doc.at("/components/schemas/Bar/oneOf/0/\$ref").asText())
            .isEqualTo("#/components/schemas/Foo")
    }

    @Test
    fun `rejects an external file reference`() {
        assertThrows<ParameterException> {
            convert(
                """
                properties:
                  foo:
                    ${"\$"}ref: 'other.json#/definitions/X'
                """.trimIndent(),
                rootName = "Root",
            )
        }
    }

    @Test
    fun `rejects a ref pointing inside another schema`() {
        assertThrows<ParameterException> {
            convert(
                """
                definitions:
                  Foo:
                    type: object
                    properties:
                      bar: { type: string }
                properties:
                  foo:
                    ${"\$"}ref: '#/definitions/Foo/properties/bar'
                """.trimIndent(),
                rootName = "Root",
            )
        }
    }

    @Test
    fun `promotes a top-level string definition with examples to x-extensible-enum`() {
        val doc =
            convert(
                """
                definitions:
                  State:
                    type: string
                    examples: [a, b]
                properties:
                  s:
                    ${"\$"}ref: '#/definitions/State'
                """.trimIndent(),
                rootName = "Root",
            )
        val state = doc.at("/components/schemas/State")
        assertThat(state.has("examples")).isFalse()
        assertThat(state.at("/x-extensible-enum").map { it.asText() }).containsExactly("a", "b")
    }

    @Test
    fun `does not promote a top-level definition with examples if it also has properties`() {
        val doc =
            convert(
                """
                definitions:
                  NotAnEnum:
                    type: string
                    examples: [a, b]
                    properties:
                      x: { type: string }
                properties:
                  s:
                    ${"\$"}ref: '#/definitions/NotAnEnum'
                """.trimIndent(),
                rootName = "Root",
            )
        val notAnEnum = doc.at("/components/schemas/NotAnEnum")
        assertThat(notAnEnum.has("x-extensible-enum")).isFalse()
    }

    @Test
    fun `demotes a property-level examples array to a singular first-element example`() {
        val doc =
            convert(
                """
                properties:
                  unit:
                    type: string
                    examples: ["ml"]
                """.trimIndent(),
                rootName = "Root",
            )
        val unit = doc.at("/components/schemas/Root/properties/unit")
        assertThat(unit.has("examples")).isFalse()
        assertThat(unit.at("/example").asText()).isEqualTo("ml")
    }

    @Test
    fun `gives an implicit type object to a definition with properties but no type`() {
        val doc =
            convert(
                """
                definitions:
                  Campaign:
                    properties:
                      id: { type: string }
                properties:
                  c:
                    ${"\$"}ref: '#/definitions/Campaign'
                """.trimIndent(),
                rootName = "Root",
            )
        assertThat(doc.at("/components/schemas/Campaign/type").asText()).isEqualTo("object")
    }

    @Test
    fun `upgrades a true boolean exclusiveMinimum into the numeric form`() {
        val doc =
            convert(
                """
                properties:
                  score:
                    type: integer
                    minimum: 5
                    exclusiveMinimum: true
                """.trimIndent(),
                rootName = "Root",
            )
        val score = doc.at("/components/schemas/Root/properties/score")
        assertThat(score.has("minimum")).isFalse()
        assertThat(score.at("/exclusiveMinimum").asInt()).isEqualTo(5)
    }

    @Test
    fun `drops an unenforceable true boolean exclusiveMinimum with no paired minimum`() {
        val doc =
            convert(
                """
                properties:
                  score:
                    type: integer
                    exclusiveMinimum: true
                """.trimIndent(),
                rootName = "Root",
            )
        val score = doc.at("/components/schemas/Root/properties/score")
        assertThat(score.has("exclusiveMinimum")).isFalse()
        assertThat(score.has("minimum")).isFalse()
        assertThat(score.at("/type").asText()).isEqualTo("integer")
    }

    @Test
    fun `collapses a false boolean exclusiveMinimum to plain minimum`() {
        val doc =
            convert(
                """
                properties:
                  score:
                    type: integer
                    minimum: 5
                    exclusiveMinimum: false
                """.trimIndent(),
                rootName = "Root",
            )
        val score = doc.at("/components/schemas/Root/properties/score")
        assertThat(score.has("exclusiveMinimum")).isFalse()
        assertThat(score.at("/minimum").asInt()).isEqualTo(5)
    }

    @Test
    fun `leaves an already-numeric exclusiveMinimum unchanged`() {
        val doc =
            convert(
                """
                properties:
                  score:
                    type: integer
                    exclusiveMinimum: 5
                """.trimIndent(),
                rootName = "Root",
            )
        val score = doc.at("/components/schemas/Root/properties/score")
        assertThat(score.at("/exclusiveMinimum").asInt()).isEqualTo(5)
    }

    @Test
    fun `normalises const into a single-valued enum`() {
        val doc =
            convert(
                """
                properties:
                  kind:
                    const: "X"
                """.trimIndent(),
                rootName = "Root",
            )
        val kind = doc.at("/components/schemas/Root/properties/kind")
        assertThat(kind.has("const")).isFalse()
        assertThat(kind.at("/enum").map { it.asText() }).containsExactly("X")
    }

    @Test
    fun `keeps an existing enum when const is also present`() {
        val doc =
            convert(
                """
                properties:
                  kind:
                    const: "X"
                    enum: ["X", "Y"]
                """.trimIndent(),
                rootName = "Root",
            )
        val kind = doc.at("/components/schemas/Root/properties/kind")
        assertThat(kind.at("/enum").map { it.asText() }).containsExactly("X", "Y")
    }

    @Test
    fun `drops tuple-form items entirely`() {
        val doc =
            convert(
                """
                properties:
                  tuple:
                    type: array
                    items:
                      - type: string
                      - type: integer
                """.trimIndent(),
                rootName = "Root",
            )
        val tuple = doc.at("/components/schemas/Root/properties/tuple")
        assertThat(tuple.has("items")).isFalse()
    }

    @Test
    fun `drops prefixItems entirely`() {
        val doc =
            convert(
                """
                properties:
                  tuple:
                    type: array
                    prefixItems:
                      - type: string
                """.trimIndent(),
                rootName = "Root",
            )
        val tuple = doc.at("/components/schemas/Root/properties/tuple")
        assertThat(tuple.has("prefixItems")).isFalse()
        assertThat(tuple.has("items")).isFalse()
    }

    @Test
    fun `drops the not keyword entirely`() {
        val doc =
            convert(
                """
                properties:
                  x:
                    type: string
                    not:
                      ${"\$"}ref: '#/definitions/Excluded'
                """.trimIndent(),
                rootName = "Root",
            )
        val x = doc.at("/components/schemas/Root/properties/x")
        assertThat(x.has("not")).isFalse()
    }

    @Test
    fun `drops a oneOf whose branches are all constraint-only fragments`() {
        val doc =
            convert(
                """
                properties:
                  x:
                    properties:
                      type: { type: string }
                    oneOf:
                      - required: [type]
                        properties:
                          type:
                            pattern: '^Postgres$'
                      - required: [type]
                        properties:
                          type:
                            pattern: '^MySQL$'
                """.trimIndent(),
                rootName = "Root",
            )
        val x = doc.at("/components/schemas/Root/properties/x")
        assertThat(x.has("oneOf")).isFalse()
        assertThat(x.has("properties")).isTrue()
    }

    @Test
    fun `keeps only the typed branch when mixed with a constraint-only fragment`() {
        val doc =
            convert(
                """
                definitions:
                  Typed:
                    type: object
                    properties:
                      a: { type: string }
                properties:
                  x:
                    oneOf:
                      - required: [type]
                        properties:
                          type:
                            pattern: '^Postgres$'
                      - ${"\$"}ref: '#/definitions/Typed'
                """.trimIndent(),
                rootName = "Root",
            )
        val oneOf = doc.at("/components/schemas/Root/properties/x/oneOf")
        assertThat(oneOf.size()).isEqualTo(1)
        assertThat(oneOf[0].at("/\$ref").asText()).isEqualTo("#/components/schemas/Typed")
    }

    @Test
    fun `keeps a oneOf of inline objects whose properties carry type information`() {
        val doc =
            convert(
                """
                properties:
                  x:
                    oneOf:
                      - properties:
                          a: { type: string }
                      - properties:
                          b: { type: integer }
                """.trimIndent(),
                rootName = "Root",
            )
        val oneOf = doc.at("/components/schemas/Root/properties/x/oneOf")
        assertThat(oneOf.size()).isEqualTo(2)
    }

    @Test
    fun `leaves type arrays including null untouched`() {
        val doc =
            convert(
                """
                properties:
                  x:
                    type: ["string", "null"]
                """.trimIndent(),
                rootName = "Root",
            )
        val type = doc.at("/components/schemas/Root/properties/x/type")
        assertThat(type.map { it.asText() }).containsExactly("string", "null")
    }

    @Test
    fun `converts successfully from an empty pointer against a bare JSON Schema document`() {
        val doc =
            convert(
                """
                title: Bare
                properties:
                  a: { type: string }
                """.trimIndent(),
                pointer = "",
            )
        assertThat(doc.at("/info/title").asText()).isEqualTo("Bare")
    }

    @Test
    fun `resolves a pointer segment containing a literal percent or plus character`() {
        val doc =
            convert(
                """
                spec:
                  50%off+deal:
                    title: Deal
                    properties:
                      a: { type: string }
                """.trimIndent(),
                pointer = "/spec/50%off+deal",
            )
        assertThat(doc.at("/info/title").asText()).isEqualTo("Deal")
    }

    @Test
    fun `always declares openapi 3_1_0`() {
        val doc =
            convert(
                """
                properties:
                  a: { type: string }
                """.trimIndent(),
                rootName = "Root",
            )
        assertThat(doc.at("/openapi").asText()).isEqualTo("3.1.0")
    }
}
