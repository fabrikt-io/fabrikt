package examples.directionalEndpoints.client

import examples.directionalEndpoints.models.PetRequest
import examples.directionalEndpoints.models.PetResponse
import examples.directionalEndpoints.models.PetStatusRequest
import examples.directionalEndpoints.models.StateRequest
import feign.HeaderMap
import feign.Headers
import feign.Param
import feign.QueryMap
import feign.RequestLine
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Map

@Suppress("unused")
public interface PetsClient {
    /**
     *
     *
     * @param pet
     * @param status
     * @param state
     */
    @RequestLine("POST /pets?status={status}&state={state}")
    @Headers("Accept: application/json")
    public fun createPet(
        pet: PetRequest,
        @Param("status") status: PetStatusRequest? = null,
        @Param("state") state: StateRequest? = null,
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    ): PetResponse
}
