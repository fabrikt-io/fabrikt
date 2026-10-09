package examples.directionalEndpoints.controllers

import examples.directionalEndpoints.models.PetResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.`annotation`.Controller
import io.micronaut.http.`annotation`.Get
import io.micronaut.http.`annotation`.Produces
import kotlin.String
import kotlin.collections.Map

@Controller
public interface PetsIndexController {
    /**
     *
     */
    @Get(uri = "/pets/index")
    @Produces(value = ["application/json"])
    public fun getPetIndex(): HttpResponse<Map<String, PetResponse?>>
}
