package examples.directionalEndpoints.client

import examples.directionalEndpoints.models.PetRequest
import examples.directionalEndpoints.models.PetResponse
import examples.directionalEndpoints.models.PetStatusRequest
import examples.directionalEndpoints.models.StateRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.`header`
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.serialization.ContentConvertException
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException

public class PetsClient(
    private val httpClient: HttpClient,
) {
    /**
     * Parameters:
     * 	 @param pet
     * 	 @param status
     * 	 @param state
     *
     * Returns:
     * 	[NetworkResult.Success] with [examples.directionalEndpoints.models.PetResponse] if the request
     * was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun createPet(
        pet: PetRequest,
        status: PetStatusRequest? = null,
        state: StateRequest? = null,
        apiConfiguration: ApiConfiguration = ApiConfiguration(),
    ): NetworkResult<PetResponse> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url =
            buildString {
                append(basePath)
                append("""/pets""")
                val params =
                    buildList {
                        status?.let { add("status=$it") }
                        state?.let { add("state=$it") }
                    }
                if (params.isNotEmpty()) append("?").append(params.joinToString("&"))
            }

        return try {
            val response =
                httpClient.post(url) {
                    `header`("Accept", "application/json")
                    `header`("Content-Type", "application/json")
                    setBody(pet)
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
