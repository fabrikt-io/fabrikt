package com.example.client

import okhttp3.Headers
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.RuntimeException
import kotlin.String

/**
 * API 2xx success response returned by API call.
 *
 * @param <T> The type of data that is deserialized from response body
 */
@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    date = "2001-07-04T19:08:56.235Z",
    comments = "Generated with Fabrikt v27.0.1",
)
public data class ApiResponse<T>(
    public val statusCode: Int,
    public val headers: Headers,
    public val `data`: T? = null,
)

/**
 * API non-2xx failure responses returned by API call.
 */
@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    date = "2001-07-04T19:08:56.235Z",
    comments = "Generated with Fabrikt v27.0.1",
)
public open class ApiException(
    override val message: String,
) : RuntimeException(message)

/**
 * API 3xx redirect response returned by API call.
 */
@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    date = "2001-07-04T19:08:56.235Z",
    comments = "Generated with Fabrikt v27.0.1",
)
public open class ApiRedirectException(
    public val statusCode: Int,
    public val headers: Headers,
    override val message: String,
) : ApiException(message)

/**
 * API 4xx failure responses returned by API call.
 */
@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    date = "2001-07-04T19:08:56.235Z",
    comments = "Generated with Fabrikt v27.0.1",
)
public data class ApiClientException(
    public val statusCode: Int,
    public val headers: Headers,
    override val message: String,
) : ApiException(message)

/**
 * API 5xx failure responses returned by API call.
 */
@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    date = "2001-07-04T19:08:56.235Z",
    comments = "Generated with Fabrikt v27.0.1",
)
public data class ApiServerException(
    public val statusCode: Int,
    public val headers: Headers,
    override val message: String,
) : ApiException(message)
