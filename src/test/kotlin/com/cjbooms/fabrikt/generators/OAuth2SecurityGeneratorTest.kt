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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.nio.file.Paths

class OAuth2SecurityGeneratorTest {
    private val spec = readTextResource("/examples/oauth2Security/api.yaml")
    private val packages = Packages("examples.oauth2Security")

    @BeforeEach
    fun reset() {
        MutableSettings.updateSettings()
        ModelNameRegistry.clear()
    }

    @ParameterizedTest
    @EnumSource(ClientCodeGenTargetType::class)
    fun `Bearer and OAuth2 helpers coexist without wrapping each other`(target: ClientCodeGenTargetType) {
        val mixed =
            spec
                .replace("\r\n", "\n")
                .replace("  - OAuth2: [read:pets]\n", "  - OAuth2: [read:pets]\n  - Bearer: []\n")
                .replace("  securitySchemes:\n", "  securitySchemes:\n    Bearer:\n      type: http\n      scheme: bearer\n")
        val options = setOf(ClientCodeGenOptionType.OPENAPI_BEARER_AUTHENTICATION, ClientCodeGenOptionType.OPENAPI_OAUTH2_AUTHENTICATION)
        MutableSettings.updateSettings(genTypes = setOf(CodeGenerationType.CLIENT), clientTarget = target, clientOptions = options)
        val functions =
            generator(target, SourceApi(mixed))
                .generate(options)
                .clients
                .flatMap { it.spec.funSpecs }
                .associateBy { it.name }
        assertThat(functions).containsKeys("getProtectedWithBearerToken", "getProtectedWithOAuth2Token")
        assertThat(functions.keys).noneMatch { it.contains("WithBearerTokenWithOAuth2Token") }
        assertThat(functions.getValue("getProtectedWithOAuth2Token").toString()).contains("return getProtected(")
        assertThat(functions.getValue("getProtectedWithBearerToken").toString()).contains("return getProtected(")
    }

    @ParameterizedTest
    @EnumSource(ClientCodeGenTargetType::class)
    fun `OAuth2 option leaves existing methods unchanged`(target: ClientCodeGenTargetType) {
        MutableSettings.updateSettings(genTypes = setOf(CodeGenerationType.CLIENT), clientTarget = target)
        val ordinary = generator(target, SourceApi(spec)).generate(emptySet()).clients
        val enabled = generator(target, SourceApi(spec)).generate(setOf(ClientCodeGenOptionType.OPENAPI_OAUTH2_AUTHENTICATION)).clients
        ordinary.zip(enabled).forEach { (before, after) ->
            assertThat(after.spec.funSpecs.filterNot { it.name.endsWith("WithOAuth2Token") }).isEqualTo(before.spec.funSpecs)
            assertThat(before.spec.funSpecs.none { it.name.endsWith("WithOAuth2Token") }).isTrue()
            assertThat(
                after.spec
                    .toBuilder()
                    .apply {
                        funSpecs.removeAll { it.name.endsWith("WithOAuth2Token") }
                    }.build(),
            ).isEqualTo(before.spec)
        }
    }

    @ParameterizedTest
    @EnumSource(ClientCodeGenTargetType::class)
    fun `OAuth2 helpers are generated for each client target`(target: ClientCodeGenTargetType) {
        val api = SourceApi(spec)
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.CLIENT),
            clientTarget = target,
            clientOptions = setOf(ClientCodeGenOptionType.OPENAPI_OAUTH2_AUTHENTICATION),
            serializationLibrary =
                if (target == ClientCodeGenTargetType.KTOR) SerializationLibrary.KOTLINX_SERIALIZATION else SerializationLibrary.JACKSON,
        )
        val generated = generator(target, api).generate(setOf(ClientCodeGenOptionType.OPENAPI_OAUTH2_AUTHENTICATION))
        assertThatGenerated(generated.clients.toSingleFile())
            .isEqualTo("/examples/oauth2Security/client/${target.name.lowercase()}/Client.kt")
    }

    @ParameterizedTest
    @EnumSource(ClientCodeGenTargetType::class, names = ["OK_HTTP", "OPEN_FEIGN", "SPRING_HTTP_INTERFACE"])
    fun `typed response functions receive matching OAuth2 helpers`(target: ClientCodeGenTargetType) {
        val options =
            setOf(
                ClientCodeGenOptionType.OPENAPI_OAUTH2_AUTHENTICATION,
                ClientCodeGenOptionType.RESPONSE_MEDIA_TYPE_FUNCTIONS,
            )
        MutableSettings.updateSettings(genTypes = setOf(CodeGenerationType.CLIENT), clientTarget = target, clientOptions = options)
        val api =
            SourceApi(
                """
                openapi: 3.0.3
                info:
                  title: Secured response representations
                  version: '1.0'
                security:
                  - OAuth2: []
                paths:
                  /details:
                    get:
                      operationId: getDetails
                      parameters:
                        - name: session
                          in: cookie
                          schema:
                            type: string
                      responses:
                        '200':
                          description: OK
                          content:
                            application/json:
                              schema:
                                ${'$'}ref: '#/components/schemas/First'
                            application/problem+json:
                              schema:
                                ${'$'}ref: '#/components/schemas/Second'
                components:
                  securitySchemes:
                    OAuth2:
                      type: oauth2
                      flows:
                        clientCredentials:
                          tokenUrl: https://example.com/token
                          scopes: {}
                  schemas:
                    First:
                      type: object
                      properties:
                        name:
                          type: string
                    Second:
                      type: object
                      properties:
                        count:
                          type: integer
                """.trimIndent(),
            )
        generator(target, api).generate(options).clients.forEach { client ->
            val functions = client.spec.funSpecs.associateBy { it.name }
            mapOf("Json" to "First", "ProblemJson" to "Second").forEach { (suffix, model) ->
                val name = "getDetails$suffix"
                val function = functions.getValue(name)
                val wrapper = functions.getValue("${name}WithOAuth2Token")
                assertThat(wrapper.returnType).isEqualTo(function.returnType)
                assertThat(wrapper.returnType.toString()).contains("${packages.models}.$model")
                assertThat(wrapper.parameters.map { it.name }).contains("oauth2TokenProvider", "session")
                assertThat(wrapper.toString()).contains("return $name(", "Authorization", "OAuth2")
            }
            assertThat(functions).containsKey("getDetailsWithOAuth2Token")
        }
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
