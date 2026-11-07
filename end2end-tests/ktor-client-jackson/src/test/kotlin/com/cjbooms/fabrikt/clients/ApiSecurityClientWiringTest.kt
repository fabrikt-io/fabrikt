package com.cjbooms.fabrikt.clients

import com.example.apisecurity.client.ApiConfiguration
import com.example.apisecurity.client.ApiSecurity
import com.example.apisecurity.client.Credential
import com.example.apisecurity.client.DocumentsClient
import com.example.apisecurity.client.StatusClient
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
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.jackson.jackson
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.net.ServerSocket

/**
 * Wires the generated declaration into the generated Ktor client: the resolver's header map travels
 * through `ApiConfiguration.customHeaders` to the wire, and `security: []` gains none.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiSecurityClientWiringTest {
    private val port = ServerSocket(0).use { it.localPort }
    private val wiremock = WireMockServer(options().port(port))
    private val baseUrl = "http://localhost:$port"

    private val credentials: (scheme: String, scopes: Set<String>) -> Credential? = { scheme, _ ->
        when (scheme) {
            "ApiKeyAuth" -> Credential.ApiKey("secret")
            "BearerAuth" -> Credential.Token("bearer-token")
            "OAuth" -> Credential.Token("oauth-token")
            else -> null
        }
    }

    private val httpClient = HttpClient(CIO) { install(ContentNegotiation) { jackson() } }
    private val documentsClient = DocumentsClient(httpClient)
    private val statusClient = StatusClient(httpClient)

    private val apiSecurityPlugin =
        createClientPlugin("ApiSecurity") {
            onRequest { request, _ ->
                val path = "/" + request.url.encodedPathSegments.joinToString("/")
                ApiSecurity.resolveFor(request.method.value, path, credentials)
                    .forEach { (name, value) -> request.headers[name] = value }
            }
        }
    private val pluginClient =
        DocumentsClient(
            HttpClient(CIO) {
                install(ContentNegotiation) { jackson() }
                install(apiSecurityPlugin)
            },
        )

    private fun authHeadersFor(operationId: String) =
        ApiSecurity.requirements.getValue(operationId).resolve(ApiSecurity.schemes, credentials)

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
    fun `headers reach the wire through apiConfiguration customHeaders`() {
        wiremock.stubFor(get(urlPathEqualTo("/documents")).willReturn(aResponse().withStatus(200)))

        runBlocking { documentsClient.getDocuments(apiConfiguration = ApiConfiguration(baseUrl, authHeadersFor("getDocuments"))) }

        wiremock.verify(getRequestedFor(urlPathEqualTo("/documents")).withHeader("X-API-Key", equalTo("secret")))
    }

    @Test
    fun `oauth2 scope resolves to a bearer header through customHeaders`() {
        wiremock.stubFor(get(urlPathEqualTo("/status")).willReturn(aResponse().withStatus(200)))

        runBlocking { statusClient.getStatus(apiConfiguration = ApiConfiguration(baseUrl, authHeadersFor("getStatus"))) }

        wiremock.verify(getRequestedFor(urlPathEqualTo("/status")).withHeader("Authorization", equalTo("Bearer oauth-token")))
    }

    @Test
    fun `a ktor plugin resolves credentials for the request it covers`() {
        wiremock.stubFor(get(urlPathEqualTo("/documents/42")).willReturn(aResponse().withStatus(200)))

        runBlocking { pluginClient.getDocumentById(id = "42", apiConfiguration = ApiConfiguration(baseUrl)) }

        wiremock.verify(
            getRequestedFor(urlPathEqualTo("/documents/42")).withHeader("Authorization", equalTo("Bearer bearer-token")),
        )
    }

    @Test
    fun `an operation with an empty security requirement gains no headers`() {
        wiremock.stubFor(delete(urlPathEqualTo("/documents/42")).willReturn(aResponse().withStatus(200)))

        runBlocking { documentsClient.deleteDocument(id = "42", apiConfiguration = ApiConfiguration(baseUrl, authHeadersFor("deleteDocument"))) }

        wiremock.verify(deleteRequestedFor(urlPathEqualTo("/documents/42")).withoutHeader("Authorization"))
        wiremock.verify(deleteRequestedFor(urlPathEqualTo("/documents/42")).withoutHeader("X-API-Key"))
    }
}