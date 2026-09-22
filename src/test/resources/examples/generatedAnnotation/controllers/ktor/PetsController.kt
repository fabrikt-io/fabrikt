package com.example.controllers

import com.example.models.Pet
import io.ktor.http.Headers
import io.ktor.http.Parameters
import io.ktor.server.application.call
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.MissingRequestParameterException
import io.ktor.server.plugins.ParameterConversionException
import io.ktor.server.routing.Route
import io.ktor.server.routing.`get`
import io.ktor.util.converters.ConversionService
import io.ktor.util.converters.DefaultConversionService
import io.ktor.util.reflect.typeInfo
import javax.`annotation`.processing.Generated
import kotlin.Any
import kotlin.String
import kotlin.collections.List

@Generated(
    value = ["io.fabrikt.cli.CodeGen"],
    date = "2001-07-04T19:08:56.235Z",
    comments = "Generated with Fabrikt v27.0.1",
)
public interface PetsController {
    /**
     * List all pets
     *
     * Route is expected to respond with [kotlin.collections.List<com.example.models.Pet>].
     * Use [com.example.controllers.TypedApplicationCall.respondTyped] to send the response.
     *
     * @param call Decorated ApplicationCall with additional typed respond methods
     */
    public suspend fun listPets(call: TypedApplicationCall<List<Pet>>)

    @Generated(
        value = ["io.fabrikt.cli.CodeGen"],
        date = "2001-07-04T19:08:56.235Z",
        comments = "Generated with Fabrikt v27.0.1",
    )
    public companion object {
        /**
         * Mounts all routes for the Pets resource
         *
         * - GET /pets List all pets
         */
        public fun Route.petsRoutes(controller: PetsController) {
            `get`("/pets") {
                controller.listPets(TypedApplicationCall(call))
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
