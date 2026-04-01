package examples.parameterNameClash.client

import examples.parameterNameClash.models.SomeObject
import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.`get`
import io.ktor.client.request.`header`
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.serialization.ContentConvertException
import kotlinx.coroutines.CancellationException
import java.io.IOException
import kotlin.String
import kotlin.Unit

public class ExampleClient(
    private val httpClient: HttpClient,
) {
    /**
     * Parameters:
     * 	 @param pathB
     * 	 @param queryB
     *
     * Returns:
     * 	[NetworkResult.Success] with [kotlin.Unit] if the request was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun getByPathB(
        pathB: String,
        queryB: String,
        apiConfiguration: ApiConfiguration = ApiConfiguration(),
    ): NetworkResult<Unit> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url =
            buildString {
                append(basePath)
                append("""/example/$pathB""")
                val params =
                    buildList {
                        add("b=$queryB")
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

    /**
     * Parameters:
     * 	 @param bodySomeObject example
     * 	 @param querySomeObject
     *
     * Returns:
     * 	[NetworkResult.Success] with [kotlin.Unit] if the request was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun post(
        bodySomeObject: SomeObject,
        querySomeObject: String,
        apiConfiguration: ApiConfiguration = ApiConfiguration(),
    ): NetworkResult<Unit> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url =
            buildString {
                append(basePath)
                append("""/example""")
                val params =
                    buildList {
                        add("someObject=$querySomeObject")
                    }
                if (params.isNotEmpty()) append("?").append(params.joinToString("&"))
            }

        return try {
            val response =
                httpClient.post(url) {
                    `header`("Accept", "application/json")
                    `header`("Content-Type", "application/json")
                    setBody(bodySomeObject)
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
