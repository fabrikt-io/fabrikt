package examples.tagGrouping.controllers

import examples.tagGrouping.models.Owner
import examples.tagGrouping.models.Pet
import examples.tagGrouping.models.Vehicle
import io.micronaut.http.HttpResponse
import io.micronaut.http.`annotation`.Body
import io.micronaut.http.`annotation`.Consumes
import io.micronaut.http.`annotation`.Controller
import io.micronaut.http.`annotation`.Delete
import io.micronaut.http.`annotation`.Get
import io.micronaut.http.`annotation`.PathVariable
import io.micronaut.http.`annotation`.Post
import io.micronaut.http.`annotation`.Produces
import io.micronaut.http.`annotation`.QueryValue
import io.micronaut.security.rules.SecurityRule
import java.util.UUID
import javax.validation.Valid
import kotlin.Int
import kotlin.Unit
import kotlin.collections.List

@Controller
public interface PetController {
    /**
     * List all pets
     *
     * @param limit
     */
    @Get(uri = "/pets")
    @Produces(value = ["application/json"])
    public fun listPets(
        @QueryValue(value = "limit") limit: Int?,
    ): HttpResponse<List<Pet>>

    /**
     * Create a pet
     *
     * @param pet
     */
    @Post(uri = "/pets")
    @Consumes(value = ["application/json"])
    public fun createPet(
        @Body @Valid pet: Pet,
    ): HttpResponse<Unit>

    /**
     * Get a pet by ID
     *
     * @param petId
     */
    @Get(uri = "/pets/{petId}")
    @Produces(value = ["application/json"])
    public fun getPetById(
        @PathVariable(value = "petId") petId: UUID,
    ): HttpResponse<Pet>

    /**
     * Delete a pet
     *
     * @param petId
     */
    @Delete(uri = "/pets/{petId}")
    public fun deletePet(
        @PathVariable(value = "petId") petId: UUID,
    ): HttpResponse<Unit>
}

@Controller
public interface OwnerController {
    /**
     * List all owners
     */
    @Get(uri = "/owners")
    @Produces(value = ["application/json"])
    public fun listOwners(): HttpResponse<List<Owner>>

    /**
     * Create an owner
     *
     * @param owner
     */
    @Post(uri = "/owners")
    @Consumes(value = ["application/json"])
    public fun createOwner(
        @Body @Valid owner: Owner,
    ): HttpResponse<Unit>

    /**
     * List pets belonging to an owner
     *
     * @param ownerId
     */
    @Get(uri = "/owners/{ownerId}/pets")
    @Produces(value = ["application/json"])
    public fun listPetsByOwner(
        @PathVariable(value = "ownerId") ownerId: UUID,
    ): HttpResponse<List<Pet>>
}

@Controller
public interface VehicleController {
    /**
     * List all vehicles (tagged vehicle, alphabetically first verb=get wins)
     */
    @Get(uri = "/vehicles")
    @Produces(value = ["application/json"])
    public fun listVehicles(): HttpResponse<List<Vehicle>>

    /**
     * Create a vehicle (tagged owner, but post > get alphabetically so owner tag does NOT win)
     *
     * @param vehicle
     */
    @Post(uri = "/vehicles")
    @Consumes(value = ["application/json"])
    public fun createVehicle(
        @Body @Valid vehicle: Vehicle,
    ): HttpResponse<Unit>
}