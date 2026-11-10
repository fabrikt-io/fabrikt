package com.cjbooms.fabrikt.parser

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.math.BigDecimal

class SourceSchemaSemanticsTest {
    @Test
    fun `schema classification is reused only within its source document`() {
        var reads = 0

        fun document(body: String): Pair<GeneratorSchemaDocument, String> {
            val schema = parseSchema("3.1.2", body)
            val sources = mapOf(schema.location to schema)
            val countedSources =
                object : Map<String, SourceSchema> by sources {
                    override fun get(key: String): SourceSchema? {
                        reads++
                        return sources[key]
                    }
                }
            return GeneratorSchemaDocument(OpenApiVersion.parse("3.1.2"), emptyMap(), countedSources, emptyMap()) to schema.location
        }
        val (first, location) = document("type: string")
        assertThat(first.schemaSemanticsAt(location).isNullable).isNull()
        val initialReads = reads
        assertThat(initialReads).isPositive()
        repeat(20) {
            assertThat(first.schemaSemanticsAt(location).declaredType).isEqualTo("string")
        }
        assertThat(reads).isEqualTo(initialReads)

        val (second, sameLocation) = document("type: [string, 'null']")
        assertThat(sameLocation).isEqualTo(location)
        assertThat(second.schemaSemanticsAt(sameLocation).isNullable).isTrue()
        assertThat(first.schemaSemanticsAt(location).isNullable).isNull()
    }

    @Test
    fun `normalizes OpenAPI 3_0 schema semantics`() {
        val schema = parseSchema("3.0.4", openApi30Schema) as SourceObjectSchema

        assertCommonSemantics(schema)
        assertThat(schema.types).containsExactly(SourceSchemaType.STRING, SourceSchemaType.NULL)
        assertThat(schema.metadata.examples).containsExactly(JsonNodeFactory.instance.textNode("legacy example"))
        assertThat(schema.constraints.minimum).isEqualTo(SourceSchemaBound(BigDecimal("1.5"), true))
        assertThat(schema.constraints.maximum).isEqualTo(SourceSchemaBound(BigDecimal("9.5"), false))
    }

    @ParameterizedTest
    @ValueSource(strings = ["3.1.2", "3.2.0"])
    fun `normalizes JSON Schema 2020-12 semantics`(version: String) {
        val schema = parseSchema(version, jsonSchema202012) as SourceObjectSchema

        assertCommonSemantics(schema)
        assertThat(schema.types).containsExactly(SourceSchemaType.STRING, SourceSchemaType.NULL)
        assertThat(schema.metadata.examples).containsExactly(
            JsonNodeFactory.instance.textNode("first"),
            JsonNodeFactory.instance.textNode("second"),
        )
        assertThat(schema.constraints.minimum).isEqualTo(SourceSchemaBound(BigDecimal("2.5"), true))
        assertThat(schema.constraints.maximum).isEqualTo(SourceSchemaBound(BigDecimal("8.5"), true))
        assertThat(schema.constraints.minContains).isEqualTo(1)
        assertThat(schema.constraints.maxContains).isEqualTo(2)
        assertThat(schema.metadata.constValue).isEqualTo(JsonNodeFactory.instance.textNode("first"))
        assertThat(schema.metadata.contentEncoding).isEqualTo("base64")
        assertThat(schema.metadata.contentMediaType).isEqualTo("application/octet-stream")
        assertThat(schema.dependentRequired).containsEntry("first", linkedSetOf("second", "third"))
    }

    private fun assertCommonSemantics(schema: SourceObjectSchema) {
        assertThat(schema.metadata.title).isEqualTo("A title")
        assertThat(schema.metadata.description).isEqualTo("A description")
        assertThat(schema.metadata.format).isEqualTo("custom")
        assertThat(schema.metadata.defaultValue).isEqualTo(JsonNodeFactory.instance.textNode("default value"))
        assertThat(schema.metadata.enumValues).containsExactly(
            JsonNodeFactory.instance.textNode("first"),
            JsonNodeFactory.instance.numberNode(2),
            JsonNodeFactory.instance.nullNode(),
        )
        assertThat(schema.metadata.readOnly).isTrue()
        assertThat(schema.metadata.writeOnly).isTrue()
        assertThat(schema.metadata.deprecated).isTrue()
        assertThat(schema.metadata.extensions).containsEntry(
            "x-generator-hint",
            JsonNodeFactory.instance.textNode("preserved"),
        )
        assertThat(schema.requiredProperties).containsExactly("first", "second")
        assertThat(schema.discriminator).isEqualTo(
            SourceSchemaDiscriminator("kind", mapOf("cat" to "#/components/schemas/Cat")),
        )
        assertThat(schema.constraints.multipleOf).isEqualByComparingTo("0.5")
        assertThat(schema.constraints.minLength).isEqualTo(2)
        assertThat(schema.constraints.maxLength).isEqualTo(20)
        assertThat(schema.constraints.pattern).isEqualTo("^[a-z]+$")
        assertThat(schema.constraints.minItems).isEqualTo(1)
        assertThat(schema.constraints.maxItems).isEqualTo(5)
        assertThat(schema.constraints.uniqueItems).isTrue()
        assertThat(schema.constraints.minProperties).isEqualTo(1)
        assertThat(schema.constraints.maxProperties).isEqualTo(4)
    }

    private fun parseSchema(
        version: String,
        schema: String,
    ): SourceSchema {
        val document =
            """
            openapi: $version
            info:
              title: Test
              version: "1.0"
            paths: {}
            components:
              schemas:
                Subject:
            """.trimIndent() + "\n" + schema.prependIndent("      ")

        return SourceOpenApiDocumentParser.parse(document).componentSchemas.getValue("Subject")
    }

    private val commonSchema =
        """
        title: A title
        description: A description
        format: custom
        default: default value
        enum: [first, 2, null]
        readOnly: true
        writeOnly: true
        deprecated: true
        x-generator-hint: preserved
        required: [first, second]
        discriminator:
          propertyName: kind
          mapping:
            cat: '#/components/schemas/Cat'
        multipleOf: 0.5
        minLength: 2
        maxLength: 20
        pattern: '^[a-z]+$'
        minItems: 1
        maxItems: 5
        uniqueItems: true
        minProperties: 1
        maxProperties: 4
        """.trimIndent()

    private val openApi30Schema =
        listOf(
            """
            type: string
            nullable: true
            example: legacy example
            minimum: 1.5
            exclusiveMinimum: true
            maximum: 9.5
            exclusiveMaximum: false
            """.trimIndent(),
            commonSchema,
        ).joinToString("\n")

    private val jsonSchema202012 =
        listOf(
            """
            type: [string, 'null']
            examples: [first, second]
            minimum: 1.5
            exclusiveMinimum: 2.5
            maximum: 9.5
            exclusiveMaximum: 8.5
            minContains: 1
            maxContains: 2
            contentEncoding: base64
            contentMediaType: application/octet-stream
            const: first
            dependentRequired:
              first: [second, third]
            """.trimIndent(),
            commonSchema,
        ).joinToString("\n")
}
