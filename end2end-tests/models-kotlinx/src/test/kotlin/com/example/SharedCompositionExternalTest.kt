package com.example

import com.example.compositionexternal.models.AComposite
import com.example.compositionexternal.models.AExtraComposite
import com.example.compositionexternal.models.B
import com.example.compositionexternal.models.BaseComposite
import com.example.compositionexternal.models.C
import com.example.compositionexternal.models.E
import com.example.compositionexternal.models.F
import com.example.compositionexternal.models.G
import com.example.compositionexternal.models.LocalSameShape
import com.example.compositionexternal.models.Mixed
import com.example.compositionexternal.models.Shared
import com.example.compositionexternal.models.StandaloneComposite
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SharedCompositionExternalTest {
    @Test
    fun `Kotlinx retains canonical contract membership after external model deserialization`() {
        val b = Json.decodeFromString<B>("""{"id":"b-1","label":"B","b":"B"}""")
        val c = Json.decodeFromString<C>("""{"id":"c-1","label":"C","c":3}""")
        val e = Json.decodeFromString<E>("""{"id":"e-1","label":"E","e":"E"}""")
        val values: List<Any> = listOf(b, c, e, LocalSameShape(id = "other", label = "Other"))

        assertThat(values.filterIsInstance<AComposite>().map { it.id }).containsExactly("b-1", "c-1")
        assertThat(values.filterIsInstance<BaseComposite>().map { it.id }).containsExactly("b-1", "c-1")
        assertThat(e).isInstanceOf(AExtraComposite::class.java)
        assertThat(Json.decodeFromString<B>(Json.encodeToString(b))).isEqualTo(b)
        val shared: Shared = b
        val roundTripped = Json.decodeFromString<Shared>(Json.encodeToString(shared))
        assertThat(roundTripped.id).isEqualTo("b-1")
        assertThat(roundTripped.label).isEqualTo("B")
        val mixed: Mixed = e
        assertThat(Json.decodeFromString<Mixed>(Json.encodeToString(mixed)) as? AComposite).isNull()
    }

    @Test
    fun `Kotlinx standalone references retain contract identity and defaults`() {
        val f = Json.decodeFromString<F>("""{"id":"f-1","f":"F"}""")
        val g = Json.decodeFromString<G>("""{"id":"g-1","g":3}""")
        val values: List<Any> = listOf(f, g)

        assertThat(values.filterIsInstance<StandaloneComposite>().map { it.id }).containsExactly("f-1", "g-1")
        assertThat((f as StandaloneComposite).enabled).isTrue()
        assertThat(Json.decodeFromString<F>(Json.encodeToString(f))).isEqualTo(f)
    }
}
