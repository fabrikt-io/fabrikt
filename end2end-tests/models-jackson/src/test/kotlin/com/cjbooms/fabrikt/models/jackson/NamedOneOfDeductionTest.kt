package com.cjbooms.fabrikt.models.jackson

import com.cjbooms.fabrikt.models.jackson.Helpers.mapper
import com.example.models.DeductionA
import com.example.models.DeductionB
import com.example.models.DeductionChoice
import com.example.models.DeductionHolder
import com.fasterxml.jackson.databind.JsonMappingException
import jakarta.validation.Validation
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class NamedOneOfDeductionTest {
    private val objectMapper = mapper()

    @Test
    fun `named deduction references retain their interface in fields and collections`() {
        val json = """{"value":{"a":"hello"},"choices":[{"b":2}],"byKey":{"first":{"a":"world"}}}"""
        val result = objectMapper.readValue(json, DeductionHolder::class.java)

        val value: DeductionChoice = result.value
        assertThat(value).isEqualTo(DeductionA(a = "hello"))
        val element: DeductionChoice = result.choices!!.single()
        assertThat(element).isEqualTo(DeductionB(b = 2))
        val mapped: DeductionChoice? = result.byKey!!["first"]
        assertThat(mapped).isEqualTo(DeductionA(a = "world"))
        assertThat(result.optional).isNull()
        assertThat(objectMapper.readValue(objectMapper.writeValueAsString(result), DeductionHolder::class.java))
            .isEqualTo(result)
    }

    @Test
    fun `required references reject null while optional references and collection defaults work`() {
        val result = objectMapper.readValue("""{"value":{"b":2},"optional":null}""", DeductionHolder::class.java)
        assertThat(result.value).isEqualTo(DeductionB(b = 2))
        assertThat(result.choices).isNull()
        assertThat(result.optional).isNull()
        assertThrows<JsonMappingException> {
            objectMapper.readValue("""{"value":null}""", DeductionHolder::class.java)
        }
    }

    @Test
    fun `validation cascades through the corrected named reference`() {
        Validation.byDefaultProvider().configure()
            .messageInterpolator(ParameterMessageInterpolator())
            .buildValidatorFactory().use { factory ->
                val valid = objectMapper.readValue("""{"value":{"a":"hello"}}""", DeductionHolder::class.java)
                assertThat(factory.validator.validate(valid)).isEmpty()
                val invalid = objectMapper.readValue("""{"value":{"a":""}}""", DeductionHolder::class.java)
                assertThat(factory.validator.validate(invalid).map { it.propertyPath.toString() })
                    .containsExactly("value.a")
            }
    }
}
