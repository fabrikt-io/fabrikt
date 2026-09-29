package examples.directionalEndpoints.client

import examples.directionalEndpoints.models.PetResponse
import org.springframework.web.bind.`annotation`.RequestHeader
import org.springframework.web.bind.`annotation`.RequestParam
import org.springframework.web.service.`annotation`.HttpExchange
import kotlin.Any
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Map

@Suppress("unused")
public interface PetsIndexClient {
    /**
     *
     */
    @HttpExchange(
        url = "/pets/index",
        method = "GET",
        accept = ["application/json"],
    )
    public fun getPetIndex(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    ): Map<String, PetResponse?>
}
