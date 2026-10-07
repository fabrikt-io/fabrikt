package com.cjbooms.fabrikt.clients

import com.example.client.ExamplePath1Client
import com.example.models.FirstModel
import com.example.models.QueryResult
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.marcinziolo.kotlin.wiremock.contains
import com.marcinziolo.kotlin.wiremock.get
import com.marcinziolo.kotlin.wiremock.like
import com.marcinziolo.kotlin.wiremock.returns
import java.net.ServerSocket
import okhttp3.OkHttpClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdditionalQueryParametersTest {
    private val port: Int = ServerSocket(0).use { socket -> socket.localPort }
    private val wiremock: WireMockServer = WireMockServer(options().port(port).notifier(ConsoleNotifier(true)))
    private val mapper = JsonMapper.builder().addModule(kotlinModule()).build()
    private val httpClient = OkHttpClient.Builder().build()
    private val examplePath1Client = ExamplePath1Client(mapper, "http://localhost:$port", httpClient)

    @BeforeEach
    fun setUp() {
        wiremock.start()
    }

    @AfterEach
    fun afterEach() {
        wiremock.resetAll()
        wiremock.stop()
    }

    @Test
    fun `additional query parameters are properly appended to requests`() {
        val expectedResponse = QueryResult(listOf(FirstModel(id = "the parameter was there!")))
        wiremock.get {
            urlPath like "/example-path-1"
            queryParams contains "unspecified_param" like "some_value"
        } returns {
            statusCode = 200
            body = mapper.writeValueAsString(expectedResponse)
        }
        val result = examplePath1Client.getExamplePath1(additionalQueryParameters = mapOf("unspecified_param" to "some_value"))
        assertThat(result.data).isEqualTo(expectedResponse)
    }
}
