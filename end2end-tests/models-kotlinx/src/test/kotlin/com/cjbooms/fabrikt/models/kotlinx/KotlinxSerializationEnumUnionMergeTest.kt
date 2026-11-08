package com.cjbooms.fabrikt.models.kotlinx

import com.example.models.Animal
import com.example.models.Farm
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KotlinxSerializationEnumUnionMergeTest {
    @Test
    fun `an array of the union enum compiles and round-trips its values`() {
        // The union is referenced only through array items, so the enum must be emitted from the
        // schema list rather than from a property holder.
        val farm = Json.decodeFromString<Farm>("""{"animals":["Moo","Baa"]}""")

        assertThat(farm.animals).containsExactly(Animal.MOO, Animal.BAA)
        assertThat(Json.encodeToString(farm)).isEqualTo("""{"animals":["Moo","Baa"]}""")
    }
}
