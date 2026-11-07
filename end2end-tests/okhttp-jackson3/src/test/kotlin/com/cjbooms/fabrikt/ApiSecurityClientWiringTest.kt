package com.cjbooms.fabrikt

import com.example.apisecurity.client.ApiSecurity
import com.example.apisecurity.client.Credential
import com.example.apisecurity.client.DocumentsClient
import com.example.apisecurity.client.resolve
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.delete
import com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule
import java.net.ServerSocket

/**
 * Wires the generated declaration into the generated OkHttp client: headers produced by `resolve` and
 * `resolveFor` must reach the wire, and an operation with `security: []` must gain none.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiSecurityClientWiringTest {
    private val port: Int = ServerSocket(0).use { socket -> socket.localPort }
    private val wiremock = WireMockServer(options().port(port))
    private val mapper = JsonMapper.builder().addModule(kotlinModule()).build()
    private val baseUrl = "http://localhost:$port"

    private val credentials: (scheme: String, scopes: Set<String>) -> Credential? = { scheme, _ ->
        when (scheme) {
            "ApiKeyAuth" -> Credential.ApiKey("secret")
            "BearerAuth" -> Credential.Token("bearer-token")
            else -> null
        }
    }

    private val resolveForInterceptor =
        Interceptor { chain ->
            val request = chain.request()
            val headers = ApiSecurity.resolveFor(request.method, request.url.encodedPath, credentials)
            chain.proceed(request.newBuilder().apply { headers.forEach { (name, value) -> header(name, value) } }.build())
        }

    private val interceptedClient = DocumentsClient(mapper, baseUrl, OkHttpClient.Builder().addInterceptor(resolveForInterceptor).build())

    @BeforeAll
    fun start() {
        wiremock.start()
    }

    @AfterAll
    fun stop() {
        wiremock.stop()
    }

    @BeforeEach
    fun reset() {
        wiremock.resetAll()
    }

    @Test
    fun `resolved headers reach the request through additionalHeaders`() {
        wiremock.stubFor(get(urlPathEqualTo("/documents")).willReturn(aResponse().withStatus(200)))

        DocumentsClient(mapper, baseUrl, OkHttpClient())
            .getDocuments(additionalHeaders = ApiSecurity.requirements.getValue("getDocuments").resolve(ApiSecurity.schemes, credentials))

        wiremock.verify(getRequestedFor(urlPathEqualTo("/documents")).withHeader("X-API-Key", equalTo("secret")))
    }

    @Test
    fun `an interceptor applies resolveFor to the request it covers`() {
        wiremock.stubFor(get(urlPathEqualTo("/documents/42")).willReturn(aResponse().withStatus(200)))

        interceptedClient.getDocumentById(id = "42")

        wiremock.verify(
            getRequestedFor(urlPathEqualTo("/documents/42")).withHeader("Authorization", equalTo("Bearer bearer-token")),
        )
    }

    @Test
    fun `an operation with an empty security requirement gains no headers`() {
        wiremock.stubFor(delete(urlPathEqualTo("/documents/42")).willReturn(aResponse().withStatus(200)))

        interceptedClient.deleteDocument(id = "42")

        wiremock.verify(deleteRequestedFor(urlPathEqualTo("/documents/42")).withoutHeader("Authorization"))
        wiremock.verify(deleteRequestedFor(urlPathEqualTo("/documents/42")).withoutHeader("X-API-Key"))
    }
}
