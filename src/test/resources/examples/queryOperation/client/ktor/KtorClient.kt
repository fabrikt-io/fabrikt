package examples.queryOperation.client

import examples.queryOperation.models.SearchRequest
import examples.queryOperation.models.SearchResult
import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.`get`
import io.ktor.client.request.`header`
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import io.ktor.serialization.ContentConvertException
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException

public class SearchClient(
    private val httpClient: HttpClient,
) {
    /**
     *
     * Returns:
     * 	[NetworkResult.Success] with [examples.queryOperation.models.SearchResult] if the request was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun getSearch(apiConfiguration: ApiConfiguration = ApiConfiguration()): NetworkResult<SearchResult> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url = basePath + """/search"""

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
     * Parameters:
     * 	 @param searchRequest
     *
     * Returns:
     * 	[NetworkResult.Success] with [examples.queryOperation.models.SearchResult] if the request was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun search(
        searchRequest: SearchRequest,
        apiConfiguration: ApiConfiguration = ApiConfiguration(),
    ): NetworkResult<SearchResult> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url = basePath + """/search"""

        return try {
            val response =
                httpClient.request(url) {
                    method = HttpMethod("QUERY")
                    `header`("Accept", "application/json")
                    `header`("Content-Type", "application/json")
                    setBody(searchRequest)
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
