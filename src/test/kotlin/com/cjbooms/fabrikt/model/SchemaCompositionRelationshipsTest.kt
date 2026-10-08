package com.cjbooms.fabrikt.model

import com.cjbooms.fabrikt.parser.OpenApiDocumentParser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class SchemaCompositionRelationshipsTest {
    @ParameterizedTest
    @ValueSource(strings = ["3.0.4", "3.1.2", "3.2.0"])
    fun `finds direct transitive and nested allOf components by schema identity`(version: String) {
        val document = parse(version)
        val schemas = document.schemas

        assertThat(document.componentReferences("B"))
            .containsExactly(schemas.getValue("A").jsonReference)
        assertThat(document.componentReferences("D"))
            .containsExactlyInAnyOrder(*listOf("A", "B", "C").map { schemas.getValue(it).jsonReference }.toTypedArray())
        assertThat(document.componentReferences("Nested"))
            .containsExactly(schemas.getValue("A").jsonReference)
        assertThat(document.componentReferences("Repeated"))
            .containsExactly(schemas.getValue("A").jsonReference)
        assertThat(document.componentReferences("ViaAlias"))
            .containsExactly(schemas.getValue("A").jsonReference)
        assertThat(document.componentReferences("B"))
            .doesNotContain(schemas.getValue("Other").jsonReference)
        assertThat(document.componentReferences("Distinct"))
            .containsExactlyInAnyOrder(schemas.getValue("A").jsonReference, schemas.getValue("Other").jsonReference)
    }

    @ParameterizedTest
    @ValueSource(strings = ["3.0.4", "3.1.2", "3.2.0"])
    fun `does not infer inclusion from matching fields properties arrays or union alternatives`(version: String) {
        val document = parse(version)

        listOf("A", "Other", "Inline", "Holder", "Array", "MixedUnion", "SharedUnion", "AnyUnion", "Combined").forEach { name ->
            assertThat(document.allOfComponentSchemas(document.schemas.getValue(name)))
                .describedAs(name)
                .isEmpty()
        }
        assertThat(document.componentReferences("CombinedB"))
            .containsExactlyInAnyOrder(document.schemas.getValue("A").jsonReference, document.schemas.getValue("B").jsonReference)
        assertThat(document.componentReferences("CombinedOther"))
            .containsExactlyInAnyOrder(document.schemas.getValue("A").jsonReference, document.schemas.getValue("Other").jsonReference)
    }

    @ParameterizedTest
    @ValueSource(strings = ["3.0.4", "3.1.2", "3.2.0"])
    fun `terminates on composition cycles without treating a schema as its own ancestor`(version: String) {
        val document = parse(version)
        val schemas = document.schemas

        assertThat(document.componentReferences("CycleB"))
            .containsExactlyInAnyOrder(schemas.getValue("CycleC").jsonReference, schemas.getValue("A").jsonReference)
        assertThat(document.allOfComponentSchemas(schemas.getValue("Self"))).isEmpty()
        assertThat(document.componentReferences("CycleC"))
            .containsExactlyInAnyOrder(schemas.getValue("CycleB").jsonReference, schemas.getValue("A").jsonReference)
    }

    private fun OpenApi3Document.componentReferences(name: String): List<String> =
        allOfComponentSchemas(schemas.getValue(name))
            .map { it.jsonReference }

    private fun parse(version: String): OpenApi3Document =
        OpenApiDocumentParser
            .parse(
                """
                openapi: $version
                info:
                  title: Composition relationships
                  version: 1.0.0
                paths: {}
                components:
                  schemas:
                    A:
                      type: object
                      properties:
                        id: {type: string}
                    Other:
                      type: object
                      properties:
                        id: {type: string}
                    B:
                      allOf:
                        - ${'$'}ref: '#/components/schemas/A'
                        - type: object
                          properties:
                            label: {type: string}
                    C:
                      allOf:
                        - ${'$'}ref: '#/components/schemas/A'
                    D:
                      allOf:
                        - ${'$'}ref: '#/components/schemas/B'
                        - ${'$'}ref: '#/components/schemas/C'
                    Distinct:
                      allOf:
                        - ${'$'}ref: '#/components/schemas/A'
                        - ${'$'}ref: '#/components/schemas/Other'
                    Nested:
                      allOf:
                        - allOf:
                            - ${'$'}ref: '#/components/schemas/A'
                    Repeated:
                      allOf:
                        - ${'$'}ref: '#/components/schemas/A'
                        - ${'$'}ref: '#/components/schemas/A'
                    Alias:
                      ${'$'}ref: '#/components/schemas/A'
                    ViaAlias:
                      allOf:
                        - ${'$'}ref: '#/components/schemas/Alias'
                    Inline:
                      allOf:
                        - type: object
                          properties:
                            id: {type: string}
                    Holder:
                      type: object
                      properties:
                        value:
                          ${'$'}ref: '#/components/schemas/A'
                    Array:
                      type: array
                      items:
                        ${'$'}ref: '#/components/schemas/A'
                    MixedUnion:
                      oneOf:
                        - ${'$'}ref: '#/components/schemas/B'
                        - ${'$'}ref: '#/components/schemas/Other'
                    SharedUnion:
                      oneOf:
                        - ${'$'}ref: '#/components/schemas/B'
                        - ${'$'}ref: '#/components/schemas/C'
                    AnyUnion:
                      anyOf:
                        - ${'$'}ref: '#/components/schemas/B'
                        - ${'$'}ref: '#/components/schemas/C'
                    Combined:
                      allOf:
                        - ${'$'}ref: '#/components/schemas/A'
                        - oneOf:
                            - ${'$'}ref: '#/components/schemas/B'
                            - ${'$'}ref: '#/components/schemas/Other'
                    CycleB:
                      allOf:
                        - ${'$'}ref: '#/components/schemas/CycleC'
                        - ${'$'}ref: '#/components/schemas/A'
                    CycleC:
                      allOf:
                        - ${'$'}ref: '#/components/schemas/CycleB'
                    Self:
                      allOf:
                        - ${'$'}ref: '#/components/schemas/Self'
                """.trimIndent(),
            ).asOpenApi3Document()
}
