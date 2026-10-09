package com.example

import com.example.compositionunions.models.AComposite
import com.example.compositionunions.models.Combined
import com.example.compositionunions.models.CombinedX
import com.example.compositionunions.models.Mixed
import com.example.compositionunions.models.Shared
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SharedCompositionUnionsTest {
    private val json = Json { useArrayPolymorphism = true }

    @Test
    fun `Kotlinx keeps mixed union membership attached to the concrete variant`() {
        val values = listOf(
            json.decodeFromString<Mixed>("""["b",{"kind":"b","id":"b-1","label":"B"}]"""),
            json.decodeFromString<Mixed>("""["c",{"kind":"c","id":"c-1","count":3}]"""),
            json.decodeFromString<Mixed>("""["other",{"id":"other","message":"unrelated"}]"""),
        )

        assertThat(values.filterIsInstance<AComposite>().map { it.id }).containsExactly("b-1", "c-1")
        assertThat(values.last() as? AComposite).isNull()
        assertThat(json.decodeFromString<Mixed>(json.encodeToString(values.first()))).isEqualTo(values.first())
    }

    @Test
    fun `Kotlinx union serialization preserves the shared contract`() {
        val value = json.decodeFromString<Shared>("""["b",{"kind":"b","id":"b-1","label":"B"}]""")

        assertThat(value.id).isEqualTo("b-1")
        assertThat(json.decodeFromString<Shared>(json.encodeToString(value))).isEqualTo(value)
    }

    @Test
    fun `Kotlinx composed union serialization preserves its common fields`() {
        val value: Combined = CombinedX(id = "x-1", x = "X")
        val roundTripped = json.decodeFromString<Combined>(json.encodeToString(value))

        assertThat(roundTripped.id).isEqualTo("x-1")
        assertThat(roundTripped).isInstanceOf(AComposite::class.java)
        assertThat(roundTripped).isEqualTo(value)
    }
}
