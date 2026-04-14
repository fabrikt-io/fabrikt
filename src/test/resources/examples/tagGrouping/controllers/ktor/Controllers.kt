package examples.tagGrouping.controllers

import examples.tagGrouping.models.Owner
import examples.tagGrouping.models.Pet
import examples.tagGrouping.models.Vehicle
import io.ktor.http.Headers
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.MissingRequestParameterException
import io.ktor.server.plugins.ParameterConversionException
import io.ktor.server.plugins.dataconversion.conversionService
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.`get`
import io.ktor.server.routing.post
import io.ktor.util.converters.ConversionService
import io.ktor.util.converters.DefaultConversionService
import io.ktor.util.reflect.typeInfo
import java.util.UUID
import kotlin.Any
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List

public interface PetController {
    /**
     * List all pets
     *
     * Route is expected to respond with [kotlin.collections.List<examples.tagGrouping.models.Pet>].
     * Use [examples.tagGrouping.controllers.TypedApplicationCall.respondTyped] to send the response.
     *
     * @param limit
     * @param call Decorated ApplicationCall with additional typed respond methods
     */
    public suspend fun listPets(
        limit: Int?,
        call: TypedApplicationCall<List<Pet>>,
    )

    /**
     * Create a pet
     *
     * Route is expected to respond with status 201.
     * Use [respond] to send the response.
     *
     * @param pet
     * @param call The Ktor application call
     */
    public suspend fun createPet(
        pet: Pet,
        call: ApplicationCall,
    )

    /**
     * Get a pet by ID
     *
     * Route is expected to respond with [examples.tagGrouping.models.Pet].
     * Use [examples.tagGrouping.controllers.TypedApplicationCall.respondTyped] to send the response.
     *
     * @param petId
     * @param call Decorated ApplicationCall with additional typed respond methods
     */
    public suspend fun getPetById(
        petId: UUID,
        call: TypedApplicationCall<Pet>,
    )

    /**
     * Delete a pet
     *
     * Route is expected to respond with status 204.
     * Use [respond] to send the response.
     *
     * @param petId
     * @param call The Ktor application call
     */
    public suspend fun deletePet(
        petId: UUID,
        call: ApplicationCall,
    )

    public companion object {
        /**
         * Mounts all routes for the Pet resource
         *
         * - GET /pets List all pets
         * - POST /pets Create a pet
         * - GET /pets/{petId} Get a pet by ID
         * - DELETE /pets/{petId} Delete a pet
         */
        public fun Route.petRoutes(controller: PetController) {
            `get`("/pets") {
                val limit = call.request.queryParameters.getTyped<kotlin.Int>("limit")
                controller.listPets(limit, TypedApplicationCall(call))
            }
            post("/pets") {
                val pet = call.receive<Pet>()
                controller.createPet(pet, call)
            }
            `get`("/pets/{petId}") {
                val petId =
                    call.parameters.getTypedOrFail<java.util.UUID>(
                        "petId",
                        call.application.conversionService,
                    )
                controller.getPetById(petId, TypedApplicationCall(call))
            }
            delete("/pets/{petId}") {
                val petId =
                    call.parameters.getTypedOrFail<java.util.UUID>(
                        "petId",
                        call.application.conversionService,
                    )
                controller.deletePet(petId, call)
            }
        }

        /**
         * Gets parameter value associated with this name or null if the name is not present.
         * Converting to type R using ConversionService.
         *
         * Throws:
         *   ParameterConversionException - when conversion from String to R fails
         */
        private inline fun <reified R : Any> Parameters.getTyped(
            name: String,
            conversionService: ConversionService = DefaultConversionService,
        ): R? {
            val values = getAll(name) ?: return null
            val typeInfo = typeInfo<R>()
            return try {
                @Suppress("UNCHECKED_CAST")
                conversionService.fromValues(values, typeInfo) as R
            } catch (cause: Exception) {
                throw ParameterConversionException(
                    name,
                    typeInfo.type.simpleName
                        ?: typeInfo.type.toString(),
                    cause,
                )
            }
        }

        /**
         * Gets parameter value associated with this name or throws if the name is not present.
         * Converting to type R using ConversionService.
         *
         * Throws:
         *   MissingRequestParameterException - when parameter is missing
         *   ParameterConversionException - when conversion from String to R fails
         */
        private inline fun <reified R : Any> Parameters.getTypedOrFail(
            name: String,
            conversionService: ConversionService = DefaultConversionService,
        ): R {
            val values = getAll(name) ?: throw MissingRequestParameterException(name)
            val typeInfo = typeInfo<R>()
            return try {
                @Suppress("UNCHECKED_CAST")
                conversionService.fromValues(values, typeInfo) as R
            } catch (cause: Exception) {
                throw ParameterConversionException(
                    name,
                    typeInfo.type.simpleName
                        ?: typeInfo.type.toString(),
                    cause,
                )
            }
        }

        /**
         * Gets first value from the list of values associated with a name.
         *
         * Throws:
         *   BadRequestException - when the name is not present
         */
        private fun Headers.getOrFail(name: String): String =
            this[name] ?: throw
                BadRequestException("Header " + name + " is required")
    }
}

