package com.cjbooms.fabrikt.parser

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SourceSchemaPolymorphismTest {
    @Test
    fun selectsDescendantsByResolvedIdentity() {
        val document =
            SourceOpenApiDocumentParser.parse(
                """
                openapi: 3.1.0
                components:
                  schemas:
                    Base:
                      type: object
                      discriminator: {propertyName: kind}
                    SameShape:
                      type: object
                      discriminator: {propertyName: kind}
                    Child:
                      allOf: [{${'$'}ref: '#/components/schemas/Base'}]
                    Grandchild:
                      allOf: [{${'$'}ref: '#/components/schemas/Child'}]
                    ChildAlias:
                      ${'$'}ref: '#/components/schemas/Child'
                    OtherChild:
                      allOf: [{${'$'}ref: '#/components/schemas/SameShape'}]
                    Wrapper:
                      properties:
                        value: {${'$'}ref: '#/components/schemas/Base'}
                """.trimIndent(),
            )

        assertThat(SourceSchemaPolymorphism(document).includeSubtypes(setOf("Base")))
            .containsExactly("Base", "Child", "Grandchild")
    }

    @Test
    fun resolvesAliasesAndEscapedPointersWithoutLooping() {
        val document =
            SourceOpenApiDocumentParser.parse(
                """
                openapi: 3.1.0
                components:
                  schemas:
                    Base/Type:
                      type: object
                      discriminator: {propertyName: kind}
                    Alias:
                      ${'$'}ref: '#/components/schemas/Base~1Type'
                    Child:
                      allOf:
                        - {${'$'}ref: '#/components/schemas/Alias'}
                        - {${'$'}ref: '#/components/schemas/Cycle'}
                    Cycle:
                      allOf: [{${'$'}ref: '#/components/schemas/Child'}]
                    UnrelatedCycle:
                      allOf: [{${'$'}ref: '#/components/schemas/UnrelatedCycle'}]
                """.trimIndent(),
            )

        assertThat(SourceSchemaPolymorphism(document).includeSubtypes(setOf("Base/Type")))
            .containsExactly("Base/Type", "Child", "Cycle")
    }

    @Test
    fun leavesOrdinarySelectionsUnchanged() {
        val document =
            SourceOpenApiDocumentParser.parse(
                """
                openapi: 3.0.0
                components:
                  schemas:
                    Base: {type: object}
                    Child:
                      allOf: [{${'$'}ref: '#/components/schemas/Base'}]
                """.trimIndent(),
            )

        assertThat(SourceSchemaPolymorphism(document).includeSubtypes(setOf("Base", "Missing")))
            .containsExactly("Base", "Missing")
    }
}
