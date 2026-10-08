package examples.directionalEndpoints.client

import examples.directionalEndpoints.models.PetResponse
import org.springframework.web.bind.`annotation`.RequestHeader
import org.springframework.web.bind.`annotation`.RequestParam
import org.springframework.web.service.`annotation`.HttpExchange
import kotlin.Any
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map

@Suppress("unused")
public interface PetsListClient {
    /**
     *
     */
    @HttpExchange(
        url = "/pets/list",
        method = "GET",
        accept = ["application/json"],
    )
    public fun getPets(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam
        additionalQueryParameters: Map<String, Any> = emptyMap(),
    ): List<PetResponse>
}
