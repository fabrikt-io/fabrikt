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

class HeaderApiKeySecurityGeneratorTest {
    private val spec = readTextResource("/examples/headerApiKeySecurity/api.yaml")
    private val packages = Packages("examples.headerApiKeySecurity")

    @BeforeEach
    fun reset() {
        MutableSettings.updateSettings()
        ModelNameRegistry.clear()
    }

    private val options =
        setOf(
            ClientCodeGenOptionType.OPENAPI_API_KEY_AUTHENTICATION,
            ClientCodeGenOptionType.OPENAPI_OAUTH2_AUTHENTICATION,
            ClientCodeGenOptionType.OPENAPI_BEARER_AUTHENTICATION,
        )

    @ParameterizedTest
    @EnumSource(ClientCodeGenTargetType::class)
    fun `explicit helpers reflect complete alternatives for each target`(target: ClientCodeGenTargetType) {
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.CLIENT),
            clientTarget = target,
            clientOptions = options,
            serializationLibrary =
                if (target ==
                    ClientCodeGenTargetType.KTOR
                ) {
                    SerializationLibrary.KOTLINX_SERIALIZATION
                } else {
                    SerializationLibrary.JACKSON_2
                },
        )
        val generated = generator(target, SourceApi(spec)).generate(options)
        assertThatGenerated(
            generated.clients.toSingleFile(),
        ).isEqualTo("/examples/headerApiKeySecurity/client/${target.name.lowercase()}/Client.kt")
        val functions = generated.clients.flatMap { it.spec.funSpecs }.associateBy { it.name }
        assertThat(
            functions,
        ).containsKeys(
            "getDocumentsWithApiKey",
            "getDocumentsWithOAuth2Token",
            "createDocumentWithApiKeyAndOAuth2Token",
            "getBearerWithApiKeyAndBearerToken",
        )
        assertThat(com.squareup.kotlinpoet.KModifier.SUSPEND in functions.getValue("getDocumentsWithApiKey").modifiers)
            .isEqualTo(com.squareup.kotlinpoet.KModifier.SUSPEND in functions.getValue("getDocuments").modifiers)
        assertThat(functions).doesNotContainKeys("createDocumentWithApiKey", "createDocumentWithOAuth2Token", "deleteDocumentWithApiKey")
        assertThat(
            functions.getValue("createDocumentWithApiKeyAndOAuth2Token").parameters.take(2).map {
                it.name
            },
        ).containsExactly("apiKeyProvider", "oauth2TokenProvider")
        val ordinary = generator(target, SourceApi(spec)).generate(emptySet()).clients
        ordinary.zip(generated.clients).forEach { (before, after) ->
            assertThat(
                after.spec
                    .toBuilder()
                    .apply {
                        funSpecs.removeAll {
                            it.name !in
                                before.spec.funSpecs.map { f ->
                                    f.name
                                }
                        }
                    }.build(),
            ).isEqualTo(before.spec)
        }
    }

    @ParameterizedTest
    @EnumSource(ClientCodeGenTargetType::class)
    fun `provider and local names avoid operation parameter collisions`(target: ClientCodeGenTargetType) {
        val source =
            spec.replace(
                "      operationId: getDocuments",
                """      operationId: getDocuments
      parameters:
        - {name: apiKeyProvider, in: query, schema: {type: string}}
        - {name: oauth2TokenProvider, in: query, schema: {type: string}}
        - {name: credential, in: query, schema: {type: string}}
        - {name: credentialCache, in: query, schema: {type: string}}
        - {name: authenticationHeaders, in: query, schema: {type: string}}""",
            )
        MutableSettings.updateSettings(genTypes = setOf(CodeGenerationType.CLIENT), clientTarget = target, clientOptions = options)
        val functions =
            generator(target, SourceApi(source))
                .generate(options)
                .clients
                .flatMap { it.spec.funSpecs }
                .associateBy { it.name }
        val wrapper = functions.getValue("getDocumentsWithApiKey")
        assertThat(wrapper.parameters.map { it.name }).contains("apiKeyProviderExtra", "apiKeyProvider", "credentialCache", "credential")
        assertThat(wrapper.toString()).contains("credentialCacheExtra", "authenticationHeadersExtra", "credentialExtra")
        val token = functions.getValue("getDocumentsWithOAuth2Token")
        assertThat(token.parameters.map { it.name }).contains("oauth2TokenProviderExtra", "oauth2TokenProvider")
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
