package com.cjbooms.fabrikt.servers.spring

import com.example.controllers.PatternsController
import com.example.models.PatternValues
import jakarta.validation.ConstraintViolation
import jakarta.validation.Validation
import jakarta.validation.ValidatorFactory
import org.assertj.core.api.Assertions.assertThat
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.ResponseEntity

/**
 * Runtime coverage for https://github.com/fabrikt-io/fabrikt/issues/783 with Hibernate Validator.
 *
 * An OpenAPI `pattern` matches anywhere in the value unless it is anchored, while `@Pattern` is checked against the
 * whole value. The generated `@Pattern` on a controller parameter and on a model property must accept the same values
 * as the schema.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PatternValidationTest {
    private lateinit var factory: ValidatorFactory

    private val controller =
        object : PatternsController {
            override fun getPatterns(
                unanchored: String,
                anchored: String,
                literal: String,
            ): ResponseEntity<Unit> = ResponseEntity.noContent().build()
        }

    private val getPatterns =
        PatternsController::class.java.getMethod(
            "getPatterns",
            String::class.java,
            String::class.java,
            String::class.java,
        )

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

    @ParameterizedTest
    @ValueSource(strings = ["A", "Alice", "123A456"])
    fun `unanchored pattern accepts a match anywhere in the value`(value: String) {
        assertThat(parameterViolations(unanchored = value)).isEmpty()
        assertThat(propertyViolations(unanchored = value)).isEmpty()
    }

    @ParameterizedTest
    @ValueSource(strings = ["123", ""])
    fun `unanchored pattern rejects a value without a match`(value: String) {
        assertThat(parameterViolations(unanchored = value)).containsExactly("unanchored")
        assertThat(propertyViolations(unanchored = value)).containsExactly("unanchored")
    }

    @ParameterizedTest
    @ValueSource(strings = ["1", "123"])
    fun `anchored pattern accepts a whole value match`(value: String) {
        assertThat(parameterViolations(anchored = value)).isEmpty()
        assertThat(propertyViolations(anchored = value)).isEmpty()
    }

    @ParameterizedTest
    @ValueSource(strings = ["A123", "123A", "123A456", ""])
    fun `anchored pattern rejects a partial match`(value: String) {
        assertThat(parameterViolations(anchored = value)).containsExactly("anchored")
        assertThat(propertyViolations(anchored = value)).containsExactly("anchored")
    }

    @ParameterizedTest
    @ValueSource(strings = ["123\n", "123\r\n"])
    fun `anchored pattern rejects a trailing line terminator`(value: String) {
        assertThat(parameterViolations(anchored = value)).containsExactly("anchored")
        assertThat(propertyViolations(anchored = value)).containsExactly("anchored")
    }

    @ParameterizedTest
    @ValueSource(strings = ["\$1\$", "\$123\$"])
    fun `escaped and bracketed dollar signs match a literal dollar`(value: String) {
        assertThat(parameterViolations(literal = value)).isEmpty()
        assertThat(propertyViolations(literal = value)).isEmpty()
    }

    @ParameterizedTest
    @ValueSource(strings = ["123", "\$123", "123\$", "\$123\$\n"])
    fun `escaped and bracketed dollar signs reject a value without them`(value: String) {
        assertThat(parameterViolations(literal = value)).containsExactly("literal")
        assertThat(propertyViolations(literal = value)).containsExactly("literal")
    }

    private fun parameterViolations(
        unanchored: String = "A",
        anchored: String = "1",
        literal: String = "\$1\$",
    ): List<String> =
        factory.validator.forExecutables()
            .validateParameters(controller, getPatterns, arrayOf(unanchored, anchored, literal))
            .map { it.leafName() }

    private fun propertyViolations(
        unanchored: String = "A",
        anchored: String = "1",
        literal: String = "\$1\$",
    ): List<String> =
        factory.validator
            .validate(PatternValues(unanchored = unanchored, anchored = anchored, literal = literal))
            .map { it.leafName() }

    private fun ConstraintViolation<*>.leafName(): String = propertyPath.last().name
}
