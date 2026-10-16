package com.cjbooms.fabrikt.generators.client

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.lang.reflect.InvocationTargetException
import java.net.URLClassLoader
import java.nio.file.Path

class HeaderSecurityWrapperRuntimeTest {
    @TempDir
    lateinit var directory: Path

    @Test
    fun `compiled helpers require complete alternatives without leaking partial headers`() {
        val document =
            SourceApi(
                readTextResource(
                    "/examples/headerApiKeySecurity/api.yaml",
                ).replace("- OtherKey: []\n          OAuth: [admin:documents]", "- Key: []\n          OAuth: [admin:documents]"),
            ).openApi3
        val options =
            setOf(
                ClientCodeGenOptionType.OPENAPI_API_KEY_AUTHENTICATION,
                ClientCodeGenOptionType.OPENAPI_OAUTH2_AUTHENTICATION,
                ClientCodeGenOptionType.OPENAPI_BEARER_AUTHENTICATION,
            )
        val headers = MAP.parameterizedBy(STRING, STRING)
        val client = TypeSpec.classBuilder("Client")
        document.paths.values.flatMap { it.operations.values }.forEach { operation ->
            val function =
                FunSpec
                    .builder(operation.operationId!!)
                    .addParameter(ParameterSpec.builder("additionalHeaders", headers).defaultValue("emptyMap()").build())
                    .returns(headers)
                    .addStatement("return additionalHeaders")
                    .build()
            client.addFunction(function)
            ClientHeaderApiKeySecurity(document).forOperation(operation, options)?.forEach { plan ->
                client.addFunction(function.withHeaderSecurityWrapper(plan, TokenWrapperTarget.ADDITIONAL_HEADERS))
            }
        }
        FileSpec
            .builder("audit", "Client")
            .addType(client.build())
            .build()
            .writeTo(directory)
        val output = directory.resolve("classes")
        val diagnostics = ByteArrayOutputStream()
        val result =
            K2JVMCompiler().exec(
                PrintStream(diagnostics),
                "-no-stdlib",
                "-no-reflect",
                "-jvm-target",
                "17",
                "-classpath",
                Path
                    .of(
                        Unit::class.java.protectionDomain.codeSource.location
                            .toURI(),
                    ).toString(),
                "-d",
                output.toString(),
                directory.resolve("audit/Client.kt").toString(),
            )
        assertThat(result).withFailMessage(diagnostics.toString()).isEqualTo(ExitCode.OK)
        URLClassLoader(arrayOf(output.toUri().toURL()), javaClass.classLoader).use { loader ->
            val type = loader.loadClass("audit.Client")
            val instance = type.getConstructor().newInstance()
            val original = mapOf("x-api-key" to "old", "AUTHORIZATION" to "old-token", "X-Trace" to "trace")
            val calls = mutableListOf<String>()
            val key: (String) -> String? = {
                calls.add(it)
                "key-value"
            }
            val token: (String, Set<String>) -> String? = { scheme, scopes ->
                calls.add("$scheme:$scopes")
                "token-value"
            }
            val missing: (String) -> String? = { null }
            val noToken: (String, Set<String>) -> String? = { _, _ -> " " }
            val apiOnly = type.getMethod("getDocumentsWithApiKey", Function1::class.java, Map::class.java)
            assertThat(apiOnly.invoke(instance, key, original)).isEqualTo(
                mapOf(
                    "X-API-Key" to "key-value",
                    "AUTHORIZATION" to "old-token",
                    "X-Trace" to "trace",
                ),
            )
            assertThat(calls).containsExactly("Key")
            calls.clear()
            val oauthOnly = type.getMethod("getDocumentsWithOAuth2Token", Function2::class.java, Map::class.java)
            assertThat(oauthOnly.invoke(instance, token, original)).isEqualTo(
                mapOf(
                    "x-api-key" to "old",
                    "Authorization" to "Bearer token-value",
                    "X-Trace" to "trace",
                ),
            )
            assertThat(calls).containsExactly("OAuth:[read:documents]")
            val combined =
                type.getMethod(
                    "createDocumentWithApiKeyAndOAuth2Token",
                    Function1::class.java,
                    Function2::class.java,
                    Map::class.java,
                )
            assertThat(combined.invoke(instance, key, token, original)).isEqualTo(
                mapOf(
                    "X-API-Key" to "key-value",
                    "Authorization" to "Bearer token-value",
                    "X-Trace" to "trace",
                ),
            )
            listOf(arrayOf(missing, token), arrayOf(key, noToken)).forEach { providers ->
                assertThatThrownBy {
                    combined.invoke(instance, providers[0], providers[1], original)
                }.isInstanceOf(InvocationTargetException::class.java).hasCauseInstanceOf(IllegalStateException::class.java)
            }
            assertThatThrownBy { apiOnly.invoke(instance, missing, original) }.hasCauseInstanceOf(IllegalStateException::class.java)
            val unsafe: (String) -> String? = { "value\r\nX-Injected: true" }
            assertThatThrownBy { apiOnly.invoke(instance, unsafe, original) }.hasCauseInstanceOf(IllegalArgumentException::class.java)
            calls.clear()
            val alternateToken: (
                String,
                Set<String>,
            ) -> String? = { scheme, scopes ->
                calls.add("$scheme:$scopes")
                if ("admin:documents" in
                    scopes
                ) {
                    "admin"
                } else {
                    null
                }
            }
            val alternative =
                type.getMethod(
                    "updateDocumentWithApiKeyAndOAuth2Token",
                    Function1::class.java,
                    Function2::class.java,
                    Map::class.java,
                )
            assertThat(alternative.invoke(instance, key, alternateToken, original)).isEqualTo(
                mapOf(
                    "X-API-Key" to "key-value",
                    "Authorization" to "Bearer admin",
                    "X-Trace" to "trace",
                ),
            )
            assertThat(calls).containsExactly("Key", "OAuth:[write:documents]", "OAuth:[admin:documents]")
            calls.clear()
            val countedMissing: (String) -> String? = {
                calls.add(it)
                null
            }
            assertThatThrownBy {
                alternative.invoke(
                    instance,
                    countedMissing,
                    token,
                    original,
                )
            }.hasCauseInstanceOf(IllegalStateException::class.java)
            assertThat(calls.count { it == "Key" }).isEqualTo(1)
            val multiple =
                type.getMethod(
                    "getMultipleWithApiKeyAndOAuth2Token",
                    Function1::class.java,
                    Function2::class.java,
                    Map::class.java,
                )
            assertThat(multiple.invoke(instance, key, token, emptyMap<String, String>())).isEqualTo(
                mapOf(
                    "X-API-Key" to "key-value",
                    "X-Other-Key" to "key-value",
                    "Authorization" to "Bearer token-value",
                ),
            )
            val bearer = type.getMethod("getBearerWithApiKeyAndBearerToken", Function1::class.java, Function1::class.java, Map::class.java)
            assertThat(bearer.invoke(instance, key, key, emptyMap<String, String>())).isEqualTo(
                mapOf(
                    "X-API-Key" to "key-value",
                    "Authorization" to "Bearer key-value",
                ),
            )
            assertThat(original).containsEntry("x-api-key", "old").containsEntry("AUTHORIZATION", "old-token")
        }
    }
}
