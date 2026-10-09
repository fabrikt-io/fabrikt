package com.cjbooms.fabrikt.generators.model

import com.cjbooms.fabrikt.configurations.Packages
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
        ModelNameRegistry.clear()
    }

    @Test
    fun `an incompatible generated property does not acquire the shared contract`() {
        val document = SourceApi(spec).openApi3
        val a = TypeSpec.classBuilder("A").addProperty(PropertySpec.builder("id", STRING).build()).build()
        val b = TypeSpec.classBuilder("B").addProperty(PropertySpec.builder("id", INT).build()).build()
        val models =
            SharedCompositionContractGenerator(
                Packages("example"),
                document,
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
                document,
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
              allOf:
                - ${'$'}ref: '#/components/schemas/A'
        """.trimIndent()
}
