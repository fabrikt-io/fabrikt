package examples.multipleRequestMediaTypes.client

import examples.multipleRequestMediaTypes.models.ArrayDetailsRequestItem
import examples.multipleRequestMediaTypes.models.CountRequest
import examples.multipleRequestMediaTypes.models.DifferentInlineRequestApplicationJson
import examples.multipleRequestMediaTypes.models.DifferentInlineRequestTextJson
import examples.multipleRequestMediaTypes.models.InlineDetailsRequest
import examples.multipleRequestMediaTypes.models.RequestsDetailsRequest
import org.springframework.web.bind.`annotation`.CookieValue
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RequestHeader
import org.springframework.web.bind.`annotation`.RequestParam
import org.springframework.web.service.`annotation`.HttpExchange
import kotlin.Any
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map

@Suppress("unused")
public interface AliasClient {
    /**
     *
     *
     * @param requestsDetailsRequest
     */
    @HttpExchange(
        url = "/alias",
        method = "POST",
        contentType = "application/json",
    )
    public fun postAlias(
        @RequestBody requestsDetailsRequest: RequestsDetailsRequest?,
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
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
    @HttpExchange(
        url = "/distinct",
        method = "POST",
        contentType = "application/json",
    )
    public fun createDetails(
        @RequestBody requestsDetailsRequest: RequestsDetailsRequest,
        @CookieValue("session", required = false) session: String? = null,
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )

    /**
     *
     *
     * @param countRequest
     * @param session
     */
    @HttpExchange(
        url = "/distinct",
        method = "POST",
        contentType = "text/json",
    )
    public fun createDetailsTextJson(
        @RequestBody countRequest: CountRequest,
        @CookieValue("session", required = false) session: String? = null,
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )
}

@Suppress("unused")
public interface InlineClient {
    /**
     *
     *
     * @param body
     */
    @HttpExchange(
        url = "/inline",
        method = "POST",
        contentType = "application/json",
    )
    public fun inlineDetails(
        @RequestBody body: InlineDetailsRequest,
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )
}

@Suppress("unused")
public interface DifferentInlineClient {
    /**
     *
     *
     * @param requestBody
     */
    @HttpExchange(
        url = "/different-inline",
        method = "POST",
        contentType = "application/json",
    )
    public fun differentInline(
        @RequestBody requestBody: DifferentInlineRequestApplicationJson,
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )

    /**
     *
     *
     * @param textJson
     */
    @HttpExchange(
        url = "/different-inline",
        method = "POST",
        contentType = "text/json",
    )
    public fun differentInlineTextJson(
        @RequestBody textJson: DifferentInlineRequestTextJson,
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )
}

@Suppress("unused")
public interface ArraysClient {
    /**
     *
     *
     * @param body
     */
    @HttpExchange(
        url = "/arrays",
        method = "POST",
        contentType = "application/json",
    )
    public fun arrayDetails(
        @RequestBody body: List<ArrayDetailsRequestItem>,
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )
}

@Suppress("unused")
public interface ComponentClient {
    /**
     *
     *
     * @param requestsDetailsRequest
     */
    @HttpExchange(
        url = "/component",
        method = "POST",
        contentType = "application/json",
    )
    public fun componentDetails(
        @RequestBody requestsDetailsRequest: RequestsDetailsRequest,
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )
}
