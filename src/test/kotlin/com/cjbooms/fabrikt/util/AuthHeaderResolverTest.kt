package com.cjbooms.fabrikt.util

import com.beust.jcommander.ParameterException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class AuthHeaderResolverTest {
    @Test
    fun `resolveHeaders substitutes env var placeholders`() {
        val headers =
            AuthHeaderResolver.resolveHeaders(
                listOf("Authorization: Bearer \${TOKEN}", "X-Other: \${MISSING:-default}"),
                env = { if (it == "TOKEN") "secret" else null },
            )

        assertThat(headers).containsExactly(
            "Authorization" to "Bearer secret",
            "X-Other" to "default",
        )
    }

    @Test
    fun `resolveHeaders prefers whole env var name over placeholder expansion`() {
        val headers =
            AuthHeaderResolver.resolveHeaders(
                listOf("Authorization: API_TOKEN"),
                env = { if (it == "API_TOKEN") "env-secret" else null },
            )

        assertThat(headers).containsExactly("Authorization" to "env-secret")
    }

    @Test
    fun `resolveHeaders runs shell command for values starting with exclamation mark`() {
        val headers = AuthHeaderResolver.resolveHeaders(listOf("Authorization: Bearer !echo smoke"))

        assertThat(headers).containsExactly("Authorization" to "Bearer smoke")
    }

    @Test
    fun `resolveHeaders runs shell command when exclamation mark is at value start`() {
        val headers = AuthHeaderResolver.resolveHeaders(listOf("Authorization: !echo token"))

        assertThat(headers).containsExactly("Authorization" to "token")
    }

    @Test
    fun `resolveHeaders keeps embedded exclamation mark as a literal token`() {
        val headers = AuthHeaderResolver.resolveHeaders(listOf("Authorization: Bearer abc!def"))

        assertThat(headers).containsExactly("Authorization" to "Bearer abc!def")
    }

    @Test
    fun `resolveHeaders throws when shell command fails`() {
        assertThatThrownBy { AuthHeaderResolver.resolveHeaders(listOf("Authorization: !false")) }
            .isInstanceOf(ParameterException::class.java)
            .hasMessageContaining("Auth command 'false' failed with exit code 1")
    }

    @Test
    fun `resolveHeaders throws for header without colon`() {
        assertThatThrownBy { AuthHeaderResolver.resolveHeaders(listOf("NoColon")) }
            .isInstanceOf(ParameterException::class.java)
            .hasMessageContaining("Invalid --auth 'NoColon'")
            .hasMessageContaining("Name: value")
    }

    @Test
    fun `shell command result is final and not re scanned for env vars`() {
        val headers = AuthHeaderResolver.resolveHeaders(listOf("Authorization: !echo '\${TOKEN}'"))

        assertThat(headers).containsExactly("Authorization" to "\${TOKEN}")
    }
}
