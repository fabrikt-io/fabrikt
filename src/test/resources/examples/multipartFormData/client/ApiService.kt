package examples.multipartFormData.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import examples.multipartFormData.models.UploadResponse
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import okhttp3.OkHttpClient
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
public class FilesUploadService(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    objectMapper: ObjectMapper,
    baseUrl: String,
    okHttpClient: OkHttpClient,
) {
    public var circuitBreakerName: String = "filesUploadClient"

    private val apiClient: FilesUploadClient = FilesUploadClient(objectMapper, baseUrl, okHttpClient)

    @Throws(ApiException::class)
    public fun uploadFile(
        `file`: FileUpload,
        description: String? = null,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<UploadResponse> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.uploadFile(file, description, additionalHeaders)
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
public class FilesUploadMultipleService(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    objectMapper: ObjectMapper,
    baseUrl: String,
    okHttpClient: OkHttpClient,
) {
    public var circuitBreakerName: String = "filesUploadMultipleClient"

    private val apiClient: FilesUploadMultipleClient =
        FilesUploadMultipleClient(
            objectMapper,
            baseUrl,
            okHttpClient,
        )

    @Throws(ApiException::class)
    public fun uploadMultipleFiles(
        files: List<FileUpload>,
        category: String,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<UploadResponse> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.uploadMultipleFiles(files, category, additionalHeaders)
        }
}
