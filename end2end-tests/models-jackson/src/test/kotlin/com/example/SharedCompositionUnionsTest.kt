package com.example

import com.example.compositionunions.models.AComposite
import com.example.compositionunions.models.Combined
import com.example.compositionunions.models.DeductionChoice
import com.example.compositionunions.models.Mixed
import com.example.compositionunions.models.Shared
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SharedCompositionUnionsTest {
    private val mapper = jacksonObjectMapper()

    @Test
    fun `mixed unions expose the contract only on the appropriate deserialized variants`() {
        val values = listOf(
            mapper.readValue("""{"kind":"b","id":"b-1","label":"B"}""", Mixed::class.java),
            mapper.readValue("""{"kind":"c","id":"c-1","count":3}""", Mixed::class.java),
            mapper.readValue("""{"kind":"other","id":"other","message":"unrelated"}""", Mixed::class.java),
        )

        assertThat(values.filterIsInstance<AComposite>().map { it.id }).containsExactly("b-1", "c-1")
        assertThat(values.last() as? AComposite).isNull()
        assertThat(mapper.readValue(mapper.writeValueAsString(values.first()), Mixed::class.java)).isEqualTo(values.first())
    }

    @Test
    fun `a union whose alternatives share the contract exposes common fields directly`() {
        val value = mapper.readValue("""{"kind":"b","id":"b-1","label":"B"}""", Shared::class.java)

        assertThat(value.id).isEqualTo("b-1")
        assertThat(mapper.readValue(mapper.writeValueAsString(value), Shared::class.java)).isEqualTo(value)
    }

    @Test
    fun `named deduction preserves common fields and contract checks`() {
        val value = mapper.readValue("""{"id":"b-1","b":"B"}""", DeductionChoice::class.java)

        assertThat(value.id).isEqualTo("b-1")
        assertThat(value).isInstanceOf(AComposite::class.java)
        assertThat(mapper.readValue(mapper.writeValueAsString(value), DeductionChoice::class.java)).isEqualTo(value)
    }

    @Test
    fun `composed alternatives expose the included object through their union`() {
        val value = mapper.readValue("""{"id":"x-1","x":"X"}""", Combined::class.java)

        assertThat(value.id).isEqualTo("x-1")
        assertThat(value).isInstanceOf(AComposite::class.java)
        assertThat(mapper.readValue(mapper.writeValueAsString(value), Combined::class.java)).isEqualTo(value)
    }
}
