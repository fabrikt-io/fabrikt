package examples.directionalEndpoints.controllers

import examples.directionalEndpoints.models.PetResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.`annotation`.Controller
import io.micronaut.http.`annotation`.Get
import io.micronaut.http.`annotation`.Produces
import kotlin.collections.List

@Controller
public interface PetsListController {
    /**
     *
     */
    @Get(uri = "/pets/list")
    @Produces(value = ["application/json"])
    public fun getPets(): HttpResponse<List<PetResponse>>
}
