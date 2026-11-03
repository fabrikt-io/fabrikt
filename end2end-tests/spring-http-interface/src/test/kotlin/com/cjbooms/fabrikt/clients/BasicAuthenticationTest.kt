package com.cjbooms.fabrikt.clients

import com.example.basic.client.BasicClient
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.charset.Charset
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

class BasicAuthenticationTest {
    private val server = WireMockServer(options().dynamicPort())
    private val client by lazy {
        HttpServiceProxyFactory.builderFor(RestClientAdapter.create(RestClient.builder().baseUrl(server.baseUrl()).build()))
            .build().createClient(BasicClient::class.java)
    }

    @BeforeEach
    fun start() {
        server.start()
        server.stubFor(any(anyUrl()).willReturn(aResponse().withStatus(204)))
    }

    @AfterEach
    fun stop() {
        server.stop()
    }

    @Test
    fun `Basic credentials use the selected encoding and preserve unrelated headers`() {
        getBasic({ "Aladdin" to "open:sesame" }, headers = mapOf("authorization" to "old", "X-Trace" to "trace"))
        val request = server.allServeEvents.single().request
        assertThat(request.getHeader("Authorization")).isEqualTo("Basic QWxhZGRpbjpvcGVuOnNlc2FtZQ==")
        assertThat(request.headers.getHeader("Authorization").values()).hasSize(1)
        assertThat(request.getHeader("X-Trace")).isEqualTo("trace")
    }

    @Test
    fun `Basic combines with an API key and anonymous operations stay anonymous`() {
        createBasic()
        val request = server.allServeEvents.single().request
        assertThat(request.getHeader("Authorization")).isEqualTo("Basic dXNlcjpwYXNz")
        assertThat(request.getHeader("X-API-Key")).isEqualTo("key")
        server.resetRequests()
        deleteBasic()
        assertThat(server.allServeEvents.single().request.getHeader("Authorization")).isNull()
        assertThat(server.allServeEvents.single().request.getHeader("X-API-Key")).isNull()
    }

    @Test
    fun `missing or malformed credentials fail before making a request`() {
        assertThatThrownBy { getBasic({ null }) }.isInstanceOf(IllegalStateException::class.java)
        listOf("bad:user" to "pass", "user" to "bad\npass", "user" to "\u0000").forEach { credentials ->
            assertThatThrownBy { getBasic({ credentials }) }.isInstanceOf(IllegalArgumentException::class.java)
        }
        assertThatThrownBy { getBasic({ "用户" to "pass" }) }.isInstanceOf(IllegalArgumentException::class.java)
        assertThat(server.allServeEvents).isEmpty()
    }

    @Test
    fun `UTF8 and empty credentials are supported explicitly`() {
        getBasic({ "ü" to "päss" }, Charsets.UTF_8)
        assertThat(server.allServeEvents.single().request.getHeader("Authorization")).isEqualTo("Basic w7w6cMOkc3M=")
        server.resetRequests()
        getBasic({ "" to "" })
        assertThat(server.allServeEvents.single().request.getHeader("Authorization")).isEqualTo("Basic Og==")
    }

    private fun getBasic(provider: (String) -> Pair<String, String>?, charset: Charset = Charsets.ISO_8859_1, headers: Map<String, String> = emptyMap()) {
        client.getBasicWithBasicCredentials(provider, basicCredentialsCharset = charset, additionalHeaders = headers)
    }

    private fun createBasic() {
        client.createBasicWithApiKeyAndBasicCredentials(apiKeyProvider = { "key" }, basicCredentialsProvider = { "user" to "pass" })
    }

    private fun deleteBasic() {
        client.deleteBasic()
    }
}
