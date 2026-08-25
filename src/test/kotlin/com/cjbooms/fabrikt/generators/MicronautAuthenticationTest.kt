package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.ControllerCodeGenOptionType
import com.cjbooms.fabrikt.cli.ControllerCodeGenTargetType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.controller.MicronautControllerInterfaceGenerator
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
class MicronautAuthenticationTest {
    private val basePackage = "authenticationTest"

    @Suppress("unused")
    private fun testCases(): Stream<Pair<String, String>> =
        Stream.of(
            Pair("global_authentication_required.yml", "SecurityRule.IS_AUTHENTICATED"),
            Pair("global_authentication_prohibited.yml", "SecurityRule.IS_ANONYMOUS"),
            Pair("global_authentication_optional.yml", "SecurityRule.IS_AUTHENTICATED, SecurityRule.IS_ANONYMOUS"),
        )

    private fun setupTest(testPath: String): Collection<FileSpec> {
        val api = SourceApi(readTextResource("/authenticationTest/$testPath"))
        return MicronautControllerInterfaceGenerator(
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
            controllerTarget = ControllerCodeGenTargetType.MICRONAUT,
            controllerOptions = setOf(ControllerCodeGenOptionType.AUTHENTICATION),
        )
        ModelNameRegistry.clear()
    }

    // global authentication tests
    @ParameterizedTest
    @MethodSource("testCases")
    fun `ensure that global authentication set to any authentication will add the required parameter`(testCase: Pair<String, String>) {
        val controllers = setupTest(testCase.first)
        val functionAnnotations =
            controllers
                .flatMap { it.members }
                .flatMap { (it as TypeSpec).funSpecs.flatMap { t -> t.annotations } }

        val functionParameters =
            controllers
                .flatMap { it.members }
                .flatMap { (it as TypeSpec).funSpecs.flatMap { t -> t.parameters } }

        assertThat(functionAnnotations).anySatisfy { annotation ->
            assertThat(annotation.typeName.toString()).isEqualTo("io.micronaut.security.`annotation`.Secured")
            assertThat(annotation.members).anyMatch { it.toString() == testCase.second }
        }

        if (testCase.second != "SecurityRule.IS_ANONYMOUS") {
            assertThat(functionParameters).anySatisfy { parameter ->
                assertThat(parameter.name).isEqualTo("authentication")
            }
        } else {
            assertThat(functionParameters).noneSatisfy { parameter ->
                assertThat(parameter.name).isEqualTo("authentication")
            }
        }
    }

    @Test
    fun `ensure that no authentication defined will add no parameter`() {
        val controllers = setupTest("global_authentication_none.yml")
        val functionAnnotations =
            controllers
                .flatMap { it.members }
                .flatMap { (it as TypeSpec).funSpecs.flatMap { t -> t.annotations } }

        val functionParameters =
            controllers
                .flatMap { it.members }
                .flatMap { (it as TypeSpec).funSpecs.flatMap { t -> t.parameters } }

        assertThat(functionAnnotations).noneSatisfy { annotation ->
            assertThat(annotation.typeName.toString()).isEqualTo("Secured")
        }

        assertThat(functionParameters).noneSatisfy { parameter ->
            assertThat(parameter.name).isEqualTo("authentication")
        }
    }

