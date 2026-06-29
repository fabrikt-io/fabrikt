package com.cjbooms.fabrikt.util

import com.cjbooms.fabrikt.util.KaizenParserExtensions.basePath
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

class KaizenParserExtensionsTest {

    private fun apiWithServerUrl(url: String?): com.reprezen.kaizen.oasparser.model3.OpenApi3 {
        val serverBlock = if (url == null) "" else "servers:\n  - url: \"$url\"\n"
        val spec = serverBlock +
            """
            |openapi: 3.1.0
            |info:
            |  title: test
            |  version: 1.0.0
            |paths:
            |  /ping:
            |    get:
            |      operationId: ping
            |      responses:
            |        '200':
            |          description: ok
            """.trimMargin()
        return YamlUtils.parseOpenApi(spec)
    }

    @Test
    fun `basePath returns the path portion of a normal server url`() {
        assertThat(apiWithServerUrl("https://api.example.com/v2/").basePath()).isEqualTo("/v2")
        assertThat(apiWithServerUrl("https://api.example.com/v2").basePath()).isEqualTo("/v2")
    }

    @Test
    fun `basePath returns empty when there are no servers`() {
        assertThat(apiWithServerUrl(null).basePath()).isEqualTo("")
    }

    @Test
    fun `basePath does not throw on a templated server url and returns empty`() {
        // Templated server URLs (issue #596) contain '{' and '}', which are illegal URI characters
        // and previously made URI.create() throw, aborting the whole code generation run.
        val api = apiWithServerUrl("https://{username}.gigantic-server.com:{port}/{basePath}")
        assertDoesNotThrow { api.basePath() }
        assertThat(api.basePath()).isEqualTo("")
    }
}
