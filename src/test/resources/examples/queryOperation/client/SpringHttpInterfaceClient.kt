package examples.queryOperation.client

import examples.queryOperation.models.SearchRequest
import examples.queryOperation.models.SearchResult
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RequestHeader
import org.springframework.web.bind.`annotation`.RequestParam
import org.springframework.web.service.`annotation`.HttpExchange
import kotlin.Any
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Map

@Suppress("unused")
public interface SearchClient {
    /**
     *
     */
    @HttpExchange(
        url = "/search",
        method = "GET",
        accept = ["application/json"],
    )
    public fun getSearch(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    ): SearchResult

    /**
     *
     *
     * @param searchRequest
     */
    @HttpExchange(
        url = "/search",
        method = "QUERY",
        contentType = "application/json",
        accept = ["application/json"],
    )
    public fun search(
        @RequestBody searchRequest: SearchRequest,
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    ): SearchResult
}