public interface OwnerController {
    /**
     * List all owners
     *
     * Route is expected to respond with [kotlin.collections.List<examples.tagGrouping.models.Owner>].
     * Use [examples.tagGrouping.controllers.TypedApplicationCall.respondTyped] to send the response.
     *
     * @param call Decorated ApplicationCall with additional typed respond methods
     */
    public suspend fun listOwners(call: TypedApplicationCall<List<Owner>>)

    /**
     * Create an owner
     *
     * Route is expected to respond with status 201.
     * Use [respond] to send the response.
     *
     * @param owner
     * @param call The Ktor application call
     */
    public suspend fun createOwner(
        owner: Owner,
        call: ApplicationCall,
    )

    /**
     * List pets belonging to an owner
     *
     * Route is expected to respond with [kotlin.collections.List<examples.tagGrouping.models.Pet>].
     * Use [examples.tagGrouping.controllers.TypedApplicationCall.respondTyped] to send the response.
     *
     * @param ownerId
     * @param call Decorated ApplicationCall with additional typed respond methods
     */
    public suspend fun listPetsByOwner(
        ownerId: UUID,
        call: TypedApplicationCall<List<Pet>>,
    )

    public companion object {
        /**
         * Mounts all routes for the Owner resource
         *
         * - GET /owners List all owners
         * - POST /owners Create an owner
         * - GET /owners/{ownerId}/pets List pets belonging to an owner
         */
        public fun Route.ownerRoutes(controller: OwnerController) {
            `get`("/owners") {
                controller.listOwners(TypedApplicationCall(call))
            }
            post("/owners") {
                val owner = call.receive<Owner>()
                controller.createOwner(owner, call)
            }
            `get`("/owners/{ownerId}/pets") {
                val ownerId =
                    call.parameters.getTypedOrFail<java.util.UUID>(
                        "ownerId",
                        call.application.conversionService,
                    )
                controller.listPetsByOwner(ownerId, TypedApplicationCall(call))
            }
        }

        /**
         * Gets parameter value associated with this name or null if the name is not present.
         * Converting to type R using ConversionService.
         *
         * Throws:
         *   ParameterConversionException - when conversion from String to R fails
         */
        private inline fun <reified R : Any> Parameters.getTyped(
            name: String,
            conversionService: ConversionService = DefaultConversionService,
        ): R? {
            val values = getAll(name) ?: return null
            val typeInfo = typeInfo<R>()
            return try {
                @Suppress("UNCHECKED_CAST")
                conversionService.fromValues(values, typeInfo) as R
            } catch (cause: Exception) {
                throw ParameterConversionException(
                    name,
                    typeInfo.type.simpleName
                        ?: typeInfo.type.toString(),
                    cause,
                )
            }
        }

        /**
         * Gets parameter value associated with this name or throws if the name is not present.
         * Converting to type R using ConversionService.
         *
         * Throws:
         *   MissingRequestParameterException - when parameter is missing
         *   ParameterConversionException - when conversion from String to R fails
         */
        private inline fun <reified R : Any> Parameters.getTypedOrFail(
            name: String,
            conversionService: ConversionService = DefaultConversionService,
        ): R {
            val values = getAll(name) ?: throw MissingRequestParameterException(name)
            val typeInfo = typeInfo<R>()
            return try {
                @Suppress("UNCHECKED_CAST")
                conversionService.fromValues(values, typeInfo) as R
            } catch (cause: Exception) {
                throw ParameterConversionException(
                    name,
                    typeInfo.type.simpleName
                        ?: typeInfo.type.toString(),
                    cause,
                )
            }
        }

        /**
         * Gets first value from the list of values associated with a name.
         *
         * Throws:
         *   BadRequestException - when the name is not present
         */
        private fun Headers.getOrFail(name: String): String =
            this[name] ?: throw
                BadRequestException("Header " + name + " is required")
    }
}

