package examples.tagGrouping.client

import examples.tagGrouping.models.Owner
import examples.tagGrouping.models.Pet
import examples.tagGrouping.models.Vehicle
import org.springframework.web.bind.`annotation`.PathVariable
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RequestHeader
import org.springframework.web.bind.`annotation`.RequestParam
import org.springframework.web.service.`annotation`.HttpExchange
import java.util.UUID
import kotlin.Any
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map

@Suppress("unused")
public interface PetClient {
    /**
     * List all pets
     *
     * @param limit
     */
    @HttpExchange(
        url = "/pets",
        method = "GET",
        accept = ["application/json"],
    )
    public fun listPets(
        @RequestParam("limit") limit: Int? = null,
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    ): List<Pet>

    /**
     * Create a pet
     *
     * @param pet
     */
    @HttpExchange(
        url = "/pets",
        method = "POST",
    )
    public fun createPet(
        @RequestBody pet: Pet,
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )

    /**
     * Get a pet by ID
     *
     * @param petId
     */
    @HttpExchange(
        url = "/pets/{petId}",
        method = "GET",
        accept = ["application/json"],
    )
    public fun getPetById(
        @PathVariable("petId") petId: UUID,
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    ): Pet

    /**
     * Delete a pet
     *
     * @param petId
     */
    @HttpExchange(
        url = "/pets/{petId}",
        method = "DELETE",
    )
    public fun deletePet(
        @PathVariable("petId") petId: UUID,
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )
}

@Suppress("unused")
public interface OwnerClient {
    /**
     * List all owners
     */
    @HttpExchange(
        url = "/owners",
        method = "GET",
        accept = ["application/json"],
    )
    public fun listOwners(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    ): List<Owner>

    /**
     * Create an owner
     *
     * @param owner
     */
    @HttpExchange(
        url = "/owners",
        method = "POST",
    )
    public fun createOwner(
        @RequestBody owner: Owner,
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )

    /**
     * List pets belonging to an owner
     *
     * @param ownerId
     */
    @HttpExchange(
        url = "/owners/{ownerId}/pets",
        method = "GET",
        accept = ["application/json"],
    )
    public fun listPetsByOwner(
        @PathVariable("ownerId") ownerId: UUID,
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    ): List<Pet>
}

@Suppress("unused")
public interface VehicleClient {
    /**
     * List all vehicles (tagged vehicle, alphabetically first verb=get wins)
     */
    @HttpExchange(
        url = "/vehicles",
        method = "GET",
        accept = ["application/json"],
    )
    public fun listVehicles(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    ): List<Vehicle>

    /**
     * Create a vehicle (tagged owner, but post > get alphabetically so owner tag does NOT win)
     *
     * @param vehicle
     */
    @HttpExchange(
        url = "/vehicles",
        method = "POST",
    )
    public fun createVehicle(
        @RequestBody vehicle: Vehicle,
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )
}
