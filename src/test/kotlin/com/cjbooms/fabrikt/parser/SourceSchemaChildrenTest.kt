package com.cjbooms.fabrikt.parser

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class SourceSchemaChildrenTest {
    @ParameterizedTest
    @ValueSource(strings = ["3.1.2", "3.2.0"])
    fun `models and indexes JSON Schema 2020-12 child schemas`(version: String) {
        val document =
            SourceOpenApiDocumentParser.parse(
                """
                openapi: $version
                info:
                  title: Test
                  version: "1.0"
                paths: {}
                components:
                  schemas:
                    Root:
                      type: object
                      ${'$'}defs:
                        Named/with~escape:
                          type: object
                          properties:
                            nested:
                              type: string
                      properties:
                        direct:
                          type: string
                      patternProperties:
                        pattern:
                          type: integer
                      dependentSchemas:
                        feature/with~escape:
                          type: object
                      prefixItems:
                        - type: string
                        - false
                      items:
                        type: number
                      contains: true
                      propertyNames:
                        type: string
                      if:
                        type: object
                      then:
                        type: string
                      else:
                        type: integer
                      allOf:
                        - type: object
                      anyOf:
                        - type: string
                      oneOf:
                        - type: boolean
                      not:
                        type: 'null'
                      additionalProperties:
                        type: string
                      unevaluatedItems:
                        type: number
                      unevaluatedProperties: false
                      contentSchema:
                        type: object
                """.trimIndent(),
            )
        val root = document.componentSchemas.getValue("Root") as SourceObjectSchema
        val rootLocation = "#/components/schemas/Root"

        assertThat(root.definitions).containsOnlyKeys("Named/with~escape")
        assertThat(root.properties).containsOnlyKeys("direct")
        assertThat(root.patternProperties).containsOnlyKeys("pattern")
        assertThat(root.dependentSchemas).containsOnlyKeys("feature/with~escape")
        assertThat(root.prefixItems).hasSize(2)
        assertThat((root.prefixItems[1] as SourceBooleanSchema).allowsAnyValue).isFalse()
        assertThat((root.contains as SourceBooleanSchema).allowsAnyValue).isTrue()
        assertThat((root.unevaluatedProperties as SourceBooleanSchema).allowsAnyValue).isFalse()

        assertThat(document.schemasByLocation.keys)
            .containsExactlyInAnyOrder(
                rootLocation,
                "$rootLocation/${'$'}defs/Named~1with~0escape",
                "$rootLocation/${'$'}defs/Named~1with~0escape/properties/nested",
                "$rootLocation/properties/direct",
                "$rootLocation/patternProperties/pattern",
                "$rootLocation/dependentSchemas/feature~1with~0escape",
                "$rootLocation/prefixItems/0",
                "$rootLocation/prefixItems/1",
                "$rootLocation/items",
                "$rootLocation/contains",
                "$rootLocation/propertyNames",
                "$rootLocation/if",
                "$rootLocation/then",
                "$rootLocation/else",
                "$rootLocation/allOf/0",
                "$rootLocation/anyOf/0",
                "$rootLocation/oneOf/0",
                "$rootLocation/not",
                "$rootLocation/additionalProperties",
                "$rootLocation/unevaluatedItems",
                "$rootLocation/unevaluatedProperties",
                "$rootLocation/contentSchema",
            )

        root.childSchemas().forEach { child ->
            assertThat(document.schemasByLocation[child.location]).isSameAs(child)
        }
    }
}
