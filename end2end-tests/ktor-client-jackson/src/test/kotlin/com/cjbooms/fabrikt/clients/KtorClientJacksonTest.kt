package com.cjbooms.fabrikt.clients

import com.example.client.ApiConfiguration
import com.example.client.CatalogsItemsClient
import com.example.client.CatalogsSearchClient
import com.example.client.ItemsClient
import com.example.client.NetworkError
import com.example.client.NetworkResult
import com.example.client.NoContentClient
import com.example.models.Item
import com.example.models.SortOrder
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.marcinziolo.kotlin.wiremock.get
import com.marcinziolo.kotlin.wiremock.like
import com.marcinziolo.kotlin.wiremock.post
import com.marcinziolo.kotlin.wiremock.returns
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.jackson.jackson
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertInstanceOf
import java.net.ServerSocket
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KtorClientJacksonTest {

    private val port: Int = ServerSocket(0).use { socket -> socket.localPort }

    private val wiremock: WireMockServer = WireMockServer(options().port(port).notifier(ConsoleNotifier(true)))

    init {
        wiremock.start()
    }

    @BeforeEach
    fun setUp() {
        wiremock.resetAll()
    }

    private fun apiConfig() = ApiConfiguration(basePath = wiremock.baseUrl())

    private fun createHttpClient() = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson()
        }
        defaultRequest {
            url(wiremock.baseUrl())
        }
    }

    @Nested
    inner class Client {
        @Test
        fun `post request sends body and returns success`() {
            wiremock.post {
                urlPath like "/catalogs/catalog-a/items"
            } returns {
                statusCode = 201
                body = """{"id": "id-1", "name": "item-a", "description": "description-a", "price": 123.45}"""
                header = "Content-Type" to "application/json"
            }

            val client = CatalogsItemsClient(createHttpClient())

            runBlocking {
                val result = client.createItem(
                    item = Item(id = "id-1", name = "item-a", description = "description-a", price = 123.45),
                    catalogId = "catalog-a",
                    randomNumber = 123,
                    xRequestID = "request-id",
                    apiConfiguration = apiConfig()
                )

                assertInstanceOf<NetworkResult.Success<Item>>(result)
                assertEquals("item-a", result.data.name)
            }
        }

        @Test
        fun `post request body contains serialized data`() {
            wiremock.post {
                urlPath like "/catalogs/catalog-a/items"
            } returns {
                statusCode = 201
                body = """{"id": "id-1", "name": "item-a", "description": "description-a", "price": 123.45}"""
                header = "Content-Type" to "application/json"
            }

            val client = CatalogsItemsClient(createHttpClient())

            runBlocking {
                val result = client.createItem(
                    item = Item(id = "id-1", name = "item-a", description = "description-a", price = 123.45),
                    catalogId = "catalog-a",
                    randomNumber = 123,
                    xRequestID = "request-id",
                    apiConfiguration = apiConfig()
                )

                assertInstanceOf<NetworkResult.Success<Item>>(result)
            }

            wiremock.verify(
                postRequestedFor(urlPathEqualTo("/catalogs/catalog-a/items"))
                    .withRequestBody(matchingJsonPath("$.id", equalTo("id-1")))
                    .withRequestBody(matchingJsonPath("$.name", equalTo("item-a")))
                    .withRequestBody(matchingJsonPath("$.description", equalTo("description-a")))
                    .withRequestBody(matchingJsonPath("$.price", equalTo("123.45")))
            )
        }

        @Test
        fun `get request sends path query and header parameters`() {
            wiremock.get {
                urlPath like "/catalogs/catalog-a/search"
            } returns {
                statusCode = 200
                body = "[]"
                header = "Content-Type" to "application/json"
            }

            val client = CatalogsSearchClient(createHttpClient())

            runBlocking {
                val result = client.searchCatalogItems(
                    catalogId = "catalog-a",
                    query = "query",
                    page = 10,
                    sort = SortOrder.DESC,
                    xTracingID = "request-id-123",
                    apiConfiguration = apiConfig()
                )

                assertInstanceOf<NetworkResult.Success<List<Item>>>(result)
            }

            wiremock.verify(
                getRequestedFor(urlPathEqualTo("/catalogs/catalog-a/search"))
                    .withQueryParam("query", equalTo("query"))
                    .withQueryParam("page", equalTo("10"))
                    .withQueryParam("sort", equalTo("desc"))
                    .withHeader("X-Tracing-ID", equalTo("request-id-123"))
            )
        }

        @Test
        fun `get request sends list query parameters`() {
            wiremock.get {
                urlPath like "/catalogs/catalog-a/search"
            } returns {
                statusCode = 200
                body = "[]"
                header = "Content-Type" to "application/json"
            }

            val client = CatalogsSearchClient(createHttpClient())

            runBlocking {
                val result = client.searchCatalogItems(
                    catalogId = "catalog-a",
                    query = "query",
                    listParam = listOf("value1", "value2", "value3"),
                    apiConfiguration = apiConfig()
                )

                assertInstanceOf<NetworkResult.Success<List<Item>>>(result)
            }

            wiremock.verify(
                getRequestedFor(urlPathEqualTo("/catalogs/catalog-a/search"))
                    .withQueryParam("listParam", equalTo("value1"))
                    .withQueryParam("listParam", equalTo("value2"))
                    .withQueryParam("listParam", equalTo("value3"))
            )
        }

        @Test
        fun `get request omits null optional parameters`() {
            wiremock.get {
                urlPath like "/items"
            } returns {
                statusCode = 200
                body = "[]"
                header = "Content-Type" to "application/json"
            }

            val client = ItemsClient(createHttpClient())

            runBlocking {
                val result = client.getItems(apiConfiguration = apiConfig())

                assertInstanceOf<NetworkResult.Success<List<Item>>>(result)
            }

            wiremock.verify(
                getRequestedFor(urlPathEqualTo("/items"))
                    .withoutQueryParam("category")
                    .withoutQueryParam("limit")
                    .withoutQueryParam("priceLimit")
            )
        }

        @Test
        fun `get request includes provided optional parameters`() {
            wiremock.get {
                urlPath like "/items"
            } returns {
                statusCode = 200
                body = "[]"
                header = "Content-Type" to "application/json"
            }

            val client = ItemsClient(createHttpClient())

            runBlocking {
                val result = client.getItems(
                    category = "electronics",
                    limit = 50,
                    priceLimit = 99.99,
                    apiConfiguration = apiConfig()
                )

                assertInstanceOf<NetworkResult.Success<List<Item>>>(result)
            }

            wiremock.verify(
                getRequestedFor(urlPathEqualTo("/items"))
                    .withQueryParam("category", equalTo("electronics"))
                    .withQueryParam("limit", equalTo("50"))
                    .withQueryParam("priceLimit", equalTo("99.99"))
            )
        }

        @Test
        fun `get request returns list response`() {
            wiremock.get {
                urlPath like "/items"
            } returns {
                statusCode = 200
                body = """
                    [
                        {
                            "id": "item-1",
                            "name": "First Item",
                            "description": "Description 1",
                            "price": 10.99
                        },
                        {
                            "id": "item-2",
                            "name": "Second Item",
                            "description": "Description 2",
                            "price": 20.99
                        }
                    ]
                """.trimIndent()
                header = "Content-Type" to "application/json"
            }

            val client = ItemsClient(createHttpClient())

            runBlocking {
                val result = client.getItems(apiConfiguration = apiConfig())

                assertInstanceOf<NetworkResult.Success<List<Item>>>(result)
                val items = result.data
                assertEquals(2, items.size)
                assertEquals("item-1", items[0].id)
                assertEquals("First Item", items[0].name)
                assertEquals("item-2", items[1].id)
                assertEquals("Second Item", items[1].name)
            }
        }
    }

    @Nested
    inner class Result {
        @Test
        fun `200 returns Success with data`() {
            wiremock.post {
                urlPath like "/catalogs/catalog-a/items"
            } returns {
                statusCode = 200
                body = """
                    {
                        "id": "id-1",
                        "name": "item-a",
                        "description": "description-a",
                        "price": 123.45
                    }
                """.trimIndent()
                header = "Content-Type" to "application/json"
            }

            val client = CatalogsItemsClient(createHttpClient())

            runBlocking {
                val result = client.createItem(
                    item = Item(id = "id-1", name = "item-a", description = "description-a", price = 123.45),
                    catalogId = "catalog-a",
                    randomNumber = 123,
                    xRequestID = "request-id",
                    apiConfiguration = apiConfig()
                )

                assertInstanceOf<NetworkResult.Success<Item>>(result)
                assertEquals(
                    Item(id = "id-1", name = "item-a", description = "description-a", price = 123.45),
                    result.data
                )
            }
        }

        @Test
        fun `201 returns Success`() {
            wiremock.post {
                urlPath like "/catalogs/catalog-a/items"
            } returns {
                statusCode = 201
                body = """
                    {
                        "id": "id-1",
                        "name": "item-a",
                        "description": "description-a",
                        "price": 123.45
                    }
                """.trimIndent()
                header = "Content-Type" to "application/json"
            }

            val client = CatalogsItemsClient(createHttpClient())

            runBlocking {
                val result = client.createItem(
                    item = Item(id = "id-1", name = "item-a", description = "description-a", price = 123.45),
                    catalogId = "catalog-a",
                    randomNumber = 123,
                    xRequestID = "request-id",
                    apiConfiguration = apiConfig()
                )

                assertInstanceOf<NetworkResult.Success<Item>>(result)
            }
        }

        @Test
        fun `204 returns Success with Unit`() {
            wiremock.get {
                urlPath like "/no-content"
            } returns {
                statusCode = 204
                header = "Content-Type" to "application/json"
            }

            val client = NoContentClient(createHttpClient())

            runBlocking {
                val result = client.getNoContent(apiConfiguration = apiConfig())

                assertInstanceOf<NetworkResult.Success<Unit>>(result)
            }
        }

        @Test
        fun `302 returns Failure with Http error`() {
            wiremock.post {
                urlPath like "/catalogs/catalog-a/items"
            } returns {
                statusCode = 302
                header = "Content-Type" to "application/json"
                header = "Location" to "http://example.com/other-resource"
            }

            val client = CatalogsItemsClient(createHttpClient())

            runBlocking {
                val result = client.createItem(
                    item = Item(id = "id-1", name = "item-a", description = "description-a", price = 123.45),
                    catalogId = "catalog-a",
                    randomNumber = 123,
                    xRequestID = "request-id",
                    apiConfiguration = apiConfig()
                )

                assertInstanceOf<NetworkResult.Failure>(result)
                assertInstanceOf<NetworkError.Http>(result.error)
                assertEquals(302, result.error.statusCode)
            }
        }

        @Test
        fun `401 returns Failure with Http error`() {
            wiremock.post {
                urlPath like "/catalogs/catalog-a/items"
            } returns {
                statusCode = 401
                body = "Unauthorized"
            }

            val client = CatalogsItemsClient(createHttpClient())

            runBlocking {
                val result = client.createItem(
                    item = Item(id = "id-1", name = "item-a", description = "description-a", price = 123.45),
                    catalogId = "catalog-a",
                    randomNumber = 123,
                    xRequestID = "request-id",
                    apiConfiguration = apiConfig()
                )

                assertInstanceOf<NetworkResult.Failure>(result)
                assertInstanceOf<NetworkError.Http>(result.error)
                assertEquals(401, result.error.statusCode)
            }
        }

        @Test
        fun `403 returns Failure with Http error`() {
            wiremock.post {
                urlPath like "/catalogs/catalog-a/items"
            } returns {
                statusCode = 403
                body = "Forbidden"
            }

            val client = CatalogsItemsClient(createHttpClient())

            runBlocking {
                val result = client.createItem(
                    item = Item(id = "id-1", name = "item-a", description = "description-a", price = 123.45),
                    catalogId = "catalog-a",
                    randomNumber = 123,
                    xRequestID = "request-id",
                    apiConfiguration = apiConfig()
                )

                assertInstanceOf<NetworkResult.Failure>(result)
                assertInstanceOf<NetworkError.Http>(result.error)
                assertEquals(403, result.error.statusCode)
            }
        }

        @Test
        fun `404 returns Failure with Http error`() {
            wiremock.post {
                urlPath like "/catalogs/catalog-a/items"
            } returns {
                statusCode = 404
                body = "Not found"
            }

            val client = CatalogsItemsClient(createHttpClient())

            runBlocking {
                val result = client.createItem(
                    item = Item(id = "id-1", name = "item-a", description = "description-a", price = 123.45),
                    catalogId = "catalog-a",
                    randomNumber = 123,
                    xRequestID = "request-id",
                    apiConfiguration = apiConfig()
                )

                assertInstanceOf<NetworkResult.Failure>(result)
                assertInstanceOf<NetworkError.Http>(result.error)
                assertEquals(404, result.error.statusCode)
            }
        }

        @Test
        fun `500 returns Failure with Http error`() {
            wiremock.post {
                urlPath like "/catalogs/catalog-a/items"
            } returns {
                statusCode = 500
                body = "Internal Server Error"
            }

            val client = CatalogsItemsClient(createHttpClient())

            runBlocking {
                val result = client.createItem(
                    item = Item(id = "id-1", name = "item-a", description = "description-a", price = 123.45),
                    catalogId = "catalog-a",
                    randomNumber = 123,
                    xRequestID = "request-id",
                    apiConfiguration = apiConfig()
                )

                assertInstanceOf<NetworkResult.Failure>(result)
                assertInstanceOf<NetworkError.Http>(result.error)
                assertEquals(500, result.error.statusCode)
            }
        }

        @Test
        fun `503 returns Failure with Http error`() {
            wiremock.post {
                urlPath like "/catalogs/catalog-a/items"
            } returns {
                statusCode = 503
                body = "Service Unavailable"
            }

            val client = CatalogsItemsClient(createHttpClient())

            runBlocking {
                val result = client.createItem(
                    item = Item(id = "id-1", name = "item-a", description = "description-a", price = 123.45),
                    catalogId = "catalog-a",
                    randomNumber = 123,
                    xRequestID = "request-id",
                    apiConfiguration = apiConfig()
                )

                assertInstanceOf<NetworkResult.Failure>(result)
                assertInstanceOf<NetworkError.Http>(result.error)
                assertEquals(503, result.error.statusCode)
            }
        }

        @Test
        fun `HTTP error response body is captured`() {
            wiremock.post {
                urlPath like "/catalogs/catalog-a/items"
            } returns {
                statusCode = 400
                body = """{"error": "validation_failed", "details": "name is required"}"""
                header = "Content-Type" to "application/json"
            }

            val client = CatalogsItemsClient(createHttpClient())

            runBlocking {
                val result = client.createItem(
                    item = Item(id = "id-1", name = "item-a", description = "description-a", price = 123.45),
                    catalogId = "catalog-a",
                    randomNumber = 123,
                    xRequestID = "request-id",
                    apiConfiguration = apiConfig()
                )

                assertInstanceOf<NetworkResult.Failure>(result)
                assertInstanceOf<NetworkError.Http>(result.error)
                assertEquals(400, result.error.statusCode)
                assertEquals("Bad Request", result.error.statusDescription)
                assertEquals("""{"error": "validation_failed", "details": "name is required"}""", result.error.body)
            }
        }

        @Test
        fun `HTTP error with empty body has null body and status description`() {
            wiremock.post {
                urlPath like "/catalogs/catalog-a/items"
            } returns {
                statusCode = 404
                body = ""
            }

            val client = CatalogsItemsClient(createHttpClient())

            runBlocking {
                val result = client.createItem(
                    item = Item(id = "id-1", name = "item-a", description = "description-a", price = 123.45),
                    catalogId = "catalog-a",
                    randomNumber = 123,
                    xRequestID = "request-id",
                    apiConfiguration = apiConfig()
                )

                assertInstanceOf<NetworkResult.Failure>(result)
                assertInstanceOf<NetworkError.Http>(result.error)
                assertEquals(404, result.error.statusCode)
                assertEquals("Not Found", result.error.statusDescription)
                kotlin.test.assertNull(result.error.body)
            }
        }
    }

    @Nested
    inner class Error {
        @Test
        fun `wrong content type returns Serialization error`() {
            wiremock.get {
                urlPath like "/catalogs/catalog-a/search"
            } returns {
                statusCode = 200
                body = "[]"
                header = "Content-Type" to "text/plain"
            }

            runBlocking {
                val result = CatalogsSearchClient(createHttpClient())
                    .searchCatalogItems(catalogId = "catalog-a", query = "query", apiConfiguration = apiConfig())

                assertInstanceOf<NetworkResult.Failure>(result)
                assertInstanceOf<NetworkError.Serialization>(result.error)
                kotlin.test.assertNotNull(result.error.cause)
            }
        }

        @Test
        fun `empty response body returns Serialization error`() {
            wiremock.get {
                urlPath like "/catalogs/catalog-a/search"
            } returns {
                statusCode = 200
                body = ""
                header = "Content-Type" to "application/json"
            }

            runBlocking {
                val result = CatalogsSearchClient(createHttpClient())
                    .searchCatalogItems(catalogId = "catalog-a", query = "query", apiConfiguration = apiConfig())

                assertInstanceOf<NetworkResult.Failure>(result)
                assertInstanceOf<NetworkError.Serialization>(result.error)
            }
        }

        @Test
        fun `invalid response body returns Serialization error`() {
            wiremock.get {
                urlPath like "/catalogs/catalog-a/search"
            } returns {
                statusCode = 200
                body = "{}"
                header = "Content-Type" to "application/json"
            }

            runBlocking {
                val result = CatalogsSearchClient(createHttpClient())
                    .searchCatalogItems(catalogId = "catalog-a", query = "query", apiConfiguration = apiConfig())

                assertInstanceOf<NetworkResult.Failure>(result)
                assertInstanceOf<NetworkError.Serialization>(result.error)
            }
        }

        @Test
        fun `connection refused returns Network error`() {
            val unreachableClient = HttpClient(CIO) {
                install(ContentNegotiation) {
                    jackson()
                }
                defaultRequest {
                    url("http://localhost:1")
                }
            }

            val client = CatalogsSearchClient(unreachableClient)

            runBlocking {
                val result = client.searchCatalogItems(
                    catalogId = "catalog-a",
                    query = "query",
                    apiConfiguration = ApiConfiguration(basePath = "")
                )

                assertInstanceOf<NetworkResult.Failure>(result)
                assertInstanceOf<NetworkError.Network>(result.error)
            }
        }

        @Test
        fun `cancellation exception is rethrown`() {
            wiremock.stubFor(
                com.github.tomakehurst.wiremock.client.WireMock.get(
                    com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo("/catalogs/catalog-a/search")
                ).willReturn(
                    com.github.tomakehurst.wiremock.client.WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")
                        .withFixedDelay(5000)
                )
            )

            val client = CatalogsSearchClient(createHttpClient())

            org.junit.jupiter.api.assertThrows<kotlinx.coroutines.CancellationException> {
                runBlocking {
                    kotlinx.coroutines.withTimeout(100) {
                        client.searchCatalogItems(catalogId = "catalog-a", query = "query", apiConfiguration = apiConfig())
                    }
                }
            }
        }
    }
}
