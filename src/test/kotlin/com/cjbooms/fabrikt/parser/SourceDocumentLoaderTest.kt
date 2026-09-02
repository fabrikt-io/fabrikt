package com.cjbooms.fabrikt.parser

import com.sun.net.httpserver.HttpServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress
import java.net.URI
import java.nio.charset.StandardCharsets

class SourceDocumentLoaderTest {
    private lateinit var server: HttpServer

    @BeforeEach
    fun startServer() {
        server = HttpServer.create(InetSocketAddress("localhost", 0), 0)
        server.start()
    }

    @AfterEach
    fun stopServer() {
        server.stop(0)
    }

    @Test
    fun `loads HTTP documents with configured headers`() {
        var authorizationHeader: String? = null
        server.createContext("/schema.yaml") { exchange ->
            authorizationHeader = exchange.requestHeaders.getFirst("Authorization")
            val body = "type: string".toByteArray(StandardCharsets.UTF_8)
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        val uri = URI("http://localhost:${server.address.port}/schema.yaml")

        val content = DefaultSourceDocumentLoader(listOf("Authorization" to "Bearer token")).load(uri)

        assertThat(content).isEqualTo("type: string")
        assertThat(authorizationHeader).isEqualTo("Bearer token")
    }
}
