package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.cli.ClientCodeGenTargetType
import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.SerializationLibrary
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.client.SpringHttpInterfaceGenerator
import com.cjbooms.fabrikt.model.SimpleFile
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.GeneratedCodeAsserter.Companion.assertThatGenerated
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import com.cjbooms.fabrikt.util.TestFileUtils.toSingleFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class SpringDynamicMultipartGeneratorTest {
    private val spec = readTextResource("/examples/multipartDynamicClientParts/api.yaml").replace("\r\n", "\n")

    @ParameterizedTest
    @EnumSource(value = SerializationLibrary::class, names = ["JACKSON_2", "JACKSON_3"])
    fun `dynamic multipart preserves fixed JSON and file parts`(library: SerializationLibrary) {
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.CLIENT),
            clientTarget = ClientCodeGenTargetType.SPRING_HTTP_INTERFACE,
            serializationLibrary = library,
        )
        ModelNameRegistry.clear()
        val generator = SpringHttpInterfaceGenerator(Packages("examples.multipartDynamicClientParts"), SourceApi(spec))
        assertThatGenerated(generator.generate(emptySet()).clients.toSingleFile())
            .isEqualTo("/examples/multipartDynamicClientParts/client/spring${library.name}.kt")
        generator.generateLibrary(emptySet()).filterIsInstance<SimpleFile>().forEach {
            assertThatGenerated(it.content).isEqualTo("/examples/multipartDynamicClientParts/client/${it.path.fileName}")
        }
    }

    @ParameterizedTest
    @EnumSource(value = SerializationLibrary::class, names = ["JACKSON_2", "JACKSON_3"])
    fun `unsupported values retain fixed parts without a dynamic map`(library: SerializationLibrary) {
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.CLIENT),
            clientTarget = ClientCodeGenTargetType.SPRING_HTTP_INTERFACE,
            serializationLibrary = library,
        )
        ModelNameRegistry.clear()
        val unsupported = spec.replace("'^resource\\..+$': {type: string, format: binary}", "'^resource\\..+$': {type: integer}")
        val client =
            SpringHttpInterfaceGenerator(
                Packages("examples.multipartDynamicClientParts"),
                SourceApi(unsupported),
            ).generate(emptySet()).clients.toSingleFile()
        assertThat(client).contains("template:").doesNotContain("dynamicParts:")
    }

    @Test
    fun `security wrappers and colliding names preserve the dynamic argument`() {
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.CLIENT),
            clientTarget = ClientCodeGenTargetType.SPRING_HTTP_INTERFACE,
        )
        ModelNameRegistry.clear()
        val input =
            spec
                .replace(
                    "      requestBody:",
                    "      parameters:\n        - {name: dynamicParts, in: cookie, schema: {type: string}}\n      requestBody:",
                ).replace(
                    "components:\n",
                    """
                    security: [{Bearer: []}, {OAuth: [upload]}]
                    components:
                      securitySchemes:
                        Bearer: {type: http, scheme: bearer}
                        OAuth:
                          type: oauth2
                          flows:
                            clientCredentials:
                              tokenUrl: https://example.invalid/token
                              scopes: {upload: Upload}
                    """.trimIndent() + "\n",
                )
        val client =
            SpringHttpInterfaceGenerator(Packages("examples.multipartDynamicClientParts"), SourceApi(input))
                .generate(
                    setOf(ClientCodeGenOptionType.OPENAPI_BEARER_AUTHENTICATION, ClientCodeGenOptionType.OPENAPI_OAUTH2_AUTHENTICATION),
                ).clients
                .toSingleFile()
        assertThat(client).contains("dynamicParts_: Map", "WithBearerToken", "WithOAuth2Token", "dynamicParts_ = dynamicParts_")
    }
}
