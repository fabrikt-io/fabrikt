package com.cjbooms.fabrikt.clients

import com.example.composition.models.ACompositeExtra
import com.example.composition.models.B
import com.example.composition.models.D
import com.example.composition.models.Other
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule

class SharedCompositionContractsTest {
    @Test
    fun `Jackson 3 retains contract membership and concrete fields after deserialization`() {
        val mapper = JsonMapper.builder().addModule(kotlinModule()).build()
        val value = mapper.readValue("""{"id":"b-1","label":"B"}""", B::class.java)
        val common: ACompositeExtra = value

        assertThat(common.id).isEqualTo("b-1")
        assertThat(mapper.readValue(mapper.writeValueAsString(value), B::class.java)).isEqualTo(value)
        val values: List<Any> = listOf(value, D(id = "d-1", label = "D"), Other(id = "other"))
        assertThat(values.filterIsInstance<ACompositeExtra>().map { it.id }).containsExactly("b-1", "d-1")
    }
}
