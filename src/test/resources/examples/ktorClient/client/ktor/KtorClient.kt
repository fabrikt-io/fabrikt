package examples.ktorClient.client

import examples.ktorClient.models.Item
import examples.ktorClient.models.SortOrder
import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.`get`
import io.ktor.client.request.`header`
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.serialization.ContentConvertException
import kotlinx.coroutines.CancellationException
import java.io.IOException
import kotlin.Double
import kotlin.Int
import kotlin.String
import kotlin.Unit
import kotlin.collections.List

public class ItemsClient(
    private val httpClient: HttpClient,
) {
    /**
     * Retrieve a list of items
     *
     * Parameters:
     * 	 @param limit Maximum number of items to return
     * 	 @param category Filter items by category
     * 	 @param priceLimit Maximum price of items to return
     *
     * Returns:
     * 	[NetworkResult.Success] with [kotlin.collections.List<examples.ktorClient.models.Item>] if the
     * request was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun getItems(
        limit: Int? = null,
        category: String? = null,
        priceLimit: Double? = null,
        apiConfiguration: ApiConfiguration = ApiConfiguration(),
    ): NetworkResult<List<Item>> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url =
            buildString {
                append(basePath)
                append("""/items""")
                val params =
                    buildList {
                        limit?.let { add("limit=$it") }
                        category?.let { add("category=$it") }
                        priceLimit?.let { add("priceLimit=$it") }
                    }
                if (params.isNotEmpty()) append("?").append(params.joinToString("&"))
            }

        return try {
            val response =
                httpClient.`get`(url) {
                    `header`("Accept", "application/json")
                    headers {
                        apiConfiguration.customHeaders.forEach { (name, value) ->
                            remove(name)
                            append(name, value)
                        }
                    }
                }

            if (response.status.isSuccess()) {
                NetworkResult.Success(response.body())
            } else {
                val errorBody = response.bodyAsText().ifBlank { null }
                NetworkResult.Failure(
                    NetworkError.Http(
                        statusCode = response.status.value,
                        statusDescription = response.status.description,
                        body = errorBody,
                    ),
                )
            }
        } catch (e: ResponseException) {
            val status = e.response.status
            val body = runCatching { e.response.bodyAsText() }.getOrNull()?.ifBlank { null }
            NetworkResult.Failure(NetworkError.Http(status.value, status.description, body))
        } catch (e: IOException) {
            NetworkResult.Failure(NetworkError.Network(e))
        } catch (e: ContentConvertException) {
            NetworkResult.Failure(NetworkError.Serialization(e))
        } catch (e: NoTransformationFoundException) {
            NetworkResult.Failure(NetworkError.Serialization(e))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            NetworkResult.Failure(NetworkError.Unknown(e))
        }
    }
}

public class CatalogsItemsClient(
    private val httpClient: HttpClient,
) {
    /**
     * Create a new item
     *
     * Parameters:
     * 	 @param item The item to create
     * 	 @param catalogId The ID of the catalog
     * 	 @param randomNumber Just a test query param
     * 	 @param xRequestID Unique identifier for the request
     * 	 @param xTracingID Unique identifier for the tracing
     *
     * Returns:
     * 	[NetworkResult.Success] with [examples.ktorClient.models.Item] if the request was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun createItem(
        item: Item,
        catalogId: String,
        randomNumber: Int,
        xRequestID: String,
        xTracingID: String? = null,
        apiConfiguration: ApiConfiguration = ApiConfiguration(),
    ): NetworkResult<Item> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url =
            buildString {
                append(basePath)
                append("""/catalogs/$catalogId/items""")
                val params =
                    buildList {
                        add("randomNumber=$randomNumber")
                    }
                if (params.isNotEmpty()) append("?").append(params.joinToString("&"))
            }

        return try {
            val response =
                httpClient.post(url) {
                    `header`("Accept", "application/json")
                    `header`("Content-Type", "application/json")
                    setBody(item)
                    `header`("X-Request-ID", xRequestID)
                    `header`("X-Tracing-ID", xTracingID)
                    headers {
                        apiConfiguration.customHeaders.forEach { (name, value) ->
                            remove(name)
                            append(name, value)
                        }
                    }
                }

            if (response.status.isSuccess()) {
                NetworkResult.Success(response.body())
            } else {
                val errorBody = response.bodyAsText().ifBlank { null }
                NetworkResult.Failure(
                    NetworkError.Http(
                        statusCode = response.status.value,
                        statusDescription = response.status.description,
                        body = errorBody,
                    ),
                )
            }
        } catch (e: ResponseException) {
            val status = e.response.status
            val body = runCatching { e.response.bodyAsText() }.getOrNull()?.ifBlank { null }
            NetworkResult.Failure(NetworkError.Http(status.value, status.description, body))
        } catch (e: IOException) {
            NetworkResult.Failure(NetworkError.Network(e))
        } catch (e: ContentConvertException) {
            NetworkResult.Failure(NetworkError.Serialization(e))
        } catch (e: NoTransformationFoundException) {
            NetworkResult.Failure(NetworkError.Serialization(e))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            NetworkResult.Failure(NetworkError.Unknown(e))
        }
    }
}

public class ItemsSubitemsClient(
    private val httpClient: HttpClient,
) {
    /**
     * Retrieve a specific subitem of an item
     *
     * Parameters:
     * 	 @param itemId The ID of the item
     * 	 @param subItemId The ID of the subitem
     *
     * Returns:
     * 	[NetworkResult.Success] with [examples.ktorClient.models.Item] if the request was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun getSubItem(
        itemId: String,
        subItemId: String,
        apiConfiguration: ApiConfiguration = ApiConfiguration(),
    ): NetworkResult<Item> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url = basePath + """/items/$itemId/subitems/$subItemId"""

        return try {
            val response =
                httpClient.`get`(url) {
                    `header`("Accept", "application/json")
                    headers {
                        apiConfiguration.customHeaders.forEach { (name, value) ->
                            remove(name)
                            append(name, value)
                        }
                    }
                }

            if (response.status.isSuccess()) {
                NetworkResult.Success(response.body())
            } else {
                val errorBody = response.bodyAsText().ifBlank { null }
                NetworkResult.Failure(
                    NetworkError.Http(
                        statusCode = response.status.value,
                        statusDescription = response.status.description,
                        body = errorBody,
                    ),
                )
            }
        } catch (e: ResponseException) {
            val status = e.response.status
            val body = runCatching { e.response.bodyAsText() }.getOrNull()?.ifBlank { null }
            NetworkResult.Failure(NetworkError.Http(status.value, status.description, body))
        } catch (e: IOException) {
            NetworkResult.Failure(NetworkError.Network(e))
        } catch (e: ContentConvertException) {
            NetworkResult.Failure(NetworkError.Serialization(e))
        } catch (e: NoTransformationFoundException) {
            NetworkResult.Failure(NetworkError.Serialization(e))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            NetworkResult.Failure(NetworkError.Unknown(e))
        }
    }
}

public class CatalogsSearchClient(
    private val httpClient: HttpClient,
) {
    /**
     * Search for items
     *
     * Parameters:
     * 	 @param catalogId The ID of the catalog
     * 	 @param query The search query
     * 	 @param page Page number
     * 	 @param sort Sort order
     * 	 @param listParam A list parameter
     * 	 @param xTracingID Unique identifier for the tracing
     *
     * Returns:
     * 	[NetworkResult.Success] with [kotlin.collections.List<examples.ktorClient.models.Item>] if the
     * request was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun searchCatalogItems(
        catalogId: String,
        query: String,
        page: Int? = null,
        sort: SortOrder? = null,
        listParam: List<String>? = null,
        xTracingID: String? = null,
        apiConfiguration: ApiConfiguration = ApiConfiguration(),
    ): NetworkResult<List<Item>> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url =
            buildString {
                append(basePath)
                append("""/catalogs/$catalogId/search""")
                val params =
                    buildList {
                        add("query=$query")
                        page?.let { add("page=$it") }
                        sort?.let { add("sort=$it") }
                        listParam?.forEach { add("listParam=$it") }
                    }
                if (params.isNotEmpty()) append("?").append(params.joinToString("&"))
            }

        return try {
            val response =
                httpClient.`get`(url) {
                    `header`("Accept", "application/json")
                    `header`("X-Tracing-ID", xTracingID)
                    headers {
                        apiConfiguration.customHeaders.forEach { (name, value) ->
                            remove(name)
                            append(name, value)
                        }
                    }
                }

            if (response.status.isSuccess()) {
                NetworkResult.Success(response.body())
            } else {
                val errorBody = response.bodyAsText().ifBlank { null }
                NetworkResult.Failure(
                    NetworkError.Http(
                        statusCode = response.status.value,
                        statusDescription = response.status.description,
                        body = errorBody,
                    ),
                )
            }
        } catch (e: ResponseException) {
            val status = e.response.status
            val body = runCatching { e.response.bodyAsText() }.getOrNull()?.ifBlank { null }
            NetworkResult.Failure(NetworkError.Http(status.value, status.description, body))
        } catch (e: IOException) {
            NetworkResult.Failure(NetworkError.Network(e))
        } catch (e: ContentConvertException) {
            NetworkResult.Failure(NetworkError.Serialization(e))
        } catch (e: NoTransformationFoundException) {
            NetworkResult.Failure(NetworkError.Serialization(e))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            NetworkResult.Failure(NetworkError.Unknown(e))
        }
    }
}

public class CatalogsItemsAvailabilityClient(
    private val httpClient: HttpClient,
) {
    /**
     * Check item availability
     *
     * Parameters:
     * 	 @param catalogId The ID of the catalog
     * 	 @param itemId The ID of the item
     *
     * Returns:
     * 	[NetworkResult.Success] with [kotlin.Unit] if the request was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun getByCatalogIdAndItemId(
        catalogId: String,
        itemId: String,
        apiConfiguration: ApiConfiguration = ApiConfiguration(),
    ): NetworkResult<Unit> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url = basePath + """/catalogs/$catalogId/items/$itemId/availability"""

        return try {
            val response =
                httpClient.`get`(url) {
                    `header`("Accept", "application/json")
                    headers {
                        apiConfiguration.customHeaders.forEach { (name, value) ->
                            remove(name)
                            append(name, value)
                        }
                    }
                }

            if (response.status.isSuccess()) {
                NetworkResult.Success(response.body())
            } else {
                val errorBody = response.bodyAsText().ifBlank { null }
                NetworkResult.Failure(
                    NetworkError.Http(
                        statusCode = response.status.value,
                        statusDescription = response.status.description,
                        body = errorBody,
                    ),
                )
            }
        } catch (e: ResponseException) {
            val status = e.response.status
            val body = runCatching { e.response.bodyAsText() }.getOrNull()?.ifBlank { null }
            NetworkResult.Failure(NetworkError.Http(status.value, status.description, body))
        } catch (e: IOException) {
            NetworkResult.Failure(NetworkError.Network(e))
        } catch (e: ContentConvertException) {
            NetworkResult.Failure(NetworkError.Serialization(e))
        } catch (e: NoTransformationFoundException) {
            NetworkResult.Failure(NetworkError.Serialization(e))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            NetworkResult.Failure(NetworkError.Unknown(e))
        }
    }

    /**
     * Update item availability
     *
     * Parameters:
     * 	 @param catalogId The ID of the catalog
     * 	 @param itemId The ID of the item
     *
     * Returns:
     * 	[NetworkResult.Success] with [kotlin.Unit] if the request was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun putByCatalogIdAndItemId(
        catalogId: String,
        itemId: String,
        apiConfiguration: ApiConfiguration = ApiConfiguration(),
    ): NetworkResult<Unit> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url = basePath + """/catalogs/$catalogId/items/$itemId/availability"""

        return try {
            val response =
                httpClient.put(url) {
                    `header`("Accept", "application/json")
                    headers {
                        apiConfiguration.customHeaders.forEach { (name, value) ->
                            remove(name)
                            append(name, value)
                        }
                    }
                }

            if (response.status.isSuccess()) {
                NetworkResult.Success(response.body())
            } else {
                val errorBody = response.bodyAsText().ifBlank { null }
                NetworkResult.Failure(
                    NetworkError.Http(
                        statusCode = response.status.value,
                        statusDescription = response.status.description,
                        body = errorBody,
                    ),
                )
            }
        } catch (e: ResponseException) {
            val status = e.response.status
            val body = runCatching { e.response.bodyAsText() }.getOrNull()?.ifBlank { null }
            NetworkResult.Failure(NetworkError.Http(status.value, status.description, body))
        } catch (e: IOException) {
            NetworkResult.Failure(NetworkError.Network(e))
        } catch (e: ContentConvertException) {
            NetworkResult.Failure(NetworkError.Serialization(e))
        } catch (e: NoTransformationFoundException) {
            NetworkResult.Failure(NetworkError.Serialization(e))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            NetworkResult.Failure(NetworkError.Unknown(e))
        }
    }
}

public class UptimeClient(
    private val httpClient: HttpClient,
) {
    /**
     * Get the uptime of the system
     *
     *
     * Returns:
     * 	[NetworkResult.Success] with [kotlin.String] if the request was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun getSystemUptime(apiConfiguration: ApiConfiguration = ApiConfiguration()): NetworkResult<String> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url = basePath + """/uptime"""

        return try {
            val response =
                httpClient.`get`(url) {
                    `header`("Accept", "application/json")
                    headers {
                        apiConfiguration.customHeaders.forEach { (name, value) ->
                            remove(name)
                            append(name, value)
                        }
                    }
                }

            if (response.status.isSuccess()) {
                NetworkResult.Success(response.body())
            } else {
                val errorBody = response.bodyAsText().ifBlank { null }
                NetworkResult.Failure(
                    NetworkError.Http(
                        statusCode = response.status.value,
                        statusDescription = response.status.description,
                        body = errorBody,
                    ),
                )
            }
        } catch (e: ResponseException) {
            val status = e.response.status
            val body = runCatching { e.response.bodyAsText() }.getOrNull()?.ifBlank { null }
            NetworkResult.Failure(NetworkError.Http(status.value, status.description, body))
        } catch (e: IOException) {
            NetworkResult.Failure(NetworkError.Network(e))
        } catch (e: ContentConvertException) {
            NetworkResult.Failure(NetworkError.Serialization(e))
        } catch (e: NoTransformationFoundException) {
            NetworkResult.Failure(NetworkError.Serialization(e))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            NetworkResult.Failure(NetworkError.Unknown(e))
        }
    }
}

public class NoContentClient(
    private val httpClient: HttpClient,
) {
    /**
     * Endpoint with no content response
     *
     *
     * Returns:
     * 	[NetworkResult.Success] with [kotlin.Unit] if the request was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun getNoContent(apiConfiguration: ApiConfiguration = ApiConfiguration()): NetworkResult<Unit> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url = basePath + """/no-content"""

        return try {
            val response =
                httpClient.`get`(url) {
                    `header`("Accept", "application/json")
                    headers {
                        apiConfiguration.customHeaders.forEach { (name, value) ->
                            remove(name)
                            append(name, value)
                        }
                    }
                }

            if (response.status.isSuccess()) {
                NetworkResult.Success(response.body())
            } else {
                val errorBody = response.bodyAsText().ifBlank { null }
                NetworkResult.Failure(
                    NetworkError.Http(
                        statusCode = response.status.value,
                        statusDescription = response.status.description,
                        body = errorBody,
                    ),
                )
            }
        } catch (e: ResponseException) {
            val status = e.response.status
            val body = runCatching { e.response.bodyAsText() }.getOrNull()?.ifBlank { null }
            NetworkResult.Failure(NetworkError.Http(status.value, status.description, body))
        } catch (e: IOException) {
            NetworkResult.Failure(NetworkError.Network(e))
        } catch (e: ContentConvertException) {
            NetworkResult.Failure(NetworkError.Serialization(e))
        } catch (e: NoTransformationFoundException) {
            NetworkResult.Failure(NetworkError.Serialization(e))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            NetworkResult.Failure(NetworkError.Unknown(e))
        }
    }
}
