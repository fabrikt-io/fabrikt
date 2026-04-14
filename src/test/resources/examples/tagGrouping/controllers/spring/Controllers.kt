package examples.tagGrouping.controllers

import examples.tagGrouping.models.Owner
import examples.tagGrouping.models.Pet
import examples.tagGrouping.models.Vehicle
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.PathVariable
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import org.springframework.web.bind.`annotation`.RequestParam
import java.util.UUID
import javax.validation.Valid
import kotlin.Int
import kotlin.Unit
import kotlin.collections.List

@Controller
@Validated
@RequestMapping("")
public interface PetController {
    /**
     * List all pets
     *
     * @param limit
     */
    @RequestMapping(
        value = ["/pets"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun listPets(
        @RequestParam(value = "limit", required = false) limit: Int?,
    ): ResponseEntity<List<Pet>>

    /**
     * Create a pet
     *
     * @param pet
     */
    @RequestMapping(
        value = ["/pets"],
        produces = [],
        method = [RequestMethod.POST],
        consumes = ["application/json"],
    )
    public fun createPet(
        @RequestBody @Valid pet: Pet,
    ): ResponseEntity<Unit>

    /**
     * Get a pet by ID
     *
     * @param petId
     */
    @RequestMapping(
        value = ["/pets/{petId}"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun getPetById(
        @PathVariable(value = "petId", required = true) petId: UUID,
    ): ResponseEntity<Pet>

    /**
     * Delete a pet
     *
     * @param petId
     */
    @RequestMapping(
        value = ["/pets/{petId}"],
        produces = [],
        method = [RequestMethod.DELETE],
    )
    public fun deletePet(
        @PathVariable(value = "petId", required = true) petId: UUID,
    ): ResponseEntity<Unit>
}

@Controller
@Validated
@RequestMapping("")
public interface OwnerController {
    /**
     * List all owners
     */
    @RequestMapping(
        value = ["/owners"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun listOwners(): ResponseEntity<List<Owner>>

    /**
     * Create an owner
     *
     * @param owner
     */
    @RequestMapping(
        value = ["/owners"],
        produces = [],
        method = [RequestMethod.POST],
        consumes = ["application/json"],
    )
    public fun createOwner(
        @RequestBody @Valid owner: Owner,
    ): ResponseEntity<Unit>

    /**
     * List pets belonging to an owner
     *
     * @param ownerId
     */
    @RequestMapping(
        value = ["/owners/{ownerId}/pets"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun listPetsByOwner(
        @PathVariable(value = "ownerId", required = true) ownerId: UUID,
    ): ResponseEntity<List<Pet>>
}

@Controller
@Validated
@RequestMapping("")
public interface VehicleController {
    /**
     * List all vehicles (tagged vehicle, alphabetically first verb=get wins)
     */
    @RequestMapping(
        value = ["/vehicles"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun listVehicles(): ResponseEntity<List<Vehicle>>

    /**
     * Create a vehicle (tagged owner, but post > get alphabetically so owner tag does NOT win)
     *
     * @param vehicle
     */
    @RequestMapping(
        value = ["/vehicles"],
        produces = [],
        method = [RequestMethod.POST],
        consumes = ["application/json"],
    )
    public fun createVehicle(
        @RequestBody @Valid vehicle: Vehicle,
    ): ResponseEntity<Unit>
}
