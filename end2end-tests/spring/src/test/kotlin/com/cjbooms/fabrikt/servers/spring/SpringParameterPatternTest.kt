package com.cjbooms.fabrikt.servers.spring

import com.example.controllers.PatternsController
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
 * whole value. The generated `@Pattern` on a controller parameter must accept the same values as the schema.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SpringParameterPatternTest {
    private lateinit var factory: ValidatorFactory

    private val controller =
        object : PatternsController {
            override fun getPatterns(
                unanchored: String,
                anchored: String,
            ): ResponseEntity<Unit> = ResponseEntity.noContent().build()
        }

    private val getPatterns =
        PatternsController::class.java.getMethod("getPatterns", String::class.java, String::class.java)

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
        assertThat(violations(unanchored = value)).isEmpty()
    }

    @ParameterizedTest
    @ValueSource(strings = ["123", ""])
    fun `unanchored pattern rejects a value without a match`(value: String) {
        assertThat(violations(unanchored = value)).containsExactly("getPatterns.unanchored")
    }

    @ParameterizedTest
    @ValueSource(strings = ["1", "123"])
    fun `anchored pattern accepts a whole value match`(value: String) {
        assertThat(violations(anchored = value)).isEmpty()
    }

    @ParameterizedTest
    @ValueSource(strings = ["A123", "123A", "123A456", ""])
    fun `anchored pattern rejects a partial match`(value: String) {
        assertThat(violations(anchored = value)).containsExactly("getPatterns.anchored")
    }

    private fun violations(
        unanchored: String = "A",
        anchored: String = "1",
    ): List<String> =
        factory.validator.forExecutables()
            .validateParameters(controller, getPatterns, arrayOf(unanchored, anchored))
            .map { it.propertyPath.toString() }
}
