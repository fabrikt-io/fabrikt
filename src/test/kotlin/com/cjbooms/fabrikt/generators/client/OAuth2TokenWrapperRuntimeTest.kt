package com.cjbooms.fabrikt.generators.client

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

class OAuth2TokenWrapperRuntimeTest {
    @TempDir
    lateinit var directory: Path

    @Test
    fun `compiled helpers pass scopes select alternatives and preserve headers`() {
        val headers = MAP.parameterizedBy(STRING, STRING)
        val client = TypeSpec.classBuilder("Client")
        val alternatives =
            listOf(
                OAuth2SecurityAlternative("OAuth", listOf("read")),
                OAuth2SecurityAlternative("OAuth", listOf("write")),
            )
        listOf("required" to true, "optional" to false, "unscoped" to true).forEach { (name, required) ->
            val function =
                FunSpec
                    .builder(name)
                    .addParameter(ParameterSpec.builder("additionalHeaders", headers).defaultValue("emptyMap()").build())
                    .returns(headers)
                    .addStatement("return additionalHeaders")
                    .build()
            val plan =
                OAuth2SecurityPlan(
                    if (name == "unscoped") listOf(OAuth2SecurityAlternative("OAuth", emptyList())) else alternatives,
                    tokenRequired = required,
                )
            client.addFunction(function).addFunction(function.withOAuth2TokenWrapper(plan, TokenWrapperTarget.ADDITIONAL_HEADERS))
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
            val calls = mutableListOf<Pair<String, Set<String>>>()
            val provider: (String, Set<String>) -> String? = { scheme, scopes ->
                calls.add(scheme to scopes)
                if (scopes == setOf("write")) "access-token" else " "
            }
            val original = mapOf("Authorization" to "existing", "X-Trace" to "trace")
            val required = type.getMethod("requiredWithOAuth2Token", Function2::class.java, Map::class.java)
            assertThat(required.invoke(instance, provider, original))
                .isEqualTo(mapOf("Authorization" to "Bearer access-token", "X-Trace" to "trace"))
            assertThat(calls).containsExactly("OAuth" to setOf("read"), "OAuth" to setOf("write"))
            val missing: (String, Set<String>) -> String? = { _, _ -> null }
            assertThatThrownBy { required.invoke(instance, missing, original) }
                .isInstanceOf(InvocationTargetException::class.java)
                .hasCauseInstanceOf(IllegalStateException::class.java)
            val optional = type.getMethod("optionalWithOAuth2Token", Function2::class.java, Map::class.java)
            assertThat(optional.invoke(instance, missing, original)).isEqualTo(original)
            val unscoped = type.getMethod("unscopedWithOAuth2Token", Function2::class.java, Map::class.java)
            val emptyScopes: (String, Set<String>) -> String? = { scheme, scopes ->
                assertThat(scheme).isEqualTo("OAuth")
                assertThat(scopes).isEmpty()
                "unscoped-token"
            }
            assertThat(unscoped.invoke(instance, emptyScopes, original))
                .isEqualTo(mapOf("Authorization" to "Bearer unscoped-token", "X-Trace" to "trace"))
            assertThat(original).containsEntry("Authorization", "existing")
        }
    }
}
