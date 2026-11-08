package com.cjbooms.fabrikt.models.jackson

import com.cjbooms.fabrikt.models.jackson.Helpers.mapper
import com.example.oneof.models.Animal
import com.example.oneof.models.Farm
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EnumUnionMergeTest {
    private val objectMapper = mapper()

    @Test
    fun `oneOf of enums becomes a union enum and round-trips its value`() {
        val farm = objectMapper.readValue("""{"animal":"Moo"}""", Farm::class.java)

        assertThat(farm.animal).isEqualTo(Animal.MOO)
        assertThat(objectMapper.readValue(objectMapper.writeValueAsString(farm), Farm::class.java)).isEqualTo(farm)
    }
}
