package com.example.client

import com.example.models.Pet
import org.springframework.web.bind.`annotation`.RequestHeader
import org.springframework.web.bind.`annotation`.RequestParam
import org.springframework.web.service.`annotation`.HttpExchange
import javax.`annotation`.processing.Generated
import kotlin.Any
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map

@Suppress("unused")
@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    date = "2001-07-04T19:08:56.235Z",
    comments = "Generated with Fabrikt v27.0.1",
)
public interface PetsClient {
    /**
     * List all pets
     */
    @HttpExchange(
        url = "/pets",
        method = "GET",
        accept = ["application/json"],
    )
    public fun listPets(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam
        additionalQueryParameters: Map<String, Any> = emptyMap(),
    ): List<Pet>
}
