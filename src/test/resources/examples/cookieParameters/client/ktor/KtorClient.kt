package examples.cookieParameters.client

import examples.cookieParameters.models.CookiePreferences
import examples.cookieParameters.models.DisplayMode
import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.cookie
import io.ktor.client.request.`get`
import io.ktor.client.request.`header`
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.serialization.ContentConvertException
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlin.String
import kotlin.collections.List

public class CookiesClient(
    private val httpClient: HttpClient,
) {
    /**
     * Parameters:
     * 	 @param id
     * 	 @param sessionId
     * 	 @param displayMode
     * 	 @param features
     * 	 @param locale
     * 	 @param scopes
     *
     * Returns:
     * 	[NetworkResult.Success] with [examples.cookieParameters.models.CookiePreferences] if the
     * request was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun getCookiePreferences(
        id: String,
        sessionId: String,
        displayMode: DisplayMode,
        features: List<String>,
        locale: String? = null,
        scopes: List<String>? = null,
        apiConfiguration: ApiConfiguration = ApiConfiguration(),
    ): NetworkResult<CookiePreferences> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url = basePath + """/cookies/$id"""

        return try {
            val response =
                httpClient.`get`(url) {
                    `header`("Accept", "application/json")
                    cookie("sessionId", sessionId.toString())
                    cookie("displayMode", displayMode.value)
                    features.forEach { cookie("features", it.toString()) }
                    locale?.let { cookie("locale", it.toString()) }
                    scopes?.let { cookie("scopes", it.joinToString(",")) }
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
