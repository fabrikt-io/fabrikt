package com.cjbooms.fabrikt.servers.spring

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.server.context.WebServerApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class QueryRoutingTest {
    @Test
    fun `routes QUERY with a JSON body alongside GET on the same path`() {
        SpringApplicationBuilder(QueryTestApplication::class.java)
            .run("--server.port=0", "--server.address=127.0.0.1")
            .use { context ->
                val port = requireNotNull((context as WebServerApplicationContext).webServer).port
                val uri = URI.create("http://127.0.0.1:$port/example")
                val client = HttpClient.newHttpClient()
                val json = ObjectMapper()
                val query =
                    HttpRequest.newBuilder(uri)
                        .timeout(Duration.ofSeconds(5))
                        .header("Content-Type", "application/json")
                        .method("QUERY", HttpRequest.BodyPublishers.ofString("""{"filter":"Ada"}"""))
                        .build()

                val queryResponse = client.send(query, HttpResponse.BodyHandlers.ofString())
                assertEquals(200, queryResponse.statusCode(), queryResponse.body())
                assertEquals("Ada", json.readTree(queryResponse.body()).path("filter").asText())

                val get = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(5)).GET().build()
                val getResponse = client.send(get, HttpResponse.BodyHandlers.ofString())
                assertEquals(200, getResponse.statusCode(), getResponse.body())
                assertEquals("paramValue", json.readTree(getResponse.body()).path("param1").asText())
            }
    }
}

@Configuration(proxyBeanMethods = false)
@EnableAutoConfiguration
@Import(ExampleControllerImpl::class)
class QueryTestApplication
