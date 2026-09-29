package examples.directionalEndpoints.client

import examples.directionalEndpoints.models.PetResponse
import feign.HeaderMap
import feign.Headers
import feign.QueryMap
import feign.RequestLine
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map

@Suppress("unused")
public interface PetsListClient {
    /**
     *
     */
    @RequestLine("GET /pets/list")
    @Headers("Accept: application/json")
    public fun getPets(
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): List<PetResponse>
}
