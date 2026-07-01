package com.cjbooms.fabrikt.models.kotlinx

import com.example.openenum.models.CloseEnum
import com.example.openenum.models.OpenEnum
import com.example.openenum.models.SomeObj
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * End-to-end verification of the FAULT_TOLERANT_OPEN_ENUMS option (fabrikt issue #374)
 * with kotlinx.serialization. The "open enum" `anyOf` is generated as a fault-tolerant enum: the declared
 * values plus an UNRECOGNIZED fallback that `fromValue` returns for any unmapped input.
 */
class KotlinxSerializationOpenEnumTest {

    @Test
    fun `serializes known open enum value as its underlying string`() {
        val obj = SomeObj(openEnum = OpenEnum.BAR, closeEnum = CloseEnum.BAR)
        val json = Json.encodeToString(obj)
        assertThat(json).isEqualTo("""{"open_enum":"bar","close_enum":"bar"}""")
    }

    @Test
    fun `deserializes known open enum value`() {
        val obj = Json.decodeFromString<SomeObj>("""{"open_enum":"foo","close_enum":"foo"}""")
        assertThat(obj.openEnum).isEqualTo(OpenEnum.FOO)
        assertThat(obj.closeEnum).isEqualTo(CloseEnum.FOO)
    }

    @Test
    fun `fromValue maps known values and falls back to UNRECOGNIZED for the rest`() {
        assertThat(OpenEnum.fromValue("baz")).isEqualTo(OpenEnum.BAZ)
        assertThat(OpenEnum.fromValue("nope")).isEqualTo(OpenEnum.UNRECOGNIZED)
    }
}
