package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.ControllerCodeGenOptionType
import com.cjbooms.fabrikt.cli.ControllerCodeGenTargetType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.controller.SpringControllerInterfaceGenerator
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SpringAuthenticationTest {
    private val basePackage = "authenticationTest"

    @Suppress("unused")
    private fun testCasesNoAuthentication(): Stream<String> =
        Stream.of(
            "global_authentication_prohibited.yml",
            "global_authentication_none.yml",
        )

    private fun setupTest(testPath: String): Collection<FileSpec> {
        val api = SourceApi(readTextResource("/authenticationTest/$testPath"))
        return SpringControllerInterfaceGenerator(
            Packages(basePackage),
            api,
            JavaxValidationAnnotations,
            setOf(ControllerCodeGenOptionType.AUTHENTICATION),
        ).generate().files
    }

    @BeforeEach
    fun init() {
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.CONTROLLERS),
            controllerTarget = ControllerCodeGenTargetType.SPRING,
        )
        ModelNameRegistry.clear()
    }

    // global authentication tests
    @Test
    fun `ensure that global authentication set to any authentication will add the required parameter`() {
        val controllers = setupTest("global_authentication_required.yml")

        val functionParameters =
            controllers
                .flatMap { it.members }
                .flatMap { (it as TypeSpec).funSpecs.flatMap { t -> t.parameters } }

        assertThat(functionParameters).anySatisfy { parameter ->
            assertThat(parameter.name).isEqualTo("authentication")
        }
    }

    @Test
    fun `ensure that global authentication set to any AND empty authentication will add the optional parameter`() {
        val controllers = setupTest("global_authentication_optional.yml")

        val functionParameters =
            controllers
                .flatMap { it.members }
                .flatMap { (it as TypeSpec).funSpecs.flatMap { t -> t.parameters } }

        assertThat(functionParameters).anySatisfy { parameter ->
            assertThat(parameter.name).isEqualTo("authentication")
            assertThat(parameter.type.isNullable)
        }
    }

    @ParameterizedTest
    @MethodSource("testCasesNoAuthentication")
    fun `ensure that global authentication set to empty authentication will add the prohibited parameter`(testCasePath: String) {
        val controllers = setupTest(testCasePath)

        val functionParameters =
            controllers
                .flatMap { it.members }
                .flatMap { (it as TypeSpec).funSpecs.flatMap { t -> t.parameters } }

        assertThat(functionParameters).noneSatisfy { parameter ->
            assertThat(parameter.name).isEqualTo("authentication")
        }
    }

    // operation level authentication tests
    @Test
    fun `ensure that both global security and operation-level security being defined will result in the correctly generated code`() {
        val controllers = setupTest("operation_authentication_with_global.yml")

        val prohibitedController = (controllers.find { it.name == "ProhibitedController" }!!.members.single() as TypeSpec).funSpecs.single()

        assertThat(prohibitedController.parameters).noneSatisfy { parameter ->
            assertThat(parameter.type.toString()).endsWith("Authentication")
        }

        val requiredController = (controllers.find { it.name == "RequiredController" }!!.members.single() as TypeSpec).funSpecs.single()

        assertThat(requiredController.parameters).anySatisfy { parameter ->
            assertThat(parameter.name).isEqualTo("authentication")
            assertThat(parameter.type.toString()).isEqualTo("org.springframework.security.core.Authentication")
        }

        val optionalController = (controllers.find { it.name == "OptionalController" }!!.members.single() as TypeSpec).funSpecs.single()

        assertThat(optionalController.parameters).anySatisfy { parameter ->
            assertThat(parameter.name).isEqualTo("authentication")
            assertThat(parameter.type.toString()).isEqualTo("org.springframework.security.core.Authentication?")
            assertThat(parameter.type.isNullable)
        }

        val defaultController = (controllers.find { it.name == "DefaultController" }!!.members.single() as TypeSpec).funSpecs.single()

        assertThat(defaultController.parameters).anySatisfy { parameter ->
            assertThat(parameter.name).isEqualTo("authentication")
            assertThat(parameter.type.toString()).isEqualTo("org.springframework.security.core.Authentication")
        }

        val noneController = (controllers.find { it.name == "NoneController" }!!.members.single() as TypeSpec).funSpecs.single()

        assertThat(noneController.parameters).noneSatisfy { parameter ->
            assertThat(parameter.type.toString()).endsWith("Authentication")
        }
    }

    @Test
    fun `ensure that only operation-level security being defined will result in the correctly generated code`() {
        val controllers = setupTest("operation_authentication_without_global.yml")

        val prohibitedController = (controllers.find { it.name == "ProhibitedController" }!!.members.single() as TypeSpec).funSpecs.single()

        assertThat(prohibitedController.parameters).noneSatisfy { parameter ->
            assertThat(parameter.type.toString()).endsWith("Authentication")
        }

        val requiredController = (controllers.find { it.name == "RequiredController" }!!.members.single() as TypeSpec).funSpecs.single()

        assertThat(requiredController.parameters).anySatisfy { parameter ->
            assertThat(parameter.name).isEqualTo("authentication")
            assertThat(parameter.type.toString()).isEqualTo("org.springframework.security.core.Authentication")
        }

        val optionalController = (controllers.find { it.name == "OptionalController" }!!.members.single() as TypeSpec).funSpecs.single()

        assertThat(optionalController.parameters).anySatisfy { parameter ->
            assertThat(parameter.name).isEqualTo("authentication")
            assertThat(parameter.type.toString()).isEqualTo("org.springframework.security.core.Authentication?")
            assertThat(parameter.type.isNullable)
        }

        val noneController = (controllers.find { it.name == "NoneController" }!!.members.single() as TypeSpec).funSpecs.single()

        assertThat(noneController.parameters).noneSatisfy { parameter ->
            assertThat(parameter.type.toString()).endsWith("Authentication")
        }
    }
}