    // operation level authentication tests
    @Test
    fun `ensure that both global security and operation-level security being defined will result in the correctly generated code`() {
        val controllers = setupTest("operation_authentication_with_global.yml")

        val prohibitedController = (controllers.find { it.name == "ProhibitedController" }!!.members.single() as TypeSpec).funSpecs.single()

        assertThat(prohibitedController.annotations).anySatisfy { annotation ->
            assertThat(annotation.typeName.toString()).isEqualTo("io.micronaut.security.`annotation`.Secured")
            assertThat(annotation.members).anyMatch { it.toString() == "SecurityRule.IS_ANONYMOUS" }
        }

        assertThat(prohibitedController.parameters).noneSatisfy { parameter ->
            assertThat(parameter.type.toString()).endsWith("Authentication")
        }

        val requiredController = (controllers.find { it.name == "RequiredController" }!!.members.single() as TypeSpec).funSpecs.single()

        assertThat(requiredController.annotations).anySatisfy { annotation ->
            assertThat(annotation.typeName.toString()).isEqualTo("io.micronaut.security.`annotation`.Secured")
            assertThat(annotation.members).anyMatch { it.toString() == "SecurityRule.IS_AUTHENTICATED" }
        }

        assertThat(requiredController.parameters).anySatisfy { parameter ->
            assertThat(parameter.name).isEqualTo("authentication")
            assertThat(parameter.type.toString()).isEqualTo("io.micronaut.security.authentication.Authentication")
            assertThat(!parameter.type.isNullable)
        }

        val optionalController = (controllers.find { it.name == "OptionalController" }!!.members.single() as TypeSpec).funSpecs.single()

        assertThat(optionalController.annotations).anySatisfy { annotation ->
            assertThat(annotation.typeName.toString()).isEqualTo("io.micronaut.security.`annotation`.Secured")
            assertThat(annotation.members).anyMatch { it.toString() == "SecurityRule.IS_AUTHENTICATED, SecurityRule.IS_ANONYMOUS" }
        }

        assertThat(optionalController.parameters).anySatisfy { parameter ->
            assertThat(parameter.name).isEqualTo("authentication")
            assertThat(parameter.type.toString()).isEqualTo("io.micronaut.security.authentication.Authentication?")
            assertThat(parameter.type.isNullable)
        }

        val noneController = (controllers.find { it.name == "NoneController" }!!.members.single() as TypeSpec).funSpecs.single()

        assertThat(noneController.annotations).noneSatisfy { annotation ->
            assertThat(annotation.typeName.toString()).isEqualTo("Secured")
        }

        assertThat(noneController.parameters).noneSatisfy { parameter ->
            assertThat(parameter.type.toString()).endsWith("Authentication")
        }

        val defaultController = (controllers.find { it.name == "DefaultController" }!!.members.single() as TypeSpec).funSpecs.single()

        assertThat(defaultController.annotations).anySatisfy { annotation ->
            assertThat(annotation.typeName.toString()).isEqualTo("io.micronaut.security.`annotation`.Secured")
            assertThat(annotation.members).anyMatch { it.toString() == "SecurityRule.IS_AUTHENTICATED" }
        }

        assertThat(defaultController.parameters).anySatisfy { parameter ->
            assertThat(parameter.name).isEqualTo("authentication")
            assertThat(parameter.type.toString()).isEqualTo("io.micronaut.security.authentication.Authentication")
        }
    }

    @Test
    fun `ensure that only operation-level security being defined will result in the correctly generated code`() {
        val controllers = setupTest("operation_authentication_without_global.yml")

        val prohibitedController = (controllers.find { it.name == "ProhibitedController" }!!.members.single() as TypeSpec).funSpecs.single()

        assertThat(prohibitedController.annotations).anySatisfy { annotation ->
            assertThat(annotation.typeName.toString()).isEqualTo("io.micronaut.security.`annotation`.Secured")
            assertThat(annotation.members).anyMatch { it.toString() == "SecurityRule.IS_ANONYMOUS" }
        }

        assertThat(prohibitedController.parameters).noneSatisfy { parameter ->
            assertThat(parameter.type.toString()).endsWith("Authentication")
        }

        val requiredController = (controllers.find { it.name == "RequiredController" }!!.members.single() as TypeSpec).funSpecs.single()

        assertThat(requiredController.annotations).anySatisfy { annotation ->
            assertThat(annotation.typeName.toString()).isEqualTo("io.micronaut.security.`annotation`.Secured")
            assertThat(annotation.members).anyMatch { it.toString() == "SecurityRule.IS_AUTHENTICATED" }
        }

        assertThat(requiredController.parameters).anySatisfy { parameter ->
            assertThat(parameter.name).isEqualTo("authentication")
            assertThat(parameter.type.toString()).isEqualTo("io.micronaut.security.authentication.Authentication")
        }

        val optionalController = (controllers.find { it.name == "OptionalController" }!!.members.single() as TypeSpec).funSpecs.single()

        assertThat(optionalController.annotations).anySatisfy { annotation ->
            assertThat(annotation.typeName.toString()).isEqualTo("io.micronaut.security.`annotation`.Secured")
            assertThat(annotation.members).anyMatch { it.toString() == "SecurityRule.IS_AUTHENTICATED, SecurityRule.IS_ANONYMOUS" }
        }

        assertThat(optionalController.parameters).anySatisfy { parameter ->
            assertThat(parameter.name).isEqualTo("authentication")
            assertThat(parameter.type.toString()).isEqualTo("io.micronaut.security.authentication.Authentication?")
            assertThat(parameter.type.isNullable)
        }

        val noneController = (controllers.find { it.name == "NoneController" }!!.members.single() as TypeSpec).funSpecs.single()

        assertThat(noneController.annotations).noneSatisfy { annotation ->
            assertThat(annotation.typeName.toString()).isEqualTo("io.micronaut.security.annotation, Secured")
        }

        assertThat(noneController.parameters).noneSatisfy { parameter ->
            assertThat(parameter.type.toString()).endsWith("Authentication")
        }
    }
}