public interface VehicleController {
    /**
     * List all vehicles (tagged vehicle, alphabetically first verb=get wins)
     *
     * Route is expected to respond with
     * [kotlin.collections.List<examples.tagGrouping.models.Vehicle>].
     * Use [examples.tagGrouping.controllers.TypedApplicationCall.respondTyped] to send the response.
     *
     * @param call Decorated ApplicationCall with additional typed respond methods
     */
    public suspend fun listVehicles(call: TypedApplicationCall<List<Vehicle>>)

    /**
     * Create a vehicle (tagged owner, but post > get alphabetically so owner tag does NOT win)
     *
     * Route is expected to respond with status 201.
     * Use [respond] to send the response.
     *
     * @param vehicle
     * @param call The Ktor application call
     */
    public suspend fun createVehicle(
        vehicle: Vehicle,
        call: ApplicationCall,
    )

    public companion object {
        /**
         * Mounts all routes for the Vehicle resource
         *
         * - GET /vehicles List all vehicles (tagged vehicle, alphabetically first verb=get wins)
         * - POST /vehicles Create a vehicle (tagged owner, but post > get alphabetically so owner tag
         * does NOT win)
         */
        public fun Route.vehicleRoutes(controller: VehicleController) {
            `get`("/vehicles") {
                controller.listVehicles(TypedApplicationCall(call))
            }
            post("/vehicles") {
                val vehicle = call.receive<Vehicle>()
                controller.createVehicle(vehicle, call)
            }
        }

        /**
         * Gets parameter value associated with this name or null if the name is not present.
         * Converting to type R using ConversionService.
         *
         * Throws:
         *   ParameterConversionException - when conversion from String to R fails
         */
        private inline fun <reified R : Any> Parameters.getTyped(
            name: String,
            conversionService: ConversionService = DefaultConversionService,
        ): R? {
            val values = getAll(name) ?: return null
            val typeInfo = typeInfo<R>()
            return try {
                @Suppress("UNCHECKED_CAST")
                conversionService.fromValues(values, typeInfo) as R
            } catch (cause: Exception) {
                throw ParameterConversionException(
                    name,
                    typeInfo.type.simpleName
                        ?: typeInfo.type.toString(),
                    cause,
                )
            }
        }

        /**
         * Gets parameter value associated with this name or throws if the name is not present.
         * Converting to type R using ConversionService.
         *
         * Throws:
         *   MissingRequestParameterException - when parameter is missing
         *   ParameterConversionException - when conversion from String to R fails
         */
        private inline fun <reified R : Any> Parameters.getTypedOrFail(
            name: String,
            conversionService: ConversionService = DefaultConversionService,
        ): R {
            val values = getAll(name) ?: throw MissingRequestParameterException(name)
            val typeInfo = typeInfo<R>()
            return try {
                @Suppress("UNCHECKED_CAST")
                conversionService.fromValues(values, typeInfo) as R
            } catch (cause: Exception) {
                throw ParameterConversionException(
                    name,
                    typeInfo.type.simpleName
                        ?: typeInfo.type.toString(),
                    cause,
                )
            }
        }

        /**
         * Gets first value from the list of values associated with a name.
         *
         * Throws:
         *   BadRequestException - when the name is not present
         */
        private fun Headers.getOrFail(name: String): String =
            this[name] ?: throw
                BadRequestException("Header " + name + " is required")
    }
}

/**
 * Decorator for Ktor's ApplicationCall that provides type safe variants of the [respond] functions.
 *
 * It can be used as a drop-in replacement for [io.ktor.server.application.ApplicationCall].
 *
 * @param R The type of the response body
 */
public class TypedApplicationCall<R : Any>(
    private val applicationCall: ApplicationCall,
) : ApplicationCall by applicationCall {
    @Suppress("unused")
    public suspend inline fun <reified T : R> respondTyped(message: T) {
        respond(message)
    }

    @Suppress("unused")
    public suspend inline fun <reified T : R> respondTyped(
        status: HttpStatusCode,
        message: T,
    ) {
        respond(status, message)
    }
}
