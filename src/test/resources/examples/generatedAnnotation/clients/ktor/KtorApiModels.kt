package com.example.client

import kotlinx.io.IOException
import javax.`annotation`.processing.Generated
import kotlin.Exception
import kotlin.Int
import kotlin.Nothing
import kotlin.String
import kotlin.Throwable

/**
 * Sealed interface representing all possible network errors that can occur during API calls.
 */
@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    comments = "Generated with Fabrikt v27.0.1",
)
public sealed interface NetworkError {
    /**
     * HTTP error response (4xx, 5xx status codes).
     * @property statusCode The HTTP status code
     * @property statusDescription The standard HTTP status description (e.g., "Not Found" for 404)
     * @property body The response body content, if any
     */
    @Generated(
        value = ["io.fabrikt.cli.CodeGen"],
        comments = "Generated with Fabrikt v27.0.1",
    )
    public data class Http(
        public val statusCode: Int,
        public val statusDescription: String,
        public val body: String? = null,
    ) : NetworkError

    /**
     * Network connectivity error (connection timeout, DNS failure, etc.).
     * @property cause The underlying IOException, if available
     */
    @Generated(
        value = ["io.fabrikt.cli.CodeGen"],
        comments = "Generated with Fabrikt v27.0.1",
    )
    public data class Network(
        public val cause: IOException? = null,
    ) : NetworkError

    /**
     * Serialization/deserialization error when parsing the response.
     * @property cause The underlying exception
     */
    @Generated(
        value = ["io.fabrikt.cli.CodeGen"],
        comments = "Generated with Fabrikt v27.0.1",
    )
    public data class Serialization(
        public val cause: Exception,
    ) : NetworkError

    /**
     * Unknown error that doesn't fit other categories.
     * @property cause The underlying exception, if available
     */
    @Generated(
        value = ["io.fabrikt.cli.CodeGen"],
        comments = "Generated with Fabrikt v27.0.1",
    )
    public data class Unknown(
        public val cause: Throwable? = null,
    ) : NetworkError
}

/**
 * Sealed interface representing the result of a network operation.
 * @param T The type of data returned on success
 */
@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    comments = "Generated with Fabrikt v27.0.1",
)
public sealed interface NetworkResult<out T> {
    /**
     * Successful response with data.
     * @property data The deserialized response data
     */
    @Generated(
        value = ["io.fabrikt.cli.CodeGen"],
        comments = "Generated with Fabrikt v27.0.1",
    )
    public data class Success<out T>(
        public val `data`: T,
    ) : NetworkResult<T>

    /**
     * Failure response.
     * @property error The network error that occurred
     */
    @Generated(
        value = ["io.fabrikt.cli.CodeGen"],
        comments = "Generated with Fabrikt v27.0.1",
    )
    public data class Failure(
        public val error: NetworkError,
    ) : NetworkResult<Nothing>
}
