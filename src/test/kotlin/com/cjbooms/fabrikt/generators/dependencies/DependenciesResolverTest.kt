package com.cjbooms.fabrikt.generators.dependencies

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.cli.ClientCodeGenTargetType
import com.cjbooms.fabrikt.cli.CodeGenTypeOverride
import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.ControllerCodeGenOptionType
import com.cjbooms.fabrikt.cli.ControllerCodeGenTargetType
import com.cjbooms.fabrikt.cli.InstantLibrary
import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType
import com.cjbooms.fabrikt.cli.SerializationLibrary
import com.cjbooms.fabrikt.cli.ValidationLibrary
import com.cjbooms.fabrikt.generators.MutableSettings
import com.cjbooms.fabrikt.util.ModelNameRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class DependenciesResolverTest {
    @BeforeEach
    fun init() {
        MutableSettings.updateSettings()
        ModelNameRegistry.clear()
    }

    @Nested
    inner class DefaultSettingsDifferentGenerationTypes {
        @Test
        fun `no code generation type`() {
            val dependencies = DependenciesResolver(MutableSettings).resolve().map { it.toGavString() }
            assertThat(dependencies).isEqualTo(defaultDependencies())
        }

        @Test
        fun `default client`() {
            MutableSettings.updateSettings(
                genTypes = setOf(CodeGenerationType.CLIENT),
            )
            val dependencies =
                DependenciesResolver(MutableSettings)
                    .resolve()
                    .map { it.toGavString() }

            assertThat(dependencies).isEqualTo(
                defaultDependencies() +
                    "COMPILE com.squareup.okhttp3:okhttp:4.10.0",
            )
        }

        @Test
        fun `default controller`() {
            MutableSettings.updateSettings(
                genTypes = setOf(CodeGenerationType.CONTROLLERS),
            )
            val dependencies =
                DependenciesResolver(MutableSettings)
                    .resolve()
                    .map { it.toGavString() }

            assertThat(dependencies).isEqualTo(
                defaultDependencies() +
                    "COMPILE org.springframework:spring-webmvc:6.1.0",
            )
        }

        @Test
        fun `models only`() {
            MutableSettings.updateSettings(
                genTypes = setOf(CodeGenerationType.HTTP_MODELS),
            )
            val dependencies =
                DependenciesResolver(MutableSettings)
                    .resolve()
                    .map { it.toGavString() }

            assertThat(dependencies).isEqualTo(defaultDependencies())
        }

        @Test
        fun `quarkus reflection config only`() {
            MutableSettings.updateSettings(
                genTypes = setOf(CodeGenerationType.QUARKUS_REFLECTION_CONFIG),
            )
            val dependencies =
                DependenciesResolver(MutableSettings)
                    .resolve()
                    .map { it.toGavString() }

            assertThat(dependencies).isEqualTo(defaultDependencies())
        }
    }

    @Nested
    inner class ModelsOptions {
        @Test
        fun `all model options`() {
            MutableSettings.updateSettings(
                modelOptions = ModelCodeGenOptionType.entries.toSet(),
            )
            val dependencies = DependenciesResolver(MutableSettings).resolve().map { it.toGavString() }
            assertThat(dependencies).isEqualTo(
                listOf(
                    "COMPILE io.quarkus:quarkus-core:3.39.4",
                    "COMPILE io.micronaut:micronaut-core:3.8.7",
                    "COMPILE io.micronaut.serde:micronaut-serde-jackson:1.5.2",
                ) + defaultDependencies(),
            )
        }

        @ParameterizedTest
        @EnumSource(ValidationLibrary::class)
        fun `validation libraries options`(library: ValidationLibrary) {
            MutableSettings.updateSettings(
                validationLibrary = library,
            )
            val dependencies = DependenciesResolver(MutableSettings).resolve().map { it.toGavString() }
            assertThat(dependencies).isEqualTo(
                when (library) {
                    ValidationLibrary.JAVAX_VALIDATION ->
                        listOf(
                            "COMPILE javax.validation:validation-api:2.0.1.Final",
                            "COMPILE com.fasterxml.jackson.core:jackson-core:2.21.6",
                            "COMPILE com.fasterxml.jackson.core:jackson-databind:2.21.6",
                            "COMPILE com.fasterxml.jackson.module:jackson-module-kotlin:2.21.6",
                        )
                    ValidationLibrary.JAKARTA_VALIDATION -> defaultDependencies()
                    ValidationLibrary.NO_VALIDATION ->
                        listOf(
                            "COMPILE com.fasterxml.jackson.core:jackson-core:2.21.6",
                            "COMPILE com.fasterxml.jackson.core:jackson-databind:2.21.6",
                            "COMPILE com.fasterxml.jackson.module:jackson-module-kotlin:2.21.6",
                        )
                },
            )
        }

        @ParameterizedTest
        @EnumSource(SerializationLibrary::class)
        fun `serialization libraries options`(library: SerializationLibrary) {
            MutableSettings.updateSettings(
                serializationLibrary = library,
            )
            val dependencies = DependenciesResolver(MutableSettings).resolve().map { it.toGavString() }
            assertThat(dependencies).isEqualTo(
                when (library) {
                    SerializationLibrary.JACKSON -> defaultDependencies()
                    SerializationLibrary.JACKSON_3 ->
                        listOf(
                            "COMPILE jakarta.validation:jakarta.validation-api:3.0.2",
                            "COMPILE tools.jackson.core:jackson-databind:3.2.2",
                            "COMPILE tools.jackson.module:jackson-module-kotlin:3.2.2",
                        )
                    SerializationLibrary.KOTLINX_SERIALIZATION ->
                        listOf(
                            "COMPILE jakarta.validation:jakarta.validation-api:3.0.2",
                            "COMPILE org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0",
                        )
                },
            )
        }

        @ParameterizedTest
        @EnumSource(InstantLibrary::class)
        fun `instant libraries options`(library: InstantLibrary) {
            MutableSettings.updateSettings(
                typeOverrides = setOf(CodeGenTypeOverride.DATETIME_AS_INSTANT),
                instantLibrary = library,
            )
            val dependencies = DependenciesResolver(MutableSettings).resolve().map { it.toGavString() }
            assertThat(dependencies).isEqualTo(
                when (library) {
                    InstantLibrary.KOTLINX_INSTANT ->
                        defaultDependencies() +
                            "COMPILE org.jetbrains.kotlinx:kotlinx-datetime:0.7.1"
                    InstantLibrary.KOTLIN_TIME_INSTANT -> defaultDependencies()
                },
            )
        }
    }

    @Nested
    inner class ClientOptions {
        @Test
        fun `all client options`() {
            MutableSettings.updateSettings(
                genTypes = setOf(CodeGenerationType.CLIENT),
                clientOptions = ClientCodeGenOptionType.entries.toSet(),
            )
            val dependencies = DependenciesResolver(MutableSettings).resolve().map { it.toGavString() }
            assertThat(dependencies).isEqualTo(
                defaultDependencies() +
                    listOf(
                        "COMPILE com.squareup.okhttp3:okhttp:4.10.0",
                        "COMPILE io.github.resilience4j:resilience4j-circuitbreaker:2.1.0",
                        "COMPILE org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0",
                        "COMPILE org.springframework:spring-web:6.1.0",
                        "COMPILE org.springframework.cloud:spring-cloud-starter-openfeign:5.0.3",
                    ),
            )
        }

        @ParameterizedTest
        @EnumSource(ClientCodeGenTargetType::class)
        fun `client target options`(target: ClientCodeGenTargetType) {
            MutableSettings.updateSettings(
                genTypes = setOf(CodeGenerationType.CLIENT),
                clientTarget = target,
            )
            val dependencies = DependenciesResolver(MutableSettings).resolve().map { it.toGavString() }
            assertThat(dependencies).isEqualTo(
                when (target) {
                    ClientCodeGenTargetType.OK_HTTP ->
                        defaultDependencies() +
                            "COMPILE com.squareup.okhttp3:okhttp:4.10.0"
                    ClientCodeGenTargetType.OPEN_FEIGN ->
                        defaultDependencies() +
                            "COMPILE io.github.openfeign:feign-core:13.3"
                    ClientCodeGenTargetType.SPRING_HTTP_INTERFACE ->
                        defaultDependencies() +
                            "COMPILE org.springframework:spring-web:6.1.0"
                    ClientCodeGenTargetType.KTOR ->
                        defaultDependencies() +
                            listOf(
                                "COMPILE io.ktor:ktor-client-core:3.0.1",
                                "COMPILE io.ktor:ktor-client-content-negotiation:3.0.1",
                            )
                },
            )
        }
    }

    @Nested
    inner class ControllerOptions {
        @Test
        fun `all controller options`() {
            MutableSettings.updateSettings(
                genTypes = setOf(CodeGenerationType.CONTROLLERS),
                controllerOptions = ControllerCodeGenOptionType.entries.toSet(),
            )
            val dependencies = DependenciesResolver(MutableSettings).resolve().map { it.toGavString() }
            assertThat(dependencies).isEqualTo(
                defaultDependencies() +
                    listOf(
                        "COMPILE org.springframework:spring-webmvc:6.1.0",
                        "COMPILE org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0",
                    ),
            )
        }

        @ParameterizedTest
        @EnumSource(ControllerCodeGenTargetType::class)
        fun `controller target options`(target: ControllerCodeGenTargetType) {
            MutableSettings.updateSettings(
                genTypes = setOf(CodeGenerationType.CONTROLLERS),
                controllerTarget = target,
            )
            val dependencies = DependenciesResolver(MutableSettings).resolve().map { it.toGavString() }
            assertThat(dependencies).isEqualTo(
                when (target) {
                    ControllerCodeGenTargetType.SPRING ->
                        defaultDependencies() +
                            "COMPILE org.springframework:spring-webmvc:6.1.0"
                    ControllerCodeGenTargetType.MICRONAUT ->
                        defaultDependencies() +
                            listOf(
                                "COMPILE io.micronaut:micronaut-http:3.8.7",
                                "COMPILE io.micronaut.security:micronaut-security:3.8.7",
                            )
                    ControllerCodeGenTargetType.KTOR ->
                        defaultDependencies() +
                            "COMPILE io.ktor:ktor-server-core:3.0.1"
                },
            )
        }
    }

    private fun defaultDependencies() =
        listOf(
            "COMPILE jakarta.validation:jakarta.validation-api:3.0.2",
            "COMPILE com.fasterxml.jackson.core:jackson-core:2.21.6",
            "COMPILE com.fasterxml.jackson.core:jackson-databind:2.21.6",
            "COMPILE com.fasterxml.jackson.module:jackson-module-kotlin:2.21.6",
        )

    companion object {
        private fun DependencyNotation.toGavString() = "$scope $groupId:$artifactId:$version"
    }
}
