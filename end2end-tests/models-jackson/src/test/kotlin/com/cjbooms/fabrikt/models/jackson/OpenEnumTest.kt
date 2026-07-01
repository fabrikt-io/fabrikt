package com.cjbooms.fabrikt.models.jackson

import com.cjbooms.fabrikt.models.jackson.Helpers.mapper
import com.example.openenum.models.CloseEnum
import com.example.openenum.models.OpenEnum
import com.example.openenum.models.SomeObj
import com.fasterxml.jackson.databind.DeserializationFeature
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * End-to-end verification of the FAULT_TOLERANT_OPEN_ENUMS option (fabrikt issue #374)
 * with Jackson. The "open enum" `anyOf` is generated as a fault-tolerant enum: the declared values plus an
 * UNRECOGNIZED fallback. `fromValue` returns UNRECOGNIZED for unmapped input, and Jackson deserializes
 * unknown values to UNRECOGNIZED once READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE is enabled.
 */
class OpenEnumTest {
    private val objectMapper = mapper()

    @Test
    fun `serializes known open enum value as its underlying string`() {
        val obj = SomeObj(openEnum = OpenEnum.BAR, closeEnum = CloseEnum.BAR)
        val json = objectMapper.writeValueAsString(obj)
        assertThat(json).isEqualTo("""{"open_enum":"bar","close_enum":"bar"}""")
    }

    @Test
    fun `deserializes known open enum value`() {
        val obj = objectMapper.readValue("""{"open_enum":"foo","close_enum":"foo"}""", SomeObj::class.java)
        assertThat(obj.openEnum).isEqualTo(OpenEnum.FOO)
        assertThat(obj.closeEnum).isEqualTo(CloseEnum.FOO)
    }

    @Test
    fun `deserializes unknown open enum value to UNRECOGNIZED when configured to`() {
        val faultTolerantMapper = mapper()
            .enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
        val obj = faultTolerantMapper.readValue("""{"open_enum":"qux","close_enum":"foo"}""", SomeObj::class.java)
        assertThat(obj.openEnum).isEqualTo(OpenEnum.UNRECOGNIZED)
    }

    @Test
    fun `fromValue maps known values and falls back to UNRECOGNIZED for the rest`() {
        assertThat(OpenEnum.fromValue("baz")).isEqualTo(OpenEnum.BAZ)
        assertThat(OpenEnum.fromValue("nope")).isEqualTo(OpenEnum.UNRECOGNIZED)
    }
}
