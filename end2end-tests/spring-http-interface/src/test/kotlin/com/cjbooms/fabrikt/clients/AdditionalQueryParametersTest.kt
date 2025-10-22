package com.cjbooms.fabrikt.clients

import com.example.client.ExamplePath1Client
import com.example.models.EnumQueryParam
import com.example.models.FirstModel
import com.example.models.QueryResult
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.marcinziolo.kotlin.wiremock.contains
import com.marcinziolo.kotlin.wiremock.get
import com.marcinziolo.kotlin.wiremock.like
import com.marcinziolo.kotlin.wiremock.returns
import java.net.ServerSocket
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.core.convert.converter.Converter
import org.springframework.core.convert.support.DefaultConversionService
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdditionalQueryParametersTest {
    private val port: Int = ServerSocket(0).use { socket -> socket.localPort }
    private val wiremock: WireMockServer = WireMockServer(
        WireMockConfiguration.options().port(port).notifier(ConsoleNotifier(true)))
    private val mapper = ObjectMapper()
    private val examplePath1Client: ExamplePath1Client = run {

        // TODO - Consider whether should be included in docs, or if we just using String in this client
        val customEnumConversionSvc = DefaultConversionService().apply {
            this.addConverter(object : Converter<Enum<*>, String> {
                override fun convert(source: Enum<*>): String {
                    return source.toString()
                }
            })
        }
        val restClient = RestClient.builder()
            .baseUrl("http://localhost:$port")
            .messageConverters(listOf(MappingJackson2HttpMessageConverter(mapper)))
            .build()
        val adapter = RestClientAdapter.create(restClient)
        val factory = HttpServiceProxyFactory.builderFor(adapter)
            .conversionService(customEnumConversionSvc)
            .build()
        factory.createClient()
    }

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
            header = "Content-Type" to "application/json"
            body = mapper.writeValueAsString(expectedResponse)
        }
        val result = examplePath1Client.getExamplePath1(additionalQueryParameters = mapOf("unspecified_param" to "some_value"))
        Assertions.assertThat(result).isEqualTo(expectedResponse)
    }

    @Test
    fun `enum query parameters are properly appended to requests`() {
        val expectedResponse = QueryResult(listOf(FirstModel(id = "the parameter was there!")))
        wiremock.get {
            urlPath like "/example-path-1"
            queryParams contains "enum_query_param" like "enum_value_1"
        } returns {
            statusCode = 200
            header = "Content-Type" to "application/json"
            body = mapper.writeValueAsString(expectedResponse)
        }
        val result = examplePath1Client.getExamplePath1(enumQueryParam = EnumQueryParam.ENUM_VALUE_1)
        Assertions.assertThat(result).isEqualTo(expectedResponse)
    }
}
