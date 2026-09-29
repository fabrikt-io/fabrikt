package examples.directionalEndpoints.controllers

import examples.directionalEndpoints.models.PetRequest
import examples.directionalEndpoints.models.PetResponse
import examples.directionalEndpoints.models.PetStatusRequest
import examples.directionalEndpoints.models.StateRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.`annotation`.Body
import io.micronaut.http.`annotation`.Consumes
import io.micronaut.http.`annotation`.Controller
import io.micronaut.http.`annotation`.Post
import io.micronaut.http.`annotation`.Produces
import io.micronaut.http.`annotation`.QueryValue

@Controller
public interface PetsController {
    /**
     *
     *
     * @param pet
     * @param status
     * @param state
     */
    @Post(uri = "/pets")
    @Consumes(value = ["application/json"])
    @Produces(value = ["application/json"])
    public fun createPet(
        @Body pet: PetRequest,
        @QueryValue(value = "status") status: PetStatusRequest?,
        @QueryValue(value = "state") state: StateRequest?,
    ): HttpResponse<PetResponse>
}
