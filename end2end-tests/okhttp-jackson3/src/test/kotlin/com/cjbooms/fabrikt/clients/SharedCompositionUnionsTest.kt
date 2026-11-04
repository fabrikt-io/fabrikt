package com.cjbooms.fabrikt.clients

import com.example.compositionunions.models.AComposite
import com.example.compositionunions.models.InlineContainer
import com.example.compositionunions.models.Combined
import com.example.compositionunions.models.DeductionChoice
import com.example.compositionunions.models.Mixed
import com.example.compositionunions.models.Shared
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule

class SharedCompositionUnionsTest {
    @Test
    fun `inline unions and composed objects retain typed contracts after deserialization`() {
        val payload = """{"named":{"kind":"b","id":"b-4","label":"B"},"direct":{"kind":"b","id":"b-1","label":"B"},"entries":[{"kind":"c","id":"c-1","count":3}],"nested":{"choice":{"id":"a-nested","note":"N"}},"composed":{"id":"a-1","note":"N"},"rows":[{"id":"a-2","note":"R"}],"alias":[{"kind":"b","id":"b-3","label":"B"}],"records":{"one":{"id":"a-3","note":"M"}},"mixed":{"kind":"other","id":"other-1","message":"O"}}"""
        val value = mapper.readValue(payload, InlineContainer::class.java)
        val contracts: List<AComposite> = listOf(value.direct!!, value.entries!!.single(), value.nested!!.choice!!, value.composed!!, value.rows!!.single(), value.alias!!.single(), value.records!!.getValue("one")!!, value.named!!)
        assertThat(contracts.map { it.id }).containsExactly("b-1", "c-1", "a-nested", "a-1", "a-2", "b-3", "a-3", "b-4")
        assertThat(value.mixed as? AComposite).isNull()
        assertThat(mapper.readValue(mapper.writeValueAsString(value), InlineContainer::class.java)).isEqualTo(value)
    }

    private val mapper = JsonMapper.builder().addModule(kotlinModule()).build()

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
