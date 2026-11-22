package com.cjbooms.fabrikt.servers.spring

import com.example.controllers.SearchController
import com.example.models.SearchRequest
import com.example.models.SearchResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.server.context.WebServerApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
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
                val uri = URI.create("http://127.0.0.1:$port/search")
                val client = HttpClient.newHttpClient()
                val query =
                    HttpRequest.newBuilder(uri)
                        .timeout(Duration.ofSeconds(5))
                        .header("Content-Type", "application/json")
                        .method("QUERY", HttpRequest.BodyPublishers.ofString("""{"name":"Ada"}"""))
                        .build()

                val queryResponse = client.send(query, HttpResponse.BodyHandlers.ofString())
                assertEquals(200, queryResponse.statusCode(), queryResponse.body())
                assertEquals("""{"name":"Ada"}""", queryResponse.body())

                val get = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(5)).GET().build()
                val getResponse = client.send(get, HttpResponse.BodyHandlers.ofString())
                assertEquals(200, getResponse.statusCode(), getResponse.body())
                assertEquals("""{"name":"GET"}""", getResponse.body())
            }
    }
}

@SpringBootConfiguration(proxyBeanMethods = false)
@EnableAutoConfiguration
@Import(QuerySearchController::class)
class QueryTestApplication

@Controller
class QuerySearchController : SearchController {
    override fun getSearch(): ResponseEntity<SearchResult> = ResponseEntity.ok(SearchResult("GET"))

    override fun search(searchRequest: SearchRequest): ResponseEntity<SearchResult> =
        ResponseEntity.ok(SearchResult(searchRequest.name))
}
