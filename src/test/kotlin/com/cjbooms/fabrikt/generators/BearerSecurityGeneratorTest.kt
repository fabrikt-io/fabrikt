package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.cli.ClientCodeGenTargetType
import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.SerializationLibrary
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.client.ClientGenerator
import com.cjbooms.fabrikt.generators.client.OkHttpClientGenerator
import com.cjbooms.fabrikt.generators.client.OpenFeignInterfaceGenerator
import com.cjbooms.fabrikt.generators.client.SpringHttpInterfaceGenerator
import com.cjbooms.fabrikt.generators.controller.KtorClientGenerator
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.GeneratedCodeAsserter.Companion.assertThatGenerated
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import com.cjbooms.fabrikt.util.TestFileUtils.toSingleFile
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.nio.file.Paths

class BearerSecurityGeneratorTest {
    private val spec = readTextResource("/examples/bearerSecurity/api.yaml")
    private val packages = Packages("examples.bearerSecurity")

    @BeforeEach
    fun reset() {
        MutableSettings.updateSettings()
        ModelNameRegistry.clear()
    }

    @ParameterizedTest
    @EnumSource(ClientCodeGenTargetType::class)
    fun `Bearer helpers are generated for each client target`(target: ClientCodeGenTargetType) {
        val api = SourceApi(spec)
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.CLIENT),
            clientTarget = target,
            clientOptions = setOf(ClientCodeGenOptionType.OPENAPI_BEARER_AUTHENTICATION),
            serializationLibrary =
                if (target == ClientCodeGenTargetType.KTOR) SerializationLibrary.KOTLINX_SERIALIZATION else SerializationLibrary.JACKSON,
        )
        val generated = generator(target, api).generate(setOf(ClientCodeGenOptionType.OPENAPI_BEARER_AUTHENTICATION))
        assertThatGenerated(generated.clients.toSingleFile())
            .isEqualTo("/examples/bearerSecurity/client/${target.name.lowercase()}/Client.kt")
    }

    @Test
    fun `Bearer helpers are generated for the enhanced OkHttp client`() {
        val api = SourceApi(spec)
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.CLIENT),
            clientTarget = ClientCodeGenTargetType.OK_HTTP,
            clientOptions =
                setOf(
                    ClientCodeGenOptionType.OPENAPI_BEARER_AUTHENTICATION,
                    ClientCodeGenOptionType.RESILIENCE4J,
                ),
        )
        val generated =
            generator(ClientCodeGenTargetType.OK_HTTP, api).generate(MutableSettings.clientOptions)
        assertThatGenerated(generated.clients.toSingleFile())
            .isEqualTo("/examples/bearerSecurity/client/okhttp-enhanced/Client.kt")
    }

    private fun generator(
        target: ClientCodeGenTargetType,
        api: SourceApi,
    ): ClientGenerator =
        when (target) {
            ClientCodeGenTargetType.OK_HTTP -> OkHttpClientGenerator(packages, api, Paths.get("src/main/kotlin"))
            ClientCodeGenTargetType.OPEN_FEIGN -> OpenFeignInterfaceGenerator(packages, api)
            ClientCodeGenTargetType.SPRING_HTTP_INTERFACE -> SpringHttpInterfaceGenerator(packages, api)
            ClientCodeGenTargetType.KTOR -> KtorClientGenerator(packages, api)
        }
}
