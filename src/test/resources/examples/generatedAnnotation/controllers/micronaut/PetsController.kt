package com.example.controllers

import com.example.models.Pet
import io.micronaut.http.HttpResponse
import io.micronaut.http.`annotation`.Controller
import io.micronaut.http.`annotation`.Get
import io.micronaut.http.`annotation`.Produces
import javax.`annotation`.processing.Generated
import kotlin.collections.List

@Controller
@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    date = "2001-07-04T19:08:56.235Z",
    comments = "Generated with Fabrikt v27.0.1",
)
public interface PetsController {
    /**
     * List all pets
     */
    @Get(uri = "/pets")
    @Produces(value = ["application/json"])
    public fun listPets(): HttpResponse<List<Pet>>
}
