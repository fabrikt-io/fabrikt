package com.cjbooms.fabrikt.clients

import com.example.client.ExamplePath1Client
import com.example.models.FirstModel
import com.example.models.QueryResult
import com.example.nullable.models.InnerNotMergePatch
import com.example.nullable.models.TopLevelLevelMergePatchRef
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.marcinziolo.kotlin.wiremock.get
import com.marcinziolo.kotlin.wiremock.like
import com.marcinziolo.kotlin.wiremock.returns
import java.net.ServerSocket
import okhttp3.OkHttpClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.openapitools.jackson.nullable.JsonNullable
import org.openapitools.jackson.nullable.JsonNullableJackson3Module
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule

class Jackson3OkHttpClientTest {
    private val port: Int = ServerSocket(0).use { socket -> socket.localPort }
    private val wiremock = WireMockServer(options().port(port).notifier(ConsoleNotifier(true)))
    private val mapper = JsonMapper.builder()
        .addModule(kotlinModule())
        .addModule(JsonNullableJackson3Module())
        .build()
    private val client = ExamplePath1Client(mapper, "http://localhost:$port", OkHttpClient())

    @BeforeEach
    fun setUp() {
        wiremock.start()
    }

    @AfterEach
    fun tearDown() {
        wiremock.resetAll()
        wiremock.stop()
    }

    @Test
    fun `Jackson 3 serializes and deserializes an OkHttp response`() {
        val expected = QueryResult(listOf(FirstModel(id = "jackson-3")))
        wiremock.get {
            url like "/example-path-1"
        } returns {
            statusCode = 200
            body = mapper.writeValueAsString(expected)
        }

        val response = client.getExamplePath1()

        assertThat(response.data).isEqualTo(expected)
    }

    @Test
    fun `Jackson 3 supports generated JsonNullable models`() {
        val expected = TopLevelLevelMergePatchRef(
            inner = JsonNullable.of(InnerNotMergePatch(p = "jackson-3")),
        )

        val json = mapper.writeValueAsString(expected)
        val result = mapper.readValue(json, TopLevelLevelMergePatchRef::class.java)

        assertThat(result).isEqualTo(expected)
    }
}
