package examples.directionalEndpoints.client

import examples.directionalEndpoints.models.PetRequest
import examples.directionalEndpoints.models.PetResponse
import feign.HeaderMap
import feign.Headers
import feign.Param
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
    @FeignJsonParts("pet")
    @RequestLine("POST /pets/upload")
    @Headers(
        "Content-Type: multipart/form-data",
        "Accept: application/json",
    )
    public fun uploadPet(
        @Param("pet") pet: PetRequest,
        @Param("token") token: String,
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    ): PetResponse
}
