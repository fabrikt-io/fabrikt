package examples.directionalEndpoints.client

import examples.directionalEndpoints.models.PetRequest
import examples.directionalEndpoints.models.PetResponse
import org.springframework.web.bind.`annotation`.RequestHeader
import org.springframework.web.bind.`annotation`.RequestParam
import org.springframework.web.service.`annotation`.HttpExchange
import kotlin.Any
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
    @HttpExchange(
        url = "/pets/upload",
        method = "POST",
        contentType = "multipart/form-data",
        accept = ["application/json"],
    )
    public fun uploadPet(
        pet: PetRequest,
        token: String,
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    ): PetResponse
}
