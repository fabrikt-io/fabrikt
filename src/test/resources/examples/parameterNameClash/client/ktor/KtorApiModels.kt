package examples.parameterNameClash.client

import java.io.IOException

/**
 * Sealed interface representing all possible network errors that can occur during API calls.
 */
sealed interface NetworkError {
    /**
     * HTTP error response (4xx, 5xx status codes).
     * @property statusCode The HTTP status code
     * @property statusDescription The standard HTTP status description (e.g., "Not Found" for 404)
     * @property body The response body content, if any
     */
    data class Http(
        val statusCode: Int,
        val statusDescription: String,
        val body: String? = null,
    ) : NetworkError

    /**
     * Network connectivity error (connection timeout, DNS failure, etc.).
     * @property cause The underlying IOException, if available
     */
    data class Network(val cause: IOException? = null) : NetworkError

    /**
     * Serialization/deserialization error when parsing the response.
     * @property cause The underlying exception
     */
    data class Serialization(val cause: Exception) : NetworkError

    /**
     * Unknown error that doesn't fit other categories.
     * @property cause The underlying exception, if available
     */
    data class Unknown(val cause: Throwable? = null) : NetworkError
}

/**
 * Sealed interface representing the result of a network operation.
 * @param T The type of data returned on success
 */
sealed interface NetworkResult<out T> {
    /**
     * Successful response with data.
     * @property data The deserialized response data
     */
    data class Success<out T>(val data: T) : NetworkResult<T>

    /**
     * Failure response.
     * @property error The network error that occurred
     */
    data class Failure(val error: NetworkError) : NetworkResult<Nothing>
}
