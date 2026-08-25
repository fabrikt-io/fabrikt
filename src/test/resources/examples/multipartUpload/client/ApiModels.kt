package examples.multipartUpload.client

import kotlin.Int
import kotlin.RuntimeException
import kotlin.String
import okhttp3.Headers

/**
 * API 2xx success response returned by API call.
 *
 * @param <T> The type of data that is deserialized from response body
 */
public data class ApiResponse<T>(
  public val statusCode: Int,
  public val headers: Headers,
  public val `data`: T? = null,
)

/**
 * API non-2xx failure responses returned by API call.
 */
public open class ApiException(
  override val message: String,
) : RuntimeException(message)

/**
 * API 3xx redirect response returned by API call.
 */
public open class ApiRedirectException(
  public val statusCode: Int,
  public val headers: Headers,
  override val message: String,
) : ApiException(message)

/**
 * API 4xx failure responses returned by API call.
 */
public data class ApiClientException(
  public val statusCode: Int,
  public val headers: Headers,
  override val message: String,
) : ApiException(message)

/**
 * API 5xx failure responses returned by API call.
 */
public data class ApiServerException(
  public val statusCode: Int,
  public val headers: Headers,
  override val message: String,
) : ApiException(message)
