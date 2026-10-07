package examples.multipleRequestMediaTypes.client

import examples.multipleRequestMediaTypes.models.ArrayDetailsRequestItem
import examples.multipleRequestMediaTypes.models.CountRequest
import examples.multipleRequestMediaTypes.models.DifferentInlineRequestApplicationJson
import examples.multipleRequestMediaTypes.models.DifferentInlineRequestTextJson
import examples.multipleRequestMediaTypes.models.InlineDetailsRequest
import examples.multipleRequestMediaTypes.models.RequestsDetailsRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.cookie
import io.ktor.client.request.`header`
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.serialization.ContentConvertException
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlin.String
import kotlin.Unit
import kotlin.collections.List

public class AliasClient(
    private val httpClient: HttpClient,
) {
    /**
     * Parameters:
     * 	 @param requestsDetailsRequest
     *
     * Returns:
     * 	[NetworkResult.Success] with [kotlin.Unit] if the request was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun post(
        requestsDetailsRequest: RequestsDetailsRequest,
        apiConfiguration: ApiConfiguration = ApiConfiguration(),
    ): NetworkResult<Unit> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url = basePath + """/alias"""

        return try {
            val response =
                httpClient.post(url) {
                    `header`("Accept", "application/json")
                    `header`("Content-Type", "application/json")
                    setBody(requestsDetailsRequest)
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

public class DistinctClient(
    private val httpClient: HttpClient,
) {
    /**
     * Parameters:
     * 	 @param requestsDetailsRequest
     * 	 @param session
     *
     * Returns:
     * 	[NetworkResult.Success] with [kotlin.Unit] if the request was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun createDetails(
        requestsDetailsRequest: RequestsDetailsRequest,
        session: String? = null,
        apiConfiguration: ApiConfiguration = ApiConfiguration(),
    ): NetworkResult<Unit> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url = basePath + """/distinct"""

        return try {
            val response =
                httpClient.post(url) {
                    `header`("Accept", "application/json")
                    `header`("Content-Type", "application/json")
                    setBody(requestsDetailsRequest)
                    session?.let { cookie("session", it.toString()) }
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
     * Parameters:
     * 	 @param countRequest
     * 	 @param session
     *
     * Returns:
     * 	[NetworkResult.Success] with [kotlin.Unit] if the request was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun createDetailsTextJson(
        countRequest: CountRequest,
        session: String? = null,
        apiConfiguration: ApiConfiguration = ApiConfiguration(),
    ): NetworkResult<Unit> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url = basePath + """/distinct"""

        return try {
            val response =
                httpClient.post(url) {
                    `header`("Accept", "application/json")
                    `header`("Content-Type", "text/json")
                    setBody(countRequest)
                    session?.let { cookie("session", it.toString()) }
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

public class InlineClient(
    private val httpClient: HttpClient,
) {
    /**
     * Parameters:
     * 	 @param body
     *
     * Returns:
     * 	[NetworkResult.Success] with [kotlin.Unit] if the request was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun inlineDetails(
        body: InlineDetailsRequest,
        apiConfiguration: ApiConfiguration =
            ApiConfiguration(),
    ): NetworkResult<Unit> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url = basePath + """/inline"""

        return try {
            val response =
                httpClient.post(url) {
                    `header`("Accept", "application/json")
                    `header`("Content-Type", "application/json")
                    setBody(body)
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

public class DifferentInlineClient(
    private val httpClient: HttpClient,
) {
    /**
     * Parameters:
     * 	 @param requestBody
     *
     * Returns:
     * 	[NetworkResult.Success] with [kotlin.Unit] if the request was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun differentInline(
        requestBody: DifferentInlineRequestApplicationJson,
        apiConfiguration: ApiConfiguration = ApiConfiguration(),
    ): NetworkResult<Unit> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url = basePath + """/different-inline"""

        return try {
            val response =
                httpClient.post(url) {
                    `header`("Accept", "application/json")
                    `header`("Content-Type", "application/json")
                    setBody(requestBody)
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
     * Parameters:
     * 	 @param textJson
     *
     * Returns:
     * 	[NetworkResult.Success] with [kotlin.Unit] if the request was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun differentInlineTextJson(
        textJson: DifferentInlineRequestTextJson,
        apiConfiguration: ApiConfiguration = ApiConfiguration(),
    ): NetworkResult<Unit> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url = basePath + """/different-inline"""

        return try {
            val response =
                httpClient.post(url) {
                    `header`("Accept", "application/json")
                    `header`("Content-Type", "text/json")
                    setBody(textJson)
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

public class ArraysClient(
    private val httpClient: HttpClient,
) {
    /**
     * Parameters:
     * 	 @param body
     *
     * Returns:
     * 	[NetworkResult.Success] with [kotlin.Unit] if the request was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun arrayDetails(
        body: List<ArrayDetailsRequestItem>,
        apiConfiguration: ApiConfiguration = ApiConfiguration(),
    ): NetworkResult<Unit> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url = basePath + """/arrays"""

        return try {
            val response =
                httpClient.post(url) {
                    `header`("Accept", "application/json")
                    `header`("Content-Type", "application/json")
                    setBody(body)
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

public class ComponentClient(
    private val httpClient: HttpClient,
) {
    /**
     * Parameters:
     * 	 @param requestsDetailsRequest
     *
     * Returns:
     * 	[NetworkResult.Success] with [kotlin.Unit] if the request was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun componentDetails(
        requestsDetailsRequest: RequestsDetailsRequest,
        apiConfiguration: ApiConfiguration = ApiConfiguration(),
    ): NetworkResult<Unit> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url = basePath + """/component"""

        return try {
            val response =
                httpClient.post(url) {
                    `header`("Accept", "application/json")
                    `header`("Content-Type", "application/json")
                    setBody(requestsDetailsRequest)
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
