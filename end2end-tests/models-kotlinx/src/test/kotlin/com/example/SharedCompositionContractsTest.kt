package com.example

import com.example.composition.models.ACompositeExtra
import com.example.composition.models.B
import com.example.composition.models.D
import com.example.composition.models.Other
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SharedCompositionContractsTest {
    @Test
    fun `Kotlinx preserves concrete models implementing composition contracts`() {
        val value = Json.decodeFromString<B>("""{"id":"b-1","label":"B"}""")
        val common: ACompositeExtra = value

        assertThat(common.id).isEqualTo("b-1")
        assertThat(Json.decodeFromString<B>(Json.encodeToString(value))).isEqualTo(value)
        val values: List<Any> = listOf(value, D(id = "d-1", label = "D"), Other(id = "other"))
        assertThat(values.filterIsInstance<ACompositeExtra>().map { it.id }).containsExactly("b-1", "d-1")
    }
}
