package com.example.client

import com.example.models.Pet
import feign.HeaderMap
import feign.Headers
import feign.QueryMap
import feign.RequestLine
import javax.`annotation`.processing.Generated
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map

@Suppress("unused")
@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    comments = "Generated with Fabrikt v27.0.1",
)
public interface PetsClient {
    /**
     * List all pets
     */
    @RequestLine("GET /pets")
    @Headers("Accept: application/json")
    public fun listPets(
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): List<Pet>
}
