package examples.queryOperation.client

import examples.queryOperation.models.SearchRequest
import examples.queryOperation.models.SearchResult
import feign.HeaderMap
import feign.Headers
import feign.QueryMap
import feign.RequestLine
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Map

@Suppress("unused")
public interface SearchClient {
    /**
     *
     */
    @RequestLine("GET /search")
    @Headers("Accept: application/json")
    public fun getSearch(
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    ): SearchResult

    /**
     *
     *
     * @param searchRequest
     */
    @RequestLine("QUERY /search")
    @Headers("Accept: application/json")
    public fun search(
        searchRequest: SearchRequest,
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    ): SearchResult
}
