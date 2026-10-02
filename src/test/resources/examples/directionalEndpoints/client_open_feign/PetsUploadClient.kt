package examples.directionalEndpoints.client

import examples.directionalEndpoints.models.PetRequest
import examples.directionalEndpoints.models.PetResponse
import feign.HeaderMap
import feign.Headers
import feign.QueryMap
import feign.RequestLine
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Map

@Suppress("unused")
public interface PetsUploadClient {
    /**
     *
     *
     * @param pet
     * @param token
     */
    @RequestLine("POST /pets/upload")
    @Headers("Accept: application/json")
    public fun uploadPet(
        pet: PetRequest,
        token: String,
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    ): PetResponse
}
