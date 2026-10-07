package com.cjbooms.fabrikt

import com.example.models.ArrayRequestMediaRequestItem
import com.example.models.CreateRequestMediaRequest
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.validation.Validation
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RequestMediaModelsTest {
    private val mapper = jacksonObjectMapper()

    @Test
    fun `repeated inline request models preserve JSON fields and required properties`() {
        val value = mapper.readValue<CreateRequestMediaRequest>("""{"label":"hello"}""")
        assertThat(value.label).isEqualTo("hello")
        assertThat(mapper.readTree(mapper.writeValueAsString(value))["label"].asText()).isEqualTo("hello")
        assertThrows<MismatchedInputException> { mapper.readValue<CreateRequestMediaRequest>("{}") }
    }

    @Test
    fun `array elements preserve their model and validation constraints`() {
        val values = mapper.readValue<List<ArrayRequestMediaRequestItem>>("""[{"label":"hello"}]""")
        assertThat(values.single().label).isEqualTo("hello")
        val factory = Validation.byDefaultProvider().configure()
            .messageInterpolator(ParameterMessageInterpolator())
            .buildValidatorFactory()
        factory.use {
            val validator = factory.validator
            assertThat(validator.validate(values.single())).isEmpty()
            assertThat(validator.validate(ArrayRequestMediaRequestItem(""))).hasSize(1)
        }
    }
}
