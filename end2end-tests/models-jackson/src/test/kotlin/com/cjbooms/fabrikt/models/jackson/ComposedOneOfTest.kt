package com.cjbooms.fabrikt.models.jackson

import com.cjbooms.fabrikt.models.jackson.Helpers.mapper
import com.example.models.ComposedDeducedCombined
import com.example.models.ComposedDeducedHolder
import com.example.models.ComposedNamedHolder
import com.example.models.ComposedNestedHolder
import com.example.models.ComposedSiblingHolder
import com.example.models.ComposedTaggedCombined
import com.example.models.ComposedTaggedHolder
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonMappingException
import jakarta.validation.Validation
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ComposedOneOfTest {
    private val objectMapper = mapper()

    @Test
    fun `undiscriminated combinations preserve the original JSON object`() {
        val holders = listOf(ComposedNestedHolder::class.java, ComposedNamedHolder::class.java, ComposedSiblingHolder::class.java)
        for (reader in listOf(objectMapper, objectMapper.copy().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES))) {
            for (holder in holders) {
                for (json in listOf("""{"value":{"id":"1","a":"hello"}}""", """{"value":{"id":"2","b":3}}""")) {
                    val result = reader.readValue(json, holder)
                    assertThat(reader.readTree(reader.writeValueAsString(result)))
                        .isEqualTo(reader.readTree(json))
                }
            }
        }
    }

    @Test
    fun `deduction selects composed variants in fields lists and maps`() {
        val json = """{"value":{"id":"1","a":"hello"},"values":[{"id":"2","b":3}],"byKey":{"first":{"id":"3","a":"world"}}}"""
        val result = objectMapper.readValue(json, ComposedDeducedHolder::class.java)
        val value: ComposedDeducedCombined = result.value
        val item: ComposedDeducedCombined = result.values!!.single()
        val mapped: ComposedDeducedCombined? = result.byKey!!["first"]
        assertThat(value.javaClass.getMethod("getA").invoke(value)).isEqualTo("hello")
        assertThat(item.javaClass.getMethod("getB").invoke(item)).isEqualTo(3)
        assertThat(mapped!!.javaClass.getMethod("getId").invoke(mapped)).isEqualTo("3")
        assertThat(objectMapper.readTree(objectMapper.writeValueAsString(result)))
            .isEqualTo(objectMapper.readTree(json))
    }

    @Test
    fun `discriminator selects composed variants without changing discriminator values`() {
        val json = """{"value":{"id":"1","kind":"a","a":"hello"},"values":[{"id":"2","kind":"b","b":3}],"byKey":{"first":{"id":"3","kind":"a","a":"world"}}}"""
        val result = objectMapper.readValue(json, ComposedTaggedHolder::class.java)
        val value: ComposedTaggedCombined = result.value
        val item: ComposedTaggedCombined = result.values!!.single()
        assertThat(value.javaClass.getMethod("getA").invoke(value)).isEqualTo("hello")
        assertThat(item.javaClass.getMethod("getB").invoke(item)).isEqualTo(3)
        assertThat(objectMapper.readTree(objectMapper.writeValueAsString(result)))
            .isEqualTo(objectMapper.readTree(json))
        assertThrows<JsonMappingException> {
            objectMapper.readValue("""{"value":{"kind":"a","a":"hello"}}""", ComposedTaggedHolder::class.java)
        }
        assertThrows<JsonMappingException> {
            objectMapper.readValue("""{"value":{"id":"1","kind":"a"}}""", ComposedTaggedHolder::class.java)
        }
    }

    @Test
    fun `common constraints remain active on composed variants`() {
        Validation.byDefaultProvider().configure()
            .messageInterpolator(ParameterMessageInterpolator())
            .buildValidatorFactory().use { factory ->
                val valid = objectMapper.readValue("""{"value":{"id":"1","a":"hello"}}""", ComposedDeducedHolder::class.java)
                assertThat(factory.validator.validate(valid)).isEmpty()
                val invalid = objectMapper.readValue("""{"value":{"id":"","a":"hello"}}""", ComposedDeducedHolder::class.java)
                assertThat(factory.validator.validate(invalid).map { it.propertyPath.toString() })
                    .containsExactly("value.id")
            }
    }
}
