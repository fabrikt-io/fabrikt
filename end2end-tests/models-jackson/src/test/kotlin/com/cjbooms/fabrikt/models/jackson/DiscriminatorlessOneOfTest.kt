package com.cjbooms.fabrikt.models.jackson

import com.cjbooms.fabrikt.models.jackson.Helpers.mapper
import com.example.oneof.models.BaseErrorErrorType
import com.example.oneof.models.ErrorWrapper
import com.example.oneof.models.ServerErr
import com.example.oneof.models.ValidationErr
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Demonstrates correct Jackson serde behaviour for a discriminator-less inline oneOf.
 *
 * [ErrorWrapper.error] is typed as [Any]? because [ValidationErr] and [ServerErr] have no
 * discriminator property — Jackson has no way to select the correct subtype during
 * deserialization. A sealed interface [com.example.oneof.models.ErrorWrapperError] is still
 * generated so that [ValidationErr] and [ServerErr] are type-safe to produce, but the field
 * itself is [Any]? so deserialization captures the raw JSON as a [LinkedHashMap] — no data
 * is lost. The correct fix for full round-trip type fidelity is in the spec: add a discriminator.
 */
class DiscriminatorlessOneOfTest {

    private val objectMapper = mapper()

    @Test
    fun `serialization uses runtime type - ValidationErr unique field emitted`() {
        val wrapper = ErrorWrapper(error = ValidationErr(message = "field is required", errorType = BaseErrorErrorType.VALIDATION, fieldName = "username"))

        val json = objectMapper.writeValueAsString(wrapper)

        assertThat(json).contains("\"message\":\"field is required\"")
        assertThat(json).contains("\"errorType\":\"VALIDATION\"")
        assertThat(json).contains("\"fieldName\":\"username\"")
    }

    @Test
    fun `serialization uses runtime type - ServerErr unique field emitted`() {
        val wrapper = ErrorWrapper(error = ServerErr(message = "internal error", errorType = BaseErrorErrorType.SERVER, stackTrace = "at com.example.Service.call(Service.kt:42)"))

        val json = objectMapper.writeValueAsString(wrapper)

        assertThat(json).contains("\"message\":\"internal error\"")
        assertThat(json).contains("\"errorType\":\"SERVER\"")
        assertThat(json).contains("\"stackTrace\":\"at com.example.Service.call(Service.kt:42)\"")
    }

    @Test
    fun `deserialization captures all field values - no data lost`() {
        val json = """{"error":{"message":"field is required","errorType":"VALIDATION","fieldName":"username"}}"""

        val result = objectMapper.readValue(json, ErrorWrapper::class.java)

        @Suppress("UNCHECKED_CAST")
        val errorMap = result.error as Map<String, Any>
        assertThat(errorMap["message"]).isEqualTo("field is required")
        assertThat(errorMap["errorType"]).isEqualTo("VALIDATION")
        assertThat(errorMap["fieldName"]).isEqualTo("username")
    }

    @Test
    fun `round-trip preserves all field values including unique properties`() {
        val original = ErrorWrapper(error = ValidationErr(message = "field is required", errorType = BaseErrorErrorType.VALIDATION, fieldName = "username"))

        val json = objectMapper.writeValueAsString(original)
        val deserialized = objectMapper.readValue(json, ErrorWrapper::class.java)

        @Suppress("UNCHECKED_CAST")
        val errorMap = deserialized.error as Map<String, Any>
        assertThat(errorMap["message"]).isEqualTo("field is required")
        assertThat(errorMap["errorType"]).isEqualTo("VALIDATION")
        assertThat(errorMap["fieldName"]).isEqualTo("username")
    }
}

