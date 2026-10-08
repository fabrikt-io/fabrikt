package examples.directionalEndpoints.client

import examples.directionalEndpoints.models.PetRequest
import examples.directionalEndpoints.models.PetResponse
import examples.directionalEndpoints.models.PetStatusRequest
import examples.directionalEndpoints.models.StateRequest
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RequestHeader
import org.springframework.web.bind.`annotation`.RequestParam
import org.springframework.web.service.`annotation`.HttpExchange
import kotlin.Any
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
    @HttpExchange(
        url = "/pets",
        method = "POST",
        contentType = "application/json",
        accept = ["application/json"],
    )
    public fun createPet(
        @RequestBody pet: PetRequest,
        @RequestParam("status") status: PetStatusRequest? = null,
        @RequestParam("state") state: StateRequest? = null,
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    ): PetResponse
}
