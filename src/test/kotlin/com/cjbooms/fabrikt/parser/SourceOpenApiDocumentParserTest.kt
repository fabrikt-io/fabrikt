package com.cjbooms.fabrikt.parser

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.InstanceOfAssertFactories.type
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SourceOpenApiDocumentParserTest {
    @ParameterizedTest
    @MethodSource("nullableStringDocuments")
    fun `normalizes equivalent nullable types across OpenAPI versions`(input: String) {
        val document = SourceOpenApiDocumentParser.parse(input)

        assertThat(document.componentSchemas["Value"])
            .asInstanceOf(type(SourceObjectSchema::class.java))
            .extracting(SourceObjectSchema::types)
            .isEqualTo(linkedSetOf(SourceSchemaType.STRING, SourceSchemaType.NULL))
    }

    @Test
    fun `preserves boolean schemas that cannot be represented by Kaizen`() {
        val document =
            SourceOpenApiDocumentParser.parse(
                document(
                    "3.1.2",
                    """
                    AllowsAnything: true
                    AllowsNothing: false
                    """,
                ),
            )

        assertThat(document.componentSchemas["AllowsAnything"])
            .isEqualTo(
                SourceBooleanSchema(
                    location = "#/components/schemas/AllowsAnything",
                    node = document.root.at("/components/schemas/AllowsAnything"),
                    allowsAnyValue = true,
                ),
            )
        assertThat(document.componentSchemas["AllowsNothing"])
            .asInstanceOf(type(SourceBooleanSchema::class.java))
            .extracting(SourceBooleanSchema::allowsAnyValue)
            .isEqualTo(false)
    }

    @Test
    fun `preserves references siblings unknown keywords and escaped source locations`() {
        val input =
            document(
                "3.2.0",
                """
                Value~with/slash:
                  ${'$'}ref: '#/components/schemas/Target'
                  description: Retained reference sibling
                  futureKeyword:
                    nested: true
                """,
            )
        val document =
            SourceOpenApiDocumentParser.parse(input)
        val schema = document.componentSchemas["Value~with/slash"] as SourceObjectSchema

        assertThat(document.content).isEqualTo(input)
        assertThat(schema.location).isEqualTo("#/components/schemas/Value~0with~1slash")
        assertThat(schema.reference).isEqualTo("#/components/schemas/Target")
        assertThat(schema.node["description"].textValue()).isEqualTo("Retained reference sibling")
        assertThat(schema.node.at("/futureKeyword/nested").booleanValue()).isTrue()
    }

    @ParameterizedTest
    @ValueSource(strings = ["3.0.4", "3.1.2", "3.2.0"])
    fun `models nested schema structures recursively across OpenAPI versions`(version: String) {
        val document =
            SourceOpenApiDocumentParser.parse(
                document(
                    version,
                    """
                    Value:
                      type: object
                      properties:
                        child~with/slash:
                          type: array
                          items:
                            oneOf:
                              - type: string
                              - type: integer
                      allOf:
                        - type: object
                      anyOf:
                        - type: string
                      oneOf:
                        - type: number
                      not:
                        type: integer
                      additionalProperties:
                        type: boolean
                    """,
                ),
            )
        val value = document.componentSchemas["Value"] as SourceObjectSchema
        val property = value.properties["child~with/slash"] as SourceObjectSchema
        val items = property.items as SourceObjectSchema

        assertThat(property.location).isEqualTo("#/components/schemas/Value/properties/child~0with~1slash")
        assertThat(property.types).containsExactly(SourceSchemaType.ARRAY)
        assertThat(items.location).isEqualTo("#/components/schemas/Value/properties/child~0with~1slash/items")
        assertThat(items.oneOf.map(SourceSchema::location))
            .containsExactly(
                "#/components/schemas/Value/properties/child~0with~1slash/items/oneOf/0",
                "#/components/schemas/Value/properties/child~0with~1slash/items/oneOf/1",
            )
        assertThat((items.oneOf[0] as SourceObjectSchema).types).containsExactly(SourceSchemaType.STRING)
        assertThat((items.oneOf[1] as SourceObjectSchema).types).containsExactly(SourceSchemaType.INTEGER)
        assertThat(value.allOf.single().location).isEqualTo("#/components/schemas/Value/allOf/0")
        assertThat(value.anyOf.single().location).isEqualTo("#/components/schemas/Value/anyOf/0")
        assertThat((value.anyOf.single() as SourceObjectSchema).types).containsExactly(SourceSchemaType.STRING)
        assertThat(value.oneOf.single().location).isEqualTo("#/components/schemas/Value/oneOf/0")
        assertThat(value.not!!.location).isEqualTo("#/components/schemas/Value/not")
        assertThat((value.not as SourceObjectSchema).types).containsExactly(SourceSchemaType.INTEGER)
        assertThat(value.additionalProperties!!.location).isEqualTo("#/components/schemas/Value/additionalProperties")
        assertThat((value.additionalProperties as SourceObjectSchema).types)
            .containsExactly(SourceSchemaType.BOOLEAN)
    }

    @Suppress("unused")
    private fun nullableStringDocuments(): Stream<String> =
        Stream.of(
            document(
                "3.0.4",
                """
                Value:
                  type: string
                  nullable: true
                """,
            ),
            document(
                "3.1.2",
                """
                Value:
                  type: [string, 'null']
                """,
            ),
            document(
                "3.2.0",
                """
                Value:
                  type: [string, 'null']
                """,
            ),
        )

    private fun document(
        version: String,
        schemas: String,
    ): String =
        """
        openapi: $version
        info:
          title: Test
          version: "1.0"
        paths: {}
        components:
          schemas:
${schemas.trimIndent().prependIndent("            ")}
        """.trimIndent()
}
