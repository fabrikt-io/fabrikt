package examples.tagGrouping.client

import examples.tagGrouping.models.Owner
import examples.tagGrouping.models.Pet
import examples.tagGrouping.models.Vehicle
import feign.HeaderMap
import feign.Headers
import feign.Param
import feign.QueryMap
import feign.RequestLine
import java.util.UUID
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
    @RequestLine("GET /pets?limit={limit}")
    @Headers("Accept: application/json")
    public fun listPets(
        @Param("limit") limit: Int? = null,
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    ): List<Pet>

    /**
     * Create a pet
     *
     * @param pet
     */
    @RequestLine("POST /pets")
    public fun createPet(
        pet: Pet,
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    )

    /**
     * Get a pet by ID
     *
     * @param petId
     */
    @RequestLine("GET /pets/{petId}")
    @Headers("Accept: application/json")
    public fun getPetById(
        @Param("petId") petId: UUID,
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    ): Pet

    /**
     * Delete a pet
     *
     * @param petId
     */
    @RequestLine("DELETE /pets/{petId}")
    public fun deletePet(
        @Param("petId") petId: UUID,
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    )
}

@Suppress("unused")
public interface OwnerClient {
    /**
     * List all owners
     */
    @RequestLine("GET /owners")
    @Headers("Accept: application/json")
    public fun listOwners(
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): List<Owner>

    /**
     * Create an owner
     *
     * @param owner
     */
    @RequestLine("POST /owners")
    public fun createOwner(
        owner: Owner,
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    )

    /**
     * List pets belonging to an owner
     *
     * @param ownerId
     */
    @RequestLine("GET /owners/{ownerId}/pets")
    @Headers("Accept: application/json")
    public fun listPetsByOwner(
        @Param("ownerId") ownerId: UUID,
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    ): List<Pet>
}

@Suppress("unused")
public interface VehicleClient {
    /**
     * List all vehicles (tagged vehicle, alphabetically first verb=get wins)
     */
    @RequestLine("GET /vehicles")
    @Headers("Accept: application/json")
    public fun listVehicles(
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): List<Vehicle>

    /**
     * Create a vehicle (tagged owner, but post > get alphabetically so owner tag does NOT win)
     *
     * @param vehicle
     */
    @RequestLine("POST /vehicles")
    public fun createVehicle(
        vehicle: Vehicle,
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    )
}
