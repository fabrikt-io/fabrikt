package com.cjbooms.fabrikt.models.jackson

import com.example.validation.models.AllOfEnumValue
import com.example.validation.models.AnyOfEnumValue
import com.example.validation.models.DecimalValue
import com.example.validation.models.DirectEnumValue
import com.example.validation.models.InlineEnumValue
import com.example.validation.models.IntegerValue
import com.example.validation.models.ListValue
import com.example.validation.models.NestedListValue
import com.example.validation.models.NestedValue
import com.example.validation.models.StringValue
import com.example.validation.models.UuidValue
import jakarta.validation.Validation
import jakarta.validation.ValidatorFactory
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import com.example.validationstrings.models.UuidValue as UuidStringValue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ModelValidationTest {
    private val mapper = Helpers.mapper()
    private lateinit var factory: ValidatorFactory

    @BeforeAll
    fun openValidator() {
        factory = Validation.byDefaultProvider().configure()
            .messageInterpolator(ParameterMessageInterpolator())
            .buildValidatorFactory()
    }

    @AfterAll
    fun closeValidator() {
        if (::factory.isInitialized) factory.close()
    }

    @ParameterizedTest(name = "{0}: {1}")
    @MethodSource("validModels")
    fun `generated constraints accept valid model values`(type: Class<*>, json: String) {
        val model = mapper.readValue(json, type)

        assertThat(factory.validator.validate(model)).isEmpty()
    }

    @ParameterizedTest(name = "{0}: {1} violates {3} at {2}")
    @MethodSource("invalidModels")
    fun `generated constraints reject invalid values at the expected property`(
        type: Class<*>,
        json: String,
        path: String,
        constraint: String,
    ) {
        val model = mapper.readValue(json, type)

        val violations = factory.validator.validate(model)

        assertThat(violations.map { it.propertyPath.toString() to it.constraintDescriptor.annotation.annotationClass.simpleName })
            .containsExactly(path to constraint)
    }

    companion object {
        @JvmStatic
        fun validModels(): Stream<Arguments> = Stream.of(
            Arguments.of(StringValue::class.java, """{"value":"ready"}"""),
            Arguments.of(IntegerValue::class.java, """{"value":5}"""),
            Arguments.of(DecimalValue::class.java, """{"value":5.5}"""),
            Arguments.of(ListValue::class.java, """{"value":["ready"]}"""),
            Arguments.of(NestedValue::class.java, """{"value":{"value":"ready"}}"""),
            Arguments.of(NestedListValue::class.java, """{"value":[{"value":"ready"}]}"""),
            Arguments.of(DirectEnumValue::class.java, """{"value":"ready"}"""),
            Arguments.of(InlineEnumValue::class.java, """{"value":"ready"}"""),
            Arguments.of(AllOfEnumValue::class.java, """{"value":"ready"}"""),
            Arguments.of(AnyOfEnumValue::class.java, """{"value":"ready"}"""),
            Arguments.of(UuidStringValue::class.java, """{"value":"123e4567-e89b-12d3-a456-426614174000"}"""),
            Arguments.of(UuidValue::class.java, """{"value":"123e4567-e89b-12d3-a456-426614174000"}"""),
        )

        @JvmStatic
        fun invalidModels(): Stream<Arguments> = Stream.of(
            Arguments.of(UuidStringValue::class.java, """{"value":"abc"}""", "value", "Size"),
            Arguments.of(UuidStringValue::class.java, """{"value":"123E4567-E89B-12D3-A456-426614174000"}""", "value", "Pattern"),
            Arguments.of(StringValue::class.java, """{"value":"a"}""", "value", "Size"),
            Arguments.of(StringValue::class.java, """{"value":"abcdef"}""", "value", "Size"),
            Arguments.of(StringValue::class.java, """{"value":"READY"}""", "value", "Pattern"),
            Arguments.of(IntegerValue::class.java, """{"value":0}""", "value", "DecimalMin"),
            Arguments.of(IntegerValue::class.java, """{"value":11}""", "value", "DecimalMax"),
            Arguments.of(DecimalValue::class.java, """{"value":1.5}""", "value", "DecimalMin"),
            Arguments.of(DecimalValue::class.java, """{"value":9.5}""", "value", "DecimalMax"),
            Arguments.of(ListValue::class.java, """{"value":[]}""", "value", "Size"),
            Arguments.of(ListValue::class.java, """{"value":["a","b","c"]}""", "value", "Size"),
            Arguments.of(NestedValue::class.java, """{"value":{"value":"READY"}}""", "value.value", "Pattern"),
            Arguments.of(NestedListValue::class.java, """{"value":[{"value":"READY"}]}""", "value[0].value", "Pattern"),
        )
    }
}
