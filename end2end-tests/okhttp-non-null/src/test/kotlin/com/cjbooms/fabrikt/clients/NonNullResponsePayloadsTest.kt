package com.cjbooms.fabrikt.clients

import com.example.client.ApiClientException
import com.example.client.ApiException
import com.example.client.WidgetsClient
import com.example.client.WidgetsImageClient
import com.example.client.WidgetsSummaryClient
import com.example.models.Widget
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.marcinziolo.kotlin.wiremock.delete
import com.marcinziolo.kotlin.wiremock.like
import com.marcinziolo.kotlin.wiremock.returns
import java.net.ServerSocket
import okhttp3.OkHttpClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NonNullResponsePayloadsTest {
    private val port: Int = ServerSocket(0).use { socket -> socket.localPort }
    private val wiremock: WireMockServer = WireMockServer(options().port(port).notifier(ConsoleNotifier(true)))
    private val mapper = jacksonObjectMapper()
    private val httpClient = OkHttpClient.Builder().build()
    private val widgetsClient = WidgetsClient(mapper, "http://localhost:$port", httpClient)
    private val widgetsImageClient = WidgetsImageClient(mapper, "http://localhost:$port", httpClient)
    private val widgetsSummaryClient = WidgetsSummaryClient(mapper, "http://localhost:$port", httpClient)

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
    fun `a valid json response body deserializes into non-null data`() {
        val expectedWidget = Widget(id = "1", name = "gadget")
        wiremock.stubFor(
            get(urlPathEqualTo("/widgets/1"))
                .willReturn(aResponse().withStatus(200).withBody(mapper.writeValueAsString(expectedWidget)))
        )

        val result = widgetsClient.getWidget("1")

        assertThat(result.statusCode).isEqualTo(200)
        assertThat(result.data).isEqualTo(expectedWidget)
    }

    @Test
    fun `an empty response body on an operation with a declared response throws ApiException`() {
        wiremock.stubFor(get(urlPathEqualTo("/widgets/1")).willReturn(aResponse().withStatus(200)))

        assertThatThrownBy { widgetsClient.getWidget("1") }
            .isExactlyInstanceOf(ApiException::class.java)
            .hasMessage("[200]: Response body expected but not returned")
    }

    @Test
    fun `a json null response body throws ApiException`() {
        wiremock.stubFor(
            get(urlPathEqualTo("/widgets/1")).willReturn(aResponse().withStatus(200).withBody("null"))
        )

        assertThatThrownBy { widgetsClient.getWidget("1") }
            .isExactlyInstanceOf(ApiException::class.java)
            .hasMessage("[200]: Response body deserialized to null")
    }

    @Test
    fun `a malformed response body throws the raw deserialization exception`() {
        wiremock.stubFor(
            get(urlPathEqualTo("/widgets/1")).willReturn(aResponse().withStatus(200).withBody("{ not json"))
        )

        assertThatThrownBy { widgetsClient.getWidget("1") }
            .isExactlyInstanceOf(JsonParseException::class.java)
    }

    @Test
    fun `an operation without a response schema returns Unit data`() {
        wiremock.delete {
            urlPath like "/widgets/1"
        } returns {
            statusCode = 204
        }

        val result = widgetsClient.deleteWidget("1")

        assertThat(result.statusCode).isEqualTo(204)
        assertThat(result.data).isEqualTo(Unit)
    }

    @Test
    fun `an error status on an operation without a response schema throws ApiClientException`() {
        wiremock.delete {
            urlPath like "/widgets/1"
        } returns {
            statusCode = 404
        }

        assertThatThrownBy { widgetsClient.deleteWidget("1") }
            .isInstanceOfSatisfying(ApiClientException::class.java) { e ->
                assertThat(e.statusCode).isEqualTo(404)
            }
    }

    @Test
    fun `an operation with multiple json success schemas returns the parsed json tree`() {
        wiremock.stubFor(
            get(urlPathEqualTo("/widgets/1/summary"))
                .willReturn(aResponse().withStatus(202).withBody("""{"name":"gizmo"}"""))
        )

        val result = widgetsSummaryClient.getWidgetSummary("1")

        assertThat(result.statusCode).isEqualTo(202)
        assertThat(result.data).isEqualTo(mapper.readTree("""{"name":"gizmo"}"""))
    }

    @Test
    fun `a binary response body returns non-null bytes`() {
        val expectedBytes = byteArrayOf(0x25, 0x50, 0x44, 0x46)
        wiremock.stubFor(
            get(urlPathEqualTo("/widgets/1/image")).willReturn(aResponse().withStatus(200).withBody(expectedBytes))
        )

        val result = widgetsImageClient.getWidgetImage("1")

        assertThat(result.statusCode).isEqualTo(200)
        assertThat(result.data).isEqualTo(expectedBytes)
    }

    @Test
    fun `an empty binary response body returns an empty byte array`() {
        wiremock.stubFor(get(urlPathEqualTo("/widgets/1/image")).willReturn(aResponse().withStatus(200)))

        val result = widgetsImageClient.getWidgetImage("1")

        assertThat(result.statusCode).isEqualTo(200)
        assertThat(result.data).isEqualTo(ByteArray(0))
    }
}
