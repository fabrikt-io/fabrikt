package examples.multipleRequestMediaTypes.client

import examples.multipleRequestMediaTypes.models.ArrayDetailsRequestItem
import examples.multipleRequestMediaTypes.models.CountRequest
import examples.multipleRequestMediaTypes.models.DifferentInlineRequestApplicationJson
import examples.multipleRequestMediaTypes.models.DifferentInlineRequestTextJson
import examples.multipleRequestMediaTypes.models.InlineDetailsRequest
import examples.multipleRequestMediaTypes.models.RequestsDetailsRequest
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import okhttp3.OkHttpClient
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.jacksonTypeRef
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
public class AliasService(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    objectMapper: JsonMapper,
    baseUrl: String,
    okHttpClient: OkHttpClient,
) {
    public var circuitBreakerName: String = "aliasClient"

    private val apiClient: AliasClient = AliasClient(objectMapper, baseUrl, okHttpClient)

    @Throws(ApiException::class)
    public fun postAlias(
        requestsDetailsRequest: RequestsDetailsRequest?,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.postAlias(requestsDetailsRequest, additionalHeaders)
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
public class DistinctService(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    objectMapper: JsonMapper,
    baseUrl: String,
    okHttpClient: OkHttpClient,
) {
    public var circuitBreakerName: String = "distinctClient"

    private val apiClient: DistinctClient = DistinctClient(objectMapper, baseUrl, okHttpClient)

    @Throws(ApiException::class)
    public fun createDetails(
        requestsDetailsRequest: RequestsDetailsRequest,
        session: String? = null,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.createDetails(requestsDetailsRequest, session, additionalHeaders)
        }

    @Throws(ApiException::class)
    public fun createDetailsTextJson(
        countRequest: CountRequest,
        session: String? = null,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.createDetailsTextJson(countRequest, session, additionalHeaders)
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
public class InlineService(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    objectMapper: JsonMapper,
    baseUrl: String,
    okHttpClient: OkHttpClient,
) {
    public var circuitBreakerName: String = "inlineClient"

    private val apiClient: InlineClient = InlineClient(objectMapper, baseUrl, okHttpClient)

    @Throws(ApiException::class)
    public fun inlineDetails(
        body: InlineDetailsRequest,
        additionalHeaders: Map<String, String> =
            emptyMap(),
    ): ApiResponse<Unit> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.inlineDetails(body, additionalHeaders)
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
public class DifferentInlineService(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    objectMapper: JsonMapper,
    baseUrl: String,
    okHttpClient: OkHttpClient,
) {
    public var circuitBreakerName: String = "differentInlineClient"

    private val apiClient: DifferentInlineClient =
        DifferentInlineClient(
            objectMapper,
            baseUrl,
            okHttpClient,
        )

    @Throws(ApiException::class)
    public fun differentInline(
        requestBody: DifferentInlineRequestApplicationJson,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.differentInline(requestBody, additionalHeaders)
        }

    @Throws(ApiException::class)
    public fun differentInlineTextJson(
        textJson: DifferentInlineRequestTextJson,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.differentInlineTextJson(textJson, additionalHeaders)
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
public class ArraysService(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    objectMapper: JsonMapper,
    baseUrl: String,
    okHttpClient: OkHttpClient,
) {
    public var circuitBreakerName: String = "arraysClient"

    private val apiClient: ArraysClient = ArraysClient(objectMapper, baseUrl, okHttpClient)

    @Throws(ApiException::class)
    public fun arrayDetails(
        body: List<ArrayDetailsRequestItem>,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.arrayDetails(body, additionalHeaders)
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
public class ComponentService(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    objectMapper: JsonMapper,
    baseUrl: String,
    okHttpClient: OkHttpClient,
) {
    public var circuitBreakerName: String = "componentClient"

    private val apiClient: ComponentClient = ComponentClient(objectMapper, baseUrl, okHttpClient)

    @Throws(ApiException::class)
    public fun componentDetails(
        requestsDetailsRequest: RequestsDetailsRequest,
        additionalHeaders: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> =
        withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {
            apiClient.componentDetails(requestsDetailsRequest, additionalHeaders)
        }
}
