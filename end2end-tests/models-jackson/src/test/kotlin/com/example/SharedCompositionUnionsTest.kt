package com.example

import com.example.compositionunions.models.B
import com.example.compositionunions.models.AComposite
import com.example.compositionunions.models.DeductionB
import com.example.compositionunions.models.DeductionC
import com.example.compositionunions.models.InlineContainerMatrix
import com.example.compositionunions.models.InlineContainer
import com.example.compositionunions.models.Combined
import com.example.compositionunions.models.DeductionChoice
import com.example.compositionunions.models.Mixed
import com.example.compositionunions.models.Shared
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException
import org.junit.jupiter.api.assertThrows
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

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

    private val mapper = jacksonObjectMapper()

    @Test
    fun `inline union nested under two array levels keeps its members typed`() {
        val payload = """{"matrix":[[{"kind":"c","id":"c-1","count":3}],[{"kind":"b","id":"b-1","label":"B"}]]}"""
        val value = mapper.readValue(payload, InlineContainer::class.java)
        val matrix = requireNotNull(value.matrix)

        assertThat(matrix.map { it.single() }).containsExactly(
            mapper.readValue("""{"kind":"c","id":"c-1","count":3}""", InlineContainerMatrix::class.java),
            mapper.readValue("""{"kind":"b","id":"b-1","label":"B"}""", InlineContainerMatrix::class.java),
        )
        assertThat(matrix.flatten()).allMatch { it is AComposite }
        assertThat(mapper.readValue(mapper.writeValueAsString(value), InlineContainer::class.java)).isEqualTo(value)
    }

    @Test
    fun `deducted array item deserializes to the variant with its required field`() {
        val payload = """{"deduced":[{"id":"d-1","b":"B"},{"id":"d-2","c":2}]}"""
        val value = mapper.readValue(payload, InlineContainer::class.java)
        val deduced = requireNotNull(value.deduced)

        assertThat(deduced[0]).isInstanceOf(DeductionB::class.java)
        assertThat(deduced[1]).isInstanceOf(DeductionC::class.java)
        assertThat(mapper.readValue(mapper.writeValueAsString(value), InlineContainer::class.java)).isEqualTo(value)
    }

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

    @Test
    fun `composed oneOf variant defaults discriminator in Kotlin constructor but requires it in JSON deserialization`() {
        val b = B(id = "b-1", label = "L")
        assertThat(b).isInstanceOf(AComposite::class.java)

        val json = mapper.writeValueAsString(b)
        assertThat(json).contains(""""kind":"b"""")

        val deserialized = mapper.readValue(json, Mixed::class.java)
        assertThat(deserialized).isEqualTo(b)

        assertThrows<InvalidTypeIdException> {
            mapper.readValue("""{"id":"b-1","label":"L"}""", Mixed::class.java)
        }
    }
}
