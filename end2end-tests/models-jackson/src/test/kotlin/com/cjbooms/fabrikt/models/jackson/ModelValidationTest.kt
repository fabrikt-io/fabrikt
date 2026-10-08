package com.cjbooms.fabrikt.models.jackson

import com.example.validation.models.AllOfEnumValue
import com.example.validation.models.AnyOfEnumValue
import com.example.validation.models.ConstrainedEnum
import com.example.validation.models.DecimalValue
import com.example.validation.models.DirectEnumValue
import com.example.validation.models.InlineEnumValue
import com.example.validation.models.InlineEnumValueValue
import com.example.validation.models.IntegerValue
import com.example.validation.models.ListValue
import com.example.validation.models.MapValue
import com.example.validation.models.NestedListValue
import com.example.validation.models.NestedSetValue
import com.example.validation.models.NestedValue
import com.example.validation.models.Status
import com.example.validation.models.StringValue
import com.example.validation.models.TypedMapValue
import com.example.validation.models.UuidValue
import com.example.validation.models.ValueValue
import jakarta.validation.Validation
import jakarta.validation.ValidatorFactory
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import java.util.UUID
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import kotlin.reflect.KClass
import com.example.validationstrings.models.UuidValue as UuidStringValue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ModelValidationTest {
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

    @Test
    fun `string constraints accept a valid string`() {
        val model = StringValue(value = "ready")

        assertThat(factory.validator.validate(model)).isEmpty()
    }

    @Test
    fun `integer bounds accept an interior value`() {
        val model = IntegerValue(value = 5)

        assertThat(factory.validator.validate(model)).isEmpty()
    }

    @Test
    fun `decimal bounds accept an interior value`() {
        val model = DecimalValue(value = BigDecimal("5.5"))

        assertThat(factory.validator.validate(model)).isEmpty()
    }

    @Test
    fun `list size accepts a valid list`() {
        val model = ListValue(value = listOf("ready"))

        assertThat(factory.validator.validate(model)).isEmpty()
    }

    @Test
    fun `nested object validation accepts a valid child`() {
        val model = NestedValue(value = StringValue(value = "ready"))

        assertThat(factory.validator.validate(model)).isEmpty()
    }

    @Test
    fun `nested list validation accepts valid children`() {
        val model = NestedListValue(value = listOf(StringValue(value = "ready")))

        assertThat(factory.validator.validate(model)).isEmpty()
    }

    @Test
    fun `referenced enum with string constraints validates without exceptions`() {
        val model = DirectEnumValue(value = ConstrainedEnum.READY)

        assertThat(factory.validator.validate(model)).isEmpty()
    }

    @Test
    fun `inline enum with string constraints validates without exceptions`() {
        val model = InlineEnumValue(value = InlineEnumValueValue.READY)

        assertThat(factory.validator.validate(model)).isEmpty()
    }

    @Test
    fun `allOf enum with string constraints validates without exceptions`() {
        val model = AllOfEnumValue(value = Status.READY)

        assertThat(factory.validator.validate(model)).isEmpty()
    }

    @Test
    fun `anyOf enum with string constraints validates without exceptions`() {
        val model = AnyOfEnumValue(value = Status.READY)

        assertThat(factory.validator.validate(model)).isEmpty()
    }

    @Test
    fun `UUID as string accepts a valid value`() {
        val model = UuidStringValue(value = "123e4567-e89b-12d3-a456-426614174000")

        assertThat(factory.validator.validate(model)).isEmpty()
    }

    @Test
    fun `UUID with string constraints validates without exceptions`() {
        val model = UuidValue(value = UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))

        assertThat(factory.validator.validate(model)).isEmpty()
    }

    @Test
    fun `UUID as string enforces minimum length`() {
        assertViolation(
            model = UuidStringValue(value = "abc"),
            propertyPath = "value",
            constraint = Size::class,
        )
    }

    @Test
    fun `UUID as string enforces its pattern`() {
        assertViolation(
            model = UuidStringValue(value = "123E4567-E89B-12D3-A456-426614174000"),
            propertyPath = "value",
            constraint = Pattern::class,
        )
    }

    @Test
    fun `string enforces minimum length`() {
        assertViolation(
            model = StringValue(value = "a"),
            propertyPath = "value",
            constraint = Size::class,
        )
    }

    @Test
    fun `string enforces maximum length`() {
        assertViolation(
            model = StringValue(value = "abcdef"),
            propertyPath = "value",
            constraint = Size::class,
        )
    }

    @Test
    fun `string pattern rejects uppercase letters`() {
        assertViolation(
            model = StringValue(value = "READY"),
            propertyPath = "value",
            constraint = Pattern::class,
        )
    }

    @Test
    fun `integer enforces its lower bound`() {
        assertViolation(
            model = IntegerValue(value = 0),
            propertyPath = "value",
            constraint = DecimalMin::class,
        )
    }

    @Test
    fun `integer enforces its upper bound`() {
        assertViolation(
            model = IntegerValue(value = 11),
            propertyPath = "value",
            constraint = DecimalMax::class,
        )
    }

    @Test
    fun `decimal excludes its lower bound`() {
        assertViolation(
            model = DecimalValue(value = BigDecimal("1.5")),
            propertyPath = "value",
            constraint = DecimalMin::class,
        )
    }

    @Test
    fun `decimal excludes its upper bound`() {
        assertViolation(
            model = DecimalValue(value = BigDecimal("9.5")),
            propertyPath = "value",
            constraint = DecimalMax::class,
        )
    }

    @Test
    fun `list enforces minimum size`() {
        assertViolation(
            model = ListValue(value = emptyList()),
            propertyPath = "value",
            constraint = Size::class,
        )
    }

    @Test
    fun `list enforces maximum size`() {
        assertViolation(
            model = ListValue(value = listOf("a", "b", "c")),
            propertyPath = "value",
            constraint = Size::class,
        )
    }

    @Test
    fun `nested object validation reports the child property`() {
        assertViolation(
            model = NestedValue(value = StringValue(value = "READY")),
            propertyPath = "value.value",
            constraint = Pattern::class,
        )
    }

    @Test
    fun `nested list validation reports the child index and property`() {
        assertViolation(
            model = NestedListValue(value = listOf(StringValue(value = "READY"))),
            propertyPath = "value[0].value",
            constraint = Pattern::class,
        )
    }

    @Test
    fun `nested set validation reports the child property`() {
        assertViolation(
            model = NestedSetValue(value = linkedSetOf(StringValue(value = "READY"))),
            propertyPath = "value[].value",
            constraint = Pattern::class,
        )
    }

    @Test
    fun `map value validation reports the child property`() {
        assertViolation(
            model = MapValue(value = mapOf("first" to StringValue(value = "READY"))),
            propertyPath = "value[first].value",
            constraint = Pattern::class,
        )
    }

    @Test
    fun `nested set validation accepts valid children`() {
        val model = NestedSetValue(value = linkedSetOf(StringValue(value = "ready")))

        assertThat(factory.validator.validate(model)).isEmpty()
    }

    @Test
    fun `map value validation accepts valid children`() {
        val model = MapValue(value = mapOf("first" to StringValue(value = "ready")))

        assertThat(factory.validator.validate(model)).isEmpty()
    }

    @Test
    fun `typed map value validation reports the child property`() {
        assertViolation(
            model = TypedMapValue(value = mapOf("first" to ValueValue(text = "READY"))),
            propertyPath = "value[first].text",
            constraint = Pattern::class,
        )
    }

    @Test
    fun `typed map value validation accepts valid children`() {
        val model = TypedMapValue(value = mapOf("first" to ValueValue(text = "ready")))

        assertThat(factory.validator.validate(model)).isEmpty()
    }

    @Test
    fun `generated models do not use the deprecated container level Valid`() {
        // A fresh factory forces metadata building, which is when HV000271 is reported.
        val warnings =
            captureHibernateWarnings {
                freshValidator().use { validating ->
                    validating.validator.validate(
                        NestedListValue(value = listOf(StringValue(value = "ready"))),
                    )
                    validating.validator.validate(
                        NestedSetValue(value = linkedSetOf(StringValue(value = "ready"))),
                    )
                    validating.validator.validate(
                        MapValue(value = mapOf("first" to StringValue(value = "ready"))),
                    )
                    validating.validator.validate(
                        TypedMapValue(value = mapOf("first" to ValueValue(text = "ready"))),
                    )
                }
            }

        assertThat(warnings)
            .describedAs("container-level @Valid deprecation warnings")
            .noneMatch { it.contains("HV000271") }
    }

    @Test
    fun `the deprecated container level Valid is detected`() {
        // Positive control: proves the capture above would fail if fabrikt regressed to `@Valid List<T>`.
        val warnings =
            captureHibernateWarnings {
                freshValidator().use { validating ->
                    validating.validator.validate(LegacyContainerValidModel())
                }
            }

        assertThat(warnings).anyMatch { it.contains("HV000271") }
    }

    private fun freshValidator(): ValidatorFactory =
        Validation.byDefaultProvider().configure()
            .messageInterpolator(ParameterMessageInterpolator())
            .buildValidatorFactory()

    private inline fun captureHibernateWarnings(block: () -> Unit): List<String> {
        val messages = mutableListOf<String>()
        val logger = Logger.getLogger("org.hibernate.validator")
        val handler =
            object : Handler() {
                override fun publish(record: LogRecord) {
                    messages += record.message
                }

                override fun flush() = Unit

                override fun close() = Unit
            }
        val previousLevel = logger.level
        logger.addHandler(handler)
        logger.level = Level.ALL
        try {
            block()
        } finally {
            logger.removeHandler(handler)
            logger.level = previousLevel
        }
        return messages
    }

    private fun assertViolation(
        model: Any,
        propertyPath: String,
        constraint: KClass<out Annotation>,
    ) {
        val violations = factory.validator.validate(model)

        assertThat(violations).hasSize(1)
        val violation = violations.single()
        assertThat(violation.propertyPath.toString()).isEqualTo(propertyPath)
        assertThat(violation.constraintDescriptor.annotation.annotationClass).isEqualTo(constraint)
    }
}
