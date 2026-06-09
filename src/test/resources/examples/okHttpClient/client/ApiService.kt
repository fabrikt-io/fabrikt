package examples.okHttpClient.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import examples.okHttpClient.models.Content
import examples.okHttpClient.models.EnumQueryParam
import examples.okHttpClient.models.FirstModel
import examples.okHttpClient.models.QueryResult
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import okhttp3.OkHttpClient
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
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
public class ExamplePath1Service(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    objectMapper: ObjectMapper,
    baseUrl: String,
    okHttpClient: OkHttpClient,
) {
    public var circuitBreakerName: String = "examplePath1Client"

    private val apiClient: ExamplePath1Client =
        ExamplePath1Client(
            objectMapper,
            baseUrl,
            okHttpClient,
        )

    @Throws(ApiException::class)
    public fun getExamplePath1(
        explodeListQueryParam: List<String>? = null,
        queryParam2: Int? = null,
        intListQueryParam: List<Int>? = null,
        xJsonEncodedHeader: String? = null,
        enumQueryParam: EnumQueryParam? = null,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<QueryResult> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.getExamplePath1(
                explodeListQueryParam,
                queryParam2,
                intListQueryParam,
                xJsonEncodedHeader,
                enumQueryParam,
                additionalHeaders,
            )
        }

    @Throws(ApiException::class)
    public fun postExamplePath1(
        content: Content,
        explodeListQueryParam: List<String>? = null,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.postExamplePath1(content, explodeListQueryParam, additionalHeaders)
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
public class ExamplePath2Service(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    objectMapper: ObjectMapper,
    baseUrl: String,
    okHttpClient: OkHttpClient,
) {
    public var circuitBreakerName: String = "examplePath2Client"

    private val apiClient: ExamplePath2Client =
        ExamplePath2Client(
            objectMapper,
            baseUrl,
            okHttpClient,
        )

    @Throws(ApiException::class)
    public fun getExamplePath2PathParam(
        pathParam: String,
        limit: Int = 500,
        queryParam2: Int? = null,
        ifNoneMatch: String? = null,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<Content> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.getExamplePath2PathParam(pathParam, limit, queryParam2, ifNoneMatch, additionalHeaders)
        }

    @Throws(ApiException::class)
    public fun headOperationIdExample(
        pathParam: String,
        queryParam3: Boolean? = null,
        ifNoneMatch: String? = null,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.headOperationIdExample(pathParam, queryParam3, ifNoneMatch, additionalHeaders)
        }

    @Throws(ApiException::class)
    public fun putExamplePath2PathParam(
        firstModel: FirstModel,
        pathParam: String,
        ifMatch: String,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.putExamplePath2PathParam(firstModel, pathParam, ifMatch, additionalHeaders)
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
public class ExamplePath3SubresourceService(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    objectMapper: ObjectMapper,
    baseUrl: String,
    okHttpClient: OkHttpClient,
) {
    public var circuitBreakerName: String = "examplePath3SubresourceClient"

    private val apiClient: ExamplePath3SubresourceClient =
        ExamplePath3SubresourceClient(
            objectMapper,
            baseUrl,
            okHttpClient,
        )

    @Throws(ApiException::class)
    public fun putExamplePath3PathParamSubresource(
        firstModel: FirstModel,
        pathParam: String,
        ifMatch: String,
        csvListQueryParam: List<String>? = null,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.putExamplePath3PathParamSubresource(firstModel, pathParam, ifMatch, csvListQueryParam, additionalHeaders)
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
public class ExamplePath4OnlyFailureResponseService(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    objectMapper: ObjectMapper,
    baseUrl: String,
    okHttpClient: OkHttpClient,
) {
    public var circuitBreakerName: String = "examplePath4OnlyFailureResponseClient"

    private val apiClient: ExamplePath4OnlyFailureResponseClient =
        ExamplePath4OnlyFailureResponseClient(objectMapper, baseUrl, okHttpClient)

    @Throws(ApiException::class)
    public fun postExamplePath4OnlyFailureResponse(
        additionalHeaders: Map<String, String> =
            emptyMap(),
    ): ApiResponse<Unit> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.postExamplePath4OnlyFailureResponse(additionalHeaders)
        }
}
