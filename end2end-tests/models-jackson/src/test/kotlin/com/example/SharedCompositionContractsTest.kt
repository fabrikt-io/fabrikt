package com.example

import com.example.composition.models.A
import com.example.composition.models.ACompositeExtra
import com.example.composition.models.B
import com.example.composition.models.BComposite
import com.example.composition.models.D
import com.example.composition.models.Other
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.validation.Validation
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator
import org.junit.jupiter.api.Test

class SharedCompositionContractsTest {
    @Test
    fun `explicit compositions expose common fields without treating similar models as members`() {
        val values: List<Any> = listOf(A(id = "a-1"), B(id = "b-1", label = "B"), D(id = "d-1", label = "D"), Other(id = "other"))

        assertThat(values.filterIsInstance<ACompositeExtra>().map { it.id }).containsExactly("a-1", "b-1", "d-1")
        assertThat(values.filterIsInstance<BComposite>().map { it.label }).containsExactly("B", "D")
        assertThat(values.last() as? ACompositeExtra).isNull()
    }

    @Test
    fun `Jackson preserves concrete fields and contract checks after deserialization`() {
        val mapper = jacksonObjectMapper()
        val value = mapper.readValue<B>("""{"id":"b-1","label":"B"}""")
        val common: ACompositeExtra = value

        assertThat(common.id).isEqualTo("b-1")
        assertThat(common.description).isNull()
        val json = mapper.readTree(mapper.writeValueAsString(value))
        assertThat(json.get("id").asText()).isEqualTo("b-1")
        assertThat(json.get("label").asText()).isEqualTo("B")
        assertThat(mapper.readValue<B>(json.toString())).isEqualTo(value)
    }

    @Test
    fun `validation still accepts valid values and reports invalid inherited fields`() {
        Validation.byDefaultProvider().configure().messageInterpolator(ParameterMessageInterpolator()).buildValidatorFactory().use { factory ->
            val validator = factory.validator
            assertThat(validator.validate(B(id = "valid", label = "B"))).isEmpty()
            assertThat(validator.validate(B(id = "x", label = "B")).map { it.propertyPath.toString() }).containsExactly("id")
        }
    }
}
