package com.cjbooms.fabrikt.clients.okio3

import com.example.client.*
import com.example.models.EnumQueryParam
import com.example.models.Failure
import com.example.models.FirstModel
import com.example.models.QueryResult
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.marcinziolo.kotlin.wiremock.*
import okhttp3.OkHttpClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.net.ServerSocket
import java.util.*
import java.util.stream.Stream


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Okio3Test {
    private val port: Int = ServerSocket(0).use { socket -> socket.localPort }

    private val wiremock: WireMockServer = WireMockServer(options().port(port).notifier(ConsoleNotifier(true)))

    private val mapper = ObjectMapper()
    private val httpClient = OkHttpClient.Builder().build()
    private val examplePath1Client = ExamplePath1Client(mapper, "http://localhost:$port", httpClient)
    private val examplePath2Client = ExamplePath2Client(mapper, "http://localhost:$port", httpClient)
    private val examplePath3Client = ExamplePath3SubresourceClient(mapper, "http://localhost:$port", httpClient)

    private val uuid = UUID.randomUUID()
    private val failure = Failure(traceId = uuid,
        error = "testError",
        errorCode = "testErrorCode")

    @Suppress("unused")
    private fun path2ErrorCodes(): Stream<Int> = Stream.of(400, 422, 423)

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
    fun `throws an exception if 404 is returned`() {
        wiremock.get {
           url like "/example-path-1"
        } returns {
            statusCode = 404
        }

        val result = assertThrows<ApiClientException> {
            examplePath1Client.getExamplePath1()
        }
        assertThat(result.statusCode).isEqualTo(404)
    }

    @Test
    fun `returns data when no query parameters are send`(testInfo: TestInfo) {
        wiremock.get {
            url like "/example-path-1"
        } returns {
            statusCode = 200
            body = mapper.writeValueAsString(
                QueryResult(
                    listOf(FirstModel(id = testInfo.displayName))
                )
            )
        }

        val result = examplePath1Client.getExamplePath1()

        assertThat(result.data).isEqualTo(
            QueryResult(
                listOf(FirstModel(id = testInfo.displayName))
            )
        )
    }

    @Test
    fun `adds query_param2 to the query`(testInfo: TestInfo) {
        wiremock.get {
            urlPath like "/example-path-1"
            queryParams contains "query_param2" like "10"
        } returns {
            statusCode = 200
            body = mapper.writeValueAsString(
                QueryResult(
                    listOf(FirstModel(id = testInfo.displayName))
                )
            )
        }

        val result = examplePath1Client.getExamplePath1(queryParam2 = 10)

        assertThat(result.data).isEqualTo(
            QueryResult(
                listOf(FirstModel(id = testInfo.displayName))
            )
        )
    }

    @Test
    fun `adds enum_query_param to the query`(testInfo: TestInfo) {
        wiremock.get {
            urlPath like "/example-path-1"
            queryParams contains "enum_query_param" like "enum_value_1"
        } returns {
            statusCode = 200
            body = mapper.writeValueAsString(
                QueryResult(
                    emptyList()
                )
            )
        }

        examplePath1Client.getExamplePath1(enumQueryParam = EnumQueryParam.ENUM_VALUE_1)
    }

    @Test
    fun `adds explode_list_query_param to the query`(testInfo: TestInfo) {
        wiremock.get {
            urlPath like "/example-path-1"
            queryParams contains "explode_list_query_param" like "list"
            queryParams contains "explode_list_query_param" like "of"
            queryParams contains "explode_list_query_param" like "parameters"
        } returns {
            statusCode = 200
            body = mapper.writeValueAsString(
                QueryResult(
                    listOf(FirstModel(id = testInfo.displayName))
                )
            )
        }

        val result = examplePath1Client.getExamplePath1(explodeListQueryParam = listOf("list", "of", "parameters"))

        assertThat(result.data).isEqualTo(
            QueryResult(
                listOf(FirstModel(id = testInfo.displayName))
            )
        )
    }

    @Test
    fun `adds additional headers to the query`(testInfo: TestInfo) {
        wiremock.get {
            urlPath like "/example-path-1"
            headers contains "awesome" like "header"
        } returns {
            statusCode = 200
            body = mapper.writeValueAsString(
                QueryResult(
                    listOf(FirstModel(id = testInfo.displayName))
                )
            )
        }

        val result = examplePath1Client.getExamplePath1(additionalHeaders = mapOf("awesome" to "header"))

        assertThat(result.data).isEqualTo(
            QueryResult(
                listOf(FirstModel(id = testInfo.displayName))
            )
        )
    }

    @Test
    fun `send body with post request`(testInfo: TestInfo) {
        val content = FirstModel(id = testInfo.displayName)
        wiremock.post {
            urlPath like "/example-path-1"
            body equalTo mapper.writeValueAsString(content)
        } returns {
            statusCode = 201
        }

        val result = examplePath1Client.postExamplePath1(content)
        assertThat(result.statusCode).isEqualTo(201)
    }

    @ParameterizedTest
    @MethodSource("path2ErrorCodes")
    fun `throws an exception if a 4xx http status code is returned`(errorCode: Int) {
        wiremock.get {
            urlPath like "/example-path-2/$errorCode"
        } returns {
            statusCode = errorCode
            body = mapper.writeValueAsString(failure)
        }

        val result = assertThrows<ApiClientException> {
            examplePath2Client.getExamplePath2PathParam(errorCode.toString(), 10)
        }

        assertThat(result.statusCode).isEqualTo(errorCode)
        assertThat(mapper.readValue(result.message, Failure::class.java)).isEqualTo(failure)
    }

    @Test
    fun `throws an exception if a http status code 500 is returned`() {
        wiremock.get {
            urlPath like "/example-path-2/500"
        } returns {
            statusCode = 500
            body = mapper.writeValueAsString(failure)
        }

        val result = assertThrows<ApiServerException> {
            examplePath2Client.getExamplePath2PathParam("500", 10)
        }

        assertThat(result.statusCode).isEqualTo(500)
        assertThat(mapper.readValue(result.message, Failure::class.java)).isEqualTo(failure)
    }

    @Test
    fun `throws an exception if a http status code 304 is returned`() {
        wiremock.get {
            urlPath like "/example-path-2/304"
        } returns {
            statusCode = 304
        }

        val result = assertThrows<ApiRedirectException> {
            examplePath2Client.getExamplePath2PathParam("304", 10)
        }

        assertThat(result.statusCode).isEqualTo(304)
    }


    @Test
    fun `head returns 200`() {
        wiremock.head {
            urlPath like "/example-path-2/head200"
        } returns {
            statusCode = 200
        }

        val result = examplePath2Client.headOperationIdExample("head200")

        assertThat(result.statusCode).isEqualTo(200)
    }

    @Test
    fun `put returns 204`() {
        val model = FirstModel(id = "put", secondAttr = "204")
        wiremock.put {
            urlPath like "/example-path-2/put204"
            body equalTo mapper.writeValueAsString(model)
            headers contains "If-Match" like "match"
        } returns {
            statusCode = 204
        }

        val result = examplePath2Client.putExamplePath2PathParam(firstModel = model, pathParam = "put204", ifMatch = "match")

        assertThat(result.statusCode).isEqualTo(204)
    }

    @Test
    fun `put returns 204 with sub resource`() {
        val model = FirstModel(id = "put", secondAttr = "304")
        wiremock.put {
            urlPath like "/example-path-3/put304/subresource"
            body equalTo mapper.writeValueAsString(model)
            headers contains "If-Match" like "match"
        } returns {
            statusCode = 204
        }

        val result = examplePath3Client.putExamplePath3PathParamSubresource(firstModel = model, pathParam = "put304", ifMatch = "match")

        assertThat(result.statusCode).isEqualTo(204)
    }
}
