package com.cjbooms.fabrikt.generators.model

import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.MutableSettings
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.MUTABLE_LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

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

    @ParameterizedTest
    @MethodSource("nativeTypes")
    fun `contract membership follows Kotlin subtyping rather than schema primitive names`(case: NativeTypes) {
        val document = SourceApi(spec).openApi3
        val petType = ClassName("example.models", "Pet")
        val cat = TypeSpec.classBuilder("Cat").superclass(petType).build()
        val pet = TypeSpec.classBuilder("Pet").build()
        val a = TypeSpec.classBuilder("A").addProperty(PropertySpec.builder("id", case.expected).build()).build()
        val b = TypeSpec.classBuilder("B").addProperty(PropertySpec.builder("id", case.actual).build()).build()
        val models =
            SharedCompositionContractGenerator(
                Packages("example"),
                document.schemas.values,
                mapOf(
                    document.schemas.getValue("A").jsonReference to a,
                    document.schemas.getValue("B").jsonReference to b,
                    "pet" to pet,
                    "cat" to cat,
                ),
            ).apply(mutableSetOf(a, b, pet, cat))
        val generated = models.single { it.name == "B" }
        assertThat(generated.superinterfaces.keys.any { it.toString() == "example.models.AComposite" }).isEqualTo(case.compatible)
        assertThat(generated.propertySpecs.single().type).isEqualTo(case.actual)
    }

    data class NativeTypes(
        val actual: TypeName,
        val expected: TypeName,
        val compatible: Boolean,
    )

    companion object {
        @JvmStatic
        fun nativeTypes(): Stream<NativeTypes> {
            val pet = ClassName("example.models", "Pet")
            val cat = ClassName("example.models", "Cat")
            return Stream.of(
                NativeTypes(cat, pet, true),
                NativeTypes(pet, cat, false),
                NativeTypes(ClassName("other.models", "Cat"), pet, false),
                NativeTypes(cat.copy(nullable = true), pet, false),
                NativeTypes(cat, pet.copy(nullable = true), true),
                NativeTypes(LIST.parameterizedBy(cat), LIST.parameterizedBy(pet), true),
                NativeTypes(LIST.parameterizedBy(cat.copy(nullable = true)), LIST.parameterizedBy(pet), false),
                NativeTypes(MAP.parameterizedBy(STRING, cat), MAP.parameterizedBy(STRING, pet), true),
                NativeTypes(MAP.parameterizedBy(cat, STRING), MAP.parameterizedBy(pet, STRING), false),
                NativeTypes(MUTABLE_LIST.parameterizedBy(cat), MUTABLE_LIST.parameterizedBy(pet), false),
                NativeTypes(INT, STRING, false),
            )
        }
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
