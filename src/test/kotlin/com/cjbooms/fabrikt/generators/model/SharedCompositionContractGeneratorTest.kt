package com.cjbooms.fabrikt.generators.model

import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.MutableSettings
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SharedCompositionContractGeneratorTest {
    @BeforeEach
    fun reset() {
        MutableSettings.updateSettings()
        ModelNameRegistry.clear()
    }

    @Test
    fun `only unions whose concrete alternatives implement the contract advertise it`() {
        val document = SourceApi(unionSpec).openApi3
        val primaryModels =
            document.schemas.mapValues { (name, _) ->
                if (name in setOf("Safe", "Incompatible", "Mixed")) {
                    TypeSpec.interfaceBuilder(name).addModifiers(KModifier.SEALED).build()
                } else {
                    TypeSpec.classBuilder(name).addProperty(PropertySpec.builder("id", if (name == "C") INT else STRING).build()).build()
                }
            }
        val models =
            SharedCompositionContractGenerator(
                Packages("example"),
                document.schemas.values,
                document.schemas.map { (name, schema) -> schema.jsonReference to primaryModels.getValue(name) }.toMap(),
            ).apply(primaryModels.values.toMutableSet())

        assertThat(
            models
                .single { it.name == "Safe" }
                .superinterfaces.keys
                .map { it.toString() },
        ).contains("example.models.AComposite")
        listOf("Incompatible", "Mixed", "C", "Other").forEach { name ->
            assertThat(
                models
                    .single { it.name == name }
                    .superinterfaces.keys
                    .map { it.toString() },
            ).doesNotContain("example.models.AComposite")
        }
    }

    @Test
    fun `an incompatible generated property does not acquire the shared contract`() {
        val document = SourceApi(spec).openApi3
        val a = TypeSpec.classBuilder("A").addProperty(PropertySpec.builder("id", STRING).build()).build()
        val b = TypeSpec.classBuilder("B").addProperty(PropertySpec.builder("id", INT).build()).build()
        val models =
            SharedCompositionContractGenerator(
                Packages("example"),
                document.schemas.values,
                mapOf(document.schemas.getValue("A").jsonReference to a, document.schemas.getValue("B").jsonReference to b),
            ).apply(mutableSetOf(a, b))

        assertThat(
            models
                .single { it.name == "A" }
                .superinterfaces.keys
                .map { it.toString() },
        ).containsExactly("example.models.AComposite")
        assertThat(models.single { it.name == "B" }.superinterfaces).isEmpty()
        assertThat(
            models
                .single { it.name == "B" }
                .propertySpecs
                .single()
                .type,
        ).isEqualTo(INT)
    }

    @Test
    fun `a non-null generated property can implement a nullable contract without changing its type`() {
        val document = SourceApi(spec).openApi3
        val a = TypeSpec.classBuilder("A").addProperty(PropertySpec.builder("id", STRING.copy(nullable = true)).build()).build()
        val b = TypeSpec.classBuilder("B").addProperty(PropertySpec.builder("id", STRING).build()).build()
        val models =
            SharedCompositionContractGenerator(
                Packages("example"),
                document.schemas.values,
                mapOf(document.schemas.getValue("A").jsonReference to a, document.schemas.getValue("B").jsonReference to b),
            ).apply(mutableSetOf(a, b))
        val property = models.single { it.name == "B" }.propertySpecs.single()

        assertThat(property.type).isEqualTo(STRING)
        assertThat(property.modifiers).contains(KModifier.OVERRIDE)
        assertThat(
            models
                .single { it.name == "AComposite" }
                .propertySpecs
                .single()
                .type,
        ).isEqualTo(STRING.copy(nullable = true))
    }

    private val spec =
        """
        openapi: 3.0.4
        info: {title: Contracts, version: '1'}
        paths: {}
        components:
          schemas:
            A:
              type: object
              properties:
                id: {type: string}
            B:
              type: object
              allOf:
                - ${'$'}ref: '#/components/schemas/A'
        """.trimIndent()

    private val unionSpec =
        """
        openapi: 3.0.4
        info: {title: Union contracts, version: '1'}
        paths: {}
        components:
          schemas:
            A:
              type: object
              properties:
                id: {type: string}
            B:
              type: object
              allOf:
                - ${'$'}ref: '#/components/schemas/A'
            C:
              type: object
              allOf:
                - ${'$'}ref: '#/components/schemas/A'
            D:
              type: object
              allOf:
                - ${'$'}ref: '#/components/schemas/A'
            Other:
              type: object
              properties:
                id: {type: string}
            Safe:
              oneOf:
                - ${'$'}ref: '#/components/schemas/B'
                - ${'$'}ref: '#/components/schemas/D'
            Incompatible:
              oneOf:
                - ${'$'}ref: '#/components/schemas/B'
                - ${'$'}ref: '#/components/schemas/C'
            Mixed:
              oneOf:
                - ${'$'}ref: '#/components/schemas/B'
                - ${'$'}ref: '#/components/schemas/Other'
        """.trimIndent()
}
