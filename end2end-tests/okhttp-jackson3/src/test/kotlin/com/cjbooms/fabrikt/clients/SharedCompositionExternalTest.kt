package com.cjbooms.fabrikt.clients

import com.example.compositionexternal.models.AComposite
import com.example.compositionexternal.models.AExtraComposite
import com.example.compositionexternal.models.B
import com.example.compositionexternal.models.BaseComposite
import com.example.compositionexternal.models.C
import com.example.compositionexternal.models.D
import com.example.compositionexternal.models.E
import com.example.compositionexternal.models.F
import com.example.compositionexternal.models.G
import com.example.compositionexternal.models.LocalSameShape
import com.example.compositionexternal.models.Mixed
import com.example.compositionexternal.models.Shared
import com.example.compositionexternal.models.StandaloneComposite
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule

class SharedCompositionExternalTest {
    private val mapper = JsonMapper.builder().addModule(kotlinModule()).build()

    @Test
    fun `canonical references share a contract while same-named schemas in other documents stay separate`() {
        val b = mapper.readValue("""{"id":"b-1","label":"B","b":"B"}""", B::class.java)
        val c = mapper.readValue("""{"id":"c-1","label":"C","c":3}""", C::class.java)
        val e = mapper.readValue("""{"id":"e-1","label":"E","e":"E"}""", E::class.java)
        val values: List<Any> = listOf(b, c, D(id = "d-1", label = "D", c = 4, d = true), e, LocalSameShape(id = "other", label = "Other"))

        assertThat(values.filterIsInstance<AComposite>().map { it.id }).containsExactly("b-1", "c-1", "d-1")
        assertThat(values.filterIsInstance<BaseComposite>().map { it.id }).containsExactly("b-1", "c-1", "d-1")
        assertThat(e).isInstanceOf(AExtraComposite::class.java)
        assertThat(values.last() as? AComposite).isNull()
        assertThat(mapper.readValue(mapper.writeValueAsString(b), B::class.java)).isEqualTo(b)
    }

    @Test
    fun `contracts remain typed across external union alternatives`() {
        val shared = mapper.readValue("""{"id":"b-1","label":"B","b":"B"}""", Shared::class.java)
        val mixed = mapper.readValue("""{"id":"e-1","label":"E","e":"E"}""", Mixed::class.java)

        assertThat(shared.id).isEqualTo("b-1")
        assertThat(shared.label).isEqualTo("B")
        assertThat(mixed as? AComposite).isNull()
        assertThat(mapper.readValue(mapper.writeValueAsString(shared), Shared::class.java)).isEqualTo(shared)
    }

    @Test
    fun `standalone schema references expose one contract and retain defaults`() {
        val f = mapper.readValue("""{"id":"f-1","f":"F"}""", F::class.java)
        val g = mapper.readValue("""{"id":"g-1","g":3}""", G::class.java)
        val values: List<Any> = listOf(f, g)

        assertThat(values.filterIsInstance<StandaloneComposite>().map { it.id }).containsExactly("f-1", "g-1")
        assertThat((f as StandaloneComposite).enabled).isTrue()
        assertThat(mapper.readValue(mapper.writeValueAsString(f), F::class.java)).isEqualTo(f)
    }
}
