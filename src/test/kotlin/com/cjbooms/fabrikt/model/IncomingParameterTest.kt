package com.cjbooms.fabrikt.model

import com.squareup.kotlinpoet.asTypeName
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class IncomingParameterTest {

    @Test
    fun `required parameter is non-nullable`() {
        val parameter = RequestParameter(
            oasName = "param1",
            description = null,
            type = String::class.asTypeName(),
            isRequired = true,
            originalName = "param1",
            parameterLocation = HeaderParam,
            typeInfo = KotlinTypeInfo.Text,
        )

        assertThat(parameter.toParameterSpecBuilder().build().type.isNullable).isFalse()
    }

    @Test
    fun `optional parameter without default is nullable`() {
        val parameter = RequestParameter(
            oasName = "param1",
            description = null,
            type = String::class.asTypeName(),
            isRequired = false,
            originalName = "param1",
            parameterLocation = HeaderParam,
            typeInfo = KotlinTypeInfo.Text,
        )

        assertThat(parameter.toParameterSpecBuilder().build().type.isNullable).isTrue()
    }

    @Test
    fun `optional parameter with a default value is non-nullable`() {
        val parameter = RequestParameter(
            oasName = "param1",
            description = null,
            type = String::class.asTypeName(),
            isRequired = false,
            originalName = "param1",
            parameterLocation = HeaderParam,
            typeInfo = KotlinTypeInfo.Text,
            defaultValue = "paramValue",
        )

        assertThat(parameter.toParameterSpecBuilder().build().type.isNullable).isFalse()
    }
}
