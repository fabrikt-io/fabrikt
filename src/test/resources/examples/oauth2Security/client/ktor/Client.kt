package examples.oauth2Security.client

import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.`get`
import io.ktor.client.request.`header`
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.serialization.ContentConvertException
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlin.String
import kotlin.Unit
import kotlin.collections.Set

public class ProtectedClient(
    private val httpClient: HttpClient,
) {
    /**
     *
     * Returns:
     * 	[NetworkResult.Success] with [kotlin.Unit] if the request was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun getProtected(apiConfiguration: ApiConfiguration = ApiConfiguration()): NetworkResult<Unit> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url = basePath + """/protected"""

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

    public suspend fun getProtectedWithOAuth2Token(
        oauth2TokenProvider: (String, Set<String>) -> String?,
        apiConfiguration: ApiConfiguration = ApiConfiguration(),
    ): NetworkResult<Unit> {
        val oauth2Token =
            listOf("OAuth2" to setOf("read:pets")).firstNotNullOfOrNull { (scheme, scopes) ->
                oauth2TokenProvider(scheme, scopes)?.takeIf { it.isNotBlank() }
            }
        checkNotNull(oauth2Token) { "An OAuth2 access token is required for this operation" }
        val oauth2Headers =
            oauth2Token?.let { apiConfiguration.copy(customHeaders = apiConfiguration.customHeaders + ("Authorization" to "Bearer $it")) }
                ?: apiConfiguration
        return getProtected(
            apiConfiguration = oauth2Headers,
        )
    }
}

public class AnonymousClient(
    private val httpClient: HttpClient,
) {
    /**
     *
     * Returns:
     * 	[NetworkResult.Success] with [kotlin.Unit] if the request was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun getAnonymous(apiConfiguration: ApiConfiguration = ApiConfiguration()): NetworkResult<Unit> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url = basePath + """/anonymous"""

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

public class OptionalClient(
    private val httpClient: HttpClient,
) {
    /**
     *
     * Returns:
     * 	[NetworkResult.Success] with [kotlin.Unit] if the request was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun getOptional(apiConfiguration: ApiConfiguration = ApiConfiguration()): NetworkResult<Unit> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url = basePath + """/optional"""

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

    public suspend fun getOptionalWithOAuth2Token(
        oauth2TokenProvider: (String, Set<String>) -> String?,
        apiConfiguration: ApiConfiguration = ApiConfiguration(),
    ): NetworkResult<Unit> {
        val oauth2Token =
            listOf("OAuth2" to setOf("read:pets")).firstNotNullOfOrNull { (scheme, scopes) ->
                oauth2TokenProvider(scheme, scopes)?.takeIf { it.isNotBlank() }
            }
        val oauth2Headers =
            oauth2Token?.let { apiConfiguration.copy(customHeaders = apiConfiguration.customHeaders + ("Authorization" to "Bearer $it")) }
                ?: apiConfiguration
        return getOptional(
            apiConfiguration = oauth2Headers,
        )
    }
}

public class CombinedClient(
    private val httpClient: HttpClient,
) {
    /**
     *
     * Returns:
     * 	[NetworkResult.Success] with [kotlin.Unit] if the request was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun getCombined(apiConfiguration: ApiConfiguration = ApiConfiguration()): NetworkResult<Unit> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url = basePath + """/combined"""

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

public class AlternativeClient(
    private val httpClient: HttpClient,
) {
    /**
     *
     * Returns:
     * 	[NetworkResult.Success] with [kotlin.Unit] if the request was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun getAlternative(apiConfiguration: ApiConfiguration = ApiConfiguration()): NetworkResult<Unit> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url = basePath + """/alternative"""

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

    public suspend fun getAlternativeWithOAuth2Token(
        oauth2TokenProvider: (String, Set<String>) -> String?,
        apiConfiguration: ApiConfiguration = ApiConfiguration(),
    ): NetworkResult<Unit> {
        val oauth2Token =
            listOf("OAuth2" to setOf("read:pets")).firstNotNullOfOrNull { (scheme, scopes) ->
                oauth2TokenProvider(scheme, scopes)?.takeIf { it.isNotBlank() }
            }
        val oauth2Headers =
            oauth2Token?.let { apiConfiguration.copy(customHeaders = apiConfiguration.customHeaders + ("Authorization" to "Bearer $it")) }
                ?: apiConfiguration
        return getAlternative(
            apiConfiguration = oauth2Headers,
        )
    }
}

public class ScopeAlternativesClient(
    private val httpClient: HttpClient,
) {
    /**
     *
     * Returns:
     * 	[NetworkResult.Success] with [kotlin.Unit] if the request was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun getScopeAlternatives(apiConfiguration: ApiConfiguration = ApiConfiguration()): NetworkResult<Unit> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url = basePath + """/scope-alternatives"""

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

    public suspend fun getScopeAlternativesWithOAuth2Token(
        oauth2TokenProvider: (String, Set<String>) -> String?,
        apiConfiguration: ApiConfiguration = ApiConfiguration(),
    ): NetworkResult<Unit> {
        val oauth2Token =
            listOf("OAuth2" to setOf("read:pets"), "OAuth2" to setOf("write:pets")).firstNotNullOfOrNull { (scheme, scopes) ->
                oauth2TokenProvider(scheme, scopes)?.takeIf { it.isNotBlank() }
            }
        checkNotNull(oauth2Token) { "An OAuth2 access token is required for this operation" }
        val oauth2Headers =
            oauth2Token?.let { apiConfiguration.copy(customHeaders = apiConfiguration.customHeaders + ("Authorization" to "Bearer $it")) }
                ?: apiConfiguration
        return getScopeAlternatives(
            apiConfiguration = oauth2Headers,
        )
    }
}

public class UnscopedClient(
    private val httpClient: HttpClient,
) {
    /**
     *
     * Returns:
     * 	[NetworkResult.Success] with [kotlin.Unit] if the request was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun getUnscoped(apiConfiguration: ApiConfiguration = ApiConfiguration()): NetworkResult<Unit> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url = basePath + """/unscoped"""

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

    public suspend fun getUnscopedWithOAuth2Token(
        oauth2TokenProvider: (String, Set<String>) -> String?,
        apiConfiguration: ApiConfiguration = ApiConfiguration(),
    ): NetworkResult<Unit> {
        val oauth2Token =
            listOf("OAuth2" to setOf<String>()).firstNotNullOfOrNull { (scheme, scopes) ->
                oauth2TokenProvider(scheme, scopes)?.takeIf { it.isNotBlank() }
            }
        checkNotNull(oauth2Token) { "An OAuth2 access token is required for this operation" }
        val oauth2Headers =
            oauth2Token?.let { apiConfiguration.copy(customHeaders = apiConfiguration.customHeaders + ("Authorization" to "Bearer $it")) }
                ?: apiConfiguration
        return getUnscoped(
            apiConfiguration = oauth2Headers,
        )
    }
}

public class SchemesClient(
    private val httpClient: HttpClient,
) {
    /**
     *
     * Returns:
     * 	[NetworkResult.Success] with [kotlin.Unit] if the request was successful.
     * 	[NetworkResult.Failure] with a [NetworkError] if the request failed.
     */
    public suspend fun getSchemes(apiConfiguration: ApiConfiguration = ApiConfiguration()): NetworkResult<Unit> {
        val basePath = apiConfiguration.basePath.trimEnd('/')
        val url = basePath + """/schemes"""

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

    public suspend fun getSchemesWithOAuth2Token(
        oauth2TokenProvider: (String, Set<String>) -> String?,
        apiConfiguration: ApiConfiguration = ApiConfiguration(),
    ): NetworkResult<Unit> {
        val oauth2Token =
            listOf("OAuth2" to setOf("read:pets"), "OtherOAuth2" to setOf("write:pets")).firstNotNullOfOrNull { (scheme, scopes) ->
                oauth2TokenProvider(scheme, scopes)?.takeIf { it.isNotBlank() }
            }
        checkNotNull(oauth2Token) { "An OAuth2 access token is required for this operation" }
        val oauth2Headers =
            oauth2Token?.let { apiConfiguration.copy(customHeaders = apiConfiguration.customHeaders + ("Authorization" to "Bearer $it")) }
                ?: apiConfiguration
        return getSchemes(
            apiConfiguration = oauth2Headers,
        )
    }
}
