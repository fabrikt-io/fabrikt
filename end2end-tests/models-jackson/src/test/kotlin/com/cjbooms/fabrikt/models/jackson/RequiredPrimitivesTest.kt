package com.cjbooms.fabrikt.models.jackson

import com.example.models.RequiredPrimitives
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

/**
 * Tests for issue #489: Required primitives are not actually required.
 *
 * When Jackson deserializes missing fields for primitive types (Int, Long, Double, Float, Boolean),
 * it assigns JVM defaults (0, 0L, 0.0, 0.0f, false) instead of throwing an exception.
 * This is because JVM primitives cannot be null, so there's no null value flowing through
 * jackson-module-kotlin's null checks.
 *
 * The fix adds `required = true` to `@param:JsonProperty` for required primitive fields,
 * which tells Jackson to check for field presence before deserialization.
 */
class RequiredPrimitivesTest {
    private val objectMapper: ObjectMapper = ObjectMapper()
        .registerModule(KotlinModule.Builder().build())

    @Test
    fun `must deserialize when all required fields are present`() {
        val json = """
            {
                "required_string": "test",
                "required_int": 42,
                "required_long": 123456789,
                "required_double": 3.14,
                "required_float": 2.5,
                "required_boolean": true
            }
        """.trimIndent()

        val result = objectMapper.readValue<RequiredPrimitives>(json)

        assertThat(result.requiredString).isEqualTo("test")
        assertThat(result.requiredInt).isEqualTo(42)
        assertThat(result.requiredLong).isEqualTo(123456789L)
        assertThat(result.requiredDouble).isEqualTo(3.14)
        assertThat(result.requiredFloat).isEqualTo(2.5f)
        assertThat(result.requiredBoolean).isTrue()
        assertThat(result.optionalInt).isNull()
    }

    @Test
    fun `must fail when required int is missing`() {
        val json = """
            {
                "required_string": "test",
                "required_long": 123456789,
                "required_double": 3.14,
                "required_float": 2.5,
                "required_boolean": true
            }
        """.trimIndent()

        assertThatThrownBy { objectMapper.readValue<RequiredPrimitives>(json) }
            .isInstanceOf(JsonMappingException::class.java)
            .hasMessageContaining("required_int")
    }

    @Test
    fun `must fail when required long is missing`() {
        val json = """
            {
                "required_string": "test",
                "required_int": 42,
                "required_double": 3.14,
                "required_float": 2.5,
                "required_boolean": true
            }
        """.trimIndent()

        assertThatThrownBy { objectMapper.readValue<RequiredPrimitives>(json) }
            .isInstanceOf(JsonMappingException::class.java)
            .hasMessageContaining("required_long")
    }

    @Test
    fun `must fail when required double is missing`() {
        val json = """
            {
                "required_string": "test",
                "required_int": 42,
                "required_long": 123456789,
                "required_float": 2.5,
                "required_boolean": true
            }
        """.trimIndent()

        assertThatThrownBy { objectMapper.readValue<RequiredPrimitives>(json) }
            .isInstanceOf(JsonMappingException::class.java)
            .hasMessageContaining("required_double")
    }

    @Test
    fun `must fail when required float is missing`() {
        val json = """
            {
                "required_string": "test",
                "required_int": 42,
                "required_long": 123456789,
                "required_double": 3.14,
                "required_boolean": true
            }
        """.trimIndent()

        assertThatThrownBy { objectMapper.readValue<RequiredPrimitives>(json) }
            .isInstanceOf(JsonMappingException::class.java)
            .hasMessageContaining("required_float")
    }

    @Test
    fun `must fail when required boolean is missing`() {
        val json = """
            {
                "required_string": "test",
                "required_int": 42,
                "required_long": 123456789,
                "required_double": 3.14,
                "required_float": 2.5
            }
        """.trimIndent()

        assertThatThrownBy { objectMapper.readValue<RequiredPrimitives>(json) }
            .isInstanceOf(JsonMappingException::class.java)
            .hasMessageContaining("required_boolean")
    }

    @Test
    fun `must fail when required string is missing`() {
        val json = """
            {
                "required_int": 42,
                "required_long": 123456789,
                "required_double": 3.14,
                "required_float": 2.5,
                "required_boolean": true
            }
        """.trimIndent()

        assertThatThrownBy { objectMapper.readValue<RequiredPrimitives>(json) }
            .isInstanceOf(JsonMappingException::class.java)
            .hasMessageContaining("required_string")
    }
}
