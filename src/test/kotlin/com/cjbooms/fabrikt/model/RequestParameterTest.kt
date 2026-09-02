package com.cjbooms.fabrikt.model

import com.squareup.kotlinpoet.ClassName
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class RequestParameterTest {
    @Test
    fun `supports scalar cookie parameters`() {
        assertThatCode { requestParameter(KotlinTypeInfo.Text) }.doesNotThrowAnyException()
    }

    @Test
    fun `rejects cookie parameters that cannot be serialized safely`() {
        assertThatThrownBy { requestParameter(KotlinTypeInfo.Object("Preferences")) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Cookie parameter 'preferences' has an unsupported type")
    }

    private fun requestParameter(typeInfo: KotlinTypeInfo) =
        RequestParameter(
            oasName = "preferences",
            description = null,
            type = ClassName("example", "Preferences"),
            originalName = "preferences",
            parameterLocation = CookieParam,
            typeInfo = typeInfo,
        )
}
