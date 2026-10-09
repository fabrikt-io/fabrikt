package examples.directionalEndpoints.client

import examples.directionalEndpoints.models.PetResponse
import feign.HeaderMap
import feign.Headers
import feign.QueryMap
import feign.RequestLine
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Map

@Suppress("unused")
public interface PetsIndexClient {
    /**
     *
     */
    @RequestLine("GET /pets/index")
    @Headers("Accept: application/json")
    public fun getPetIndex(
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): Map<String, PetResponse?>
}
