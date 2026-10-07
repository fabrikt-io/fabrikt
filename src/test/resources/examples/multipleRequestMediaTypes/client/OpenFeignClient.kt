package examples.multipleRequestMediaTypes.client

import examples.multipleRequestMediaTypes.models.ArrayDetailsRequestItem
import examples.multipleRequestMediaTypes.models.CountRequest
import examples.multipleRequestMediaTypes.models.DifferentInlineRequestApplicationJson
import examples.multipleRequestMediaTypes.models.DifferentInlineRequestTextJson
import examples.multipleRequestMediaTypes.models.InlineDetailsRequest
import examples.multipleRequestMediaTypes.models.RequestsDetailsRequest
import feign.HeaderMap
import feign.Headers
import feign.Param
import feign.QueryMap
import feign.RequestLine
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.jvm.JvmSynthetic

@Suppress("unused")
public interface AliasClient {
    /**
     *
     *
     * @param requestsDetailsRequest
     */
    @RequestLine("POST /alias")
    public fun postAlias(
        requestsDetailsRequest: RequestsDetailsRequest?,
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    )
}

@Suppress("unused")
public interface DistinctClient {
    /**
     *
     *
     * @param requestsDetailsRequest
     * @param session
     */
    public fun createDetails(
        requestsDetailsRequest: RequestsDetailsRequest,
        session: String? = null,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): Unit =
        createDetailsWithCookieHeader(
            requestsDetailsRequest = requestsDetailsRequest,
            cookieHeader =
                buildList {
                    session?.let { add("session=" + it) }
                }.joinToString("; "),
            additionalHeaders = additionalHeaders,
            additionalQueryParameters = additionalQueryParameters,
        )

    @RequestLine("POST /distinct")
    @Headers(
        "Cookie: {cookieHeader}",
        "Content-Type: application/json",
    )
    @JvmSynthetic
    public fun createDetailsWithCookieHeader(
        requestsDetailsRequest: RequestsDetailsRequest,
        @Param("cookieHeader") cookieHeader: String,
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    )

    /**
     *
     *
     * @param countRequest
     * @param session
     */
    public fun createDetailsTextJson(
        countRequest: CountRequest,
        session: String? = null,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): Unit =
        createDetailsTextJsonWithCookieHeader(
            countRequest = countRequest,
            cookieHeader =
                buildList {
                    session?.let { add("session=" + it) }
                }.joinToString("; "),
            additionalHeaders = additionalHeaders,
            additionalQueryParameters = additionalQueryParameters,
        )

    @RequestLine("POST /distinct")
    @Headers(
        "Cookie: {cookieHeader}",
        "Content-Type: text/json",
    )
    @JvmSynthetic
    public fun createDetailsTextJsonWithCookieHeader(
        countRequest: CountRequest,
        @Param("cookieHeader") cookieHeader: String,
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    )
}

@Suppress("unused")
public interface InlineClient {
    /**
     *
     *
     * @param body
     */
    @RequestLine("POST /inline")
    public fun inlineDetails(
        body: InlineDetailsRequest,
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    )
}

@Suppress("unused")
public interface DifferentInlineClient {
    /**
     *
     *
     * @param requestBody
     */
    @RequestLine("POST /different-inline")
    @Headers("Content-Type: application/json")
    public fun differentInline(
        requestBody: DifferentInlineRequestApplicationJson,
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    )

    /**
     *
     *
     * @param textJson
     */
    @RequestLine("POST /different-inline")
    @Headers("Content-Type: text/json")
    public fun differentInlineTextJson(
        textJson: DifferentInlineRequestTextJson,
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    )
}

@Suppress("unused")
public interface ArraysClient {
    /**
     *
     *
     * @param body
     */
    @RequestLine("POST /arrays")
    public fun arrayDetails(
        body: List<ArrayDetailsRequestItem>,
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    )
}

@Suppress("unused")
public interface ComponentClient {
    /**
     *
     *
     * @param requestsDetailsRequest
     */
    @RequestLine("POST /component")
    public fun componentDetails(
        requestsDetailsRequest: RequestsDetailsRequest,
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    )
}
