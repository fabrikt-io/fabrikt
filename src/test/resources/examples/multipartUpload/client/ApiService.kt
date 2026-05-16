package examples.multipartUpload.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import examples.multipartUpload.models.FileMetadata
import examples.multipartUpload.models.SimpleUploadResult
import examples.multipartUpload.models.UploadResult
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import okhttp3.OkHttpClient
import kotlin.ByteArray
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.jvm.Throws

/**
 * The circuit breaker registry should have the proper configuration to correctly action on circuit
 * breaker transitions based on the client exceptions [ApiClientException], [ApiServerException] and
 * [IOException].
 *
 * @see ApiClientException
 * @see ApiServerException
 */
@Suppress("unused")
public class ApiUploadService(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    objectMapper: ObjectMapper,
    baseUrl: String,
    okHttpClient: OkHttpClient,
) {
    public var circuitBreakerName: String = "apiUploadClient"

    private val apiClient: ApiUploadClient = ApiUploadClient(objectMapper, baseUrl, okHttpClient)

    @Throws(ApiException::class)
    public fun uploadFile(
        `file`: ByteArray?,
        metadata: FileMetadata?,
        tags: List<String>?,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<UploadResult> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.uploadFile(file, metadata, tags, additionalHeaders)
        }
}

/**
 * The circuit breaker registry should have the proper configuration to correctly action on circuit
 * breaker transitions based on the client exceptions [ApiClientException], [ApiServerException] and
 * [IOException].
 *
 * @see ApiClientException
 * @see ApiServerException
 */
@Suppress("unused")
public class ApiUploadSimpleService(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    objectMapper: ObjectMapper,
    baseUrl: String,
    okHttpClient: OkHttpClient,
) {
    public var circuitBreakerName: String = "apiUploadSimpleClient"

    private val apiClient: ApiUploadSimpleClient =
        ApiUploadSimpleClient(
            objectMapper,
            baseUrl,
            okHttpClient,
        )

    @Throws(ApiException::class)
    public fun uploadSingleFile(
        `file`: ByteArray?,
        additionalHeaders: Map<String, String> =
            emptyMap(),
    ): ApiResponse<SimpleUploadResult> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.uploadSingleFile(file, additionalHeaders)
        }
}

/**
 * The circuit breaker registry should have the proper configuration to correctly action on circuit
 * breaker transitions based on the client exceptions [ApiClientException], [ApiServerException] and
 * [IOException].
 *
 * @see ApiClientException
 * @see ApiServerException
 */
@Suppress("unused")
public class ApiUploadMultipleService(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    objectMapper: ObjectMapper,
    baseUrl: String,
    okHttpClient: OkHttpClient,
) {
    public var circuitBreakerName: String = "apiUploadMultipleClient"

    private val apiClient: ApiUploadMultipleClient =
        ApiUploadMultipleClient(
            objectMapper,
            baseUrl,
            okHttpClient,
        )

    @Throws(ApiException::class)
    public fun uploadMultipleFiles(
        files: List<ByteArray>?,
        commonMetadata: FileMetadata?,
        description: String?,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<List<UploadResult>> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.uploadMultipleFiles(files, commonMetadata, description, additionalHeaders)
        }
}
