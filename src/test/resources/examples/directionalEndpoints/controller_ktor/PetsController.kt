package examples.directionalEndpoints.controllers

import examples.directionalEndpoints.models.PetRequest
import examples.directionalEndpoints.models.PetResponse
import examples.directionalEndpoints.models.PetStatusRequest
import examples.directionalEndpoints.models.StateRequest
import io.ktor.http.Headers
import io.ktor.http.Parameters
import io.ktor.server.application.call
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.MissingRequestParameterException
import io.ktor.server.plugins.ParameterConversionException
import io.ktor.server.plugins.dataconversion.conversionService
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.util.converters.ConversionService
import io.ktor.util.converters.DefaultConversionService
import io.ktor.util.reflect.typeInfo
import kotlin.Any
import kotlin.String

public interface PetsController {
    /**
     * Route is expected to respond with [examples.directionalEndpoints.models.PetResponse].
     * Use [examples.directionalEndpoints.controllers.TypedApplicationCall.respondTyped] to send the
     * response.
     *
     * @param pet
     * @param status
     * @param state
     * @param call Decorated ApplicationCall with additional typed respond methods
     */
    public suspend fun createPet(
        status: PetStatusRequest?,
        state: StateRequest?,
        pet: PetRequest,
        call: TypedApplicationCall<PetResponse>,
    )

    public companion object {
        /**
         * Mounts all routes for the Pets resource
         *
         * - POST /pets
         */
        public fun Route.petsRoutes(controller: PetsController) {
            post("/pets") {
                val status =
                    call.request.queryParameters.getTyped<examples.directionalEndpoints.models.PetStatusRequest>(
                        "status",
                        call.application.conversionService,
                    )
                val state =
                    call.request.queryParameters.getTyped<examples.directionalEndpoints.models.StateRequest>(
                        "state",
                        call.application.conversionService,
                    )
                val pet = call.receive<PetRequest>()
                controller.createPet(status, state, pet, TypedApplicationCall(call))
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
