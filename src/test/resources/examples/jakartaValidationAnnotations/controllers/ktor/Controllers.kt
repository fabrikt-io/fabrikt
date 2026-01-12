package examples.jakartaValidationAnnotations.controllers

import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.MissingRequestParameterException
import io.ktor.server.plugins.ParameterConversionException
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.`get`
import io.ktor.util.converters.ConversionService
import io.ktor.util.converters.DefaultConversionService
import io.ktor.util.reflect.typeInfo
import kotlin.Any
import kotlin.ByteArray
import kotlin.Long
import kotlin.String
import kotlin.Suppress

public interface MaximumTestController {
    /**
     * Route is expected to respond with status 204.
     * Use [respond] to send the response.
     *
     * @param pathId
     * @param headerid
     * @param queryid
     * @param call The Ktor application call
     */
    public suspend fun getById(
        headerid: String?,
        pathId: Long?,
        queryid: Long?,
        call: ApplicationCall,
    )

    public companion object {
        /**
         * Mounts all routes for the MaximumTest resource
         *
         * - GET /maximumTest/{pathId}
         */
        public fun Route.maximumTestRoutes(controller: MaximumTestController) {
            `get`("/maximumTest/{pathId}") {
                val pathId = call.parameters.getTypedOrFail<kotlin.Long>("pathId")
                val headerid = call.request.headers["headerid"]
                val queryid = call.request.queryParameters.getTyped<kotlin.Long>("queryid")
                controller.getById(headerid, pathId, queryid, call)
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

public interface MinimumTestController {
    /**
     * Route is expected to respond with status 204.
     * Use [respond] to send the response.
     *
     * @param pathId
     * @param headerid
     * @param queryid
     * @param call The Ktor application call
     */
    public suspend fun getById(
        headerid: String?,
        pathId: Long?,
        queryid: Long?,
        call: ApplicationCall,
    )

    public companion object {
        /**
         * Mounts all routes for the MinimumTest resource
         *
         * - GET /minimumTest/{pathId}
         */
        public fun Route.minimumTestRoutes(controller: MinimumTestController) {
            `get`("/minimumTest/{pathId}") {
                val pathId = call.parameters.getTypedOrFail<kotlin.Long>("pathId")
                val headerid = call.request.headers["headerid"]
                val queryid = call.request.queryParameters.getTyped<kotlin.Long>("queryid")
                controller.getById(headerid, pathId, queryid, call)
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

public interface MinMaxTestController {
    /**
     * Route is expected to respond with status 204.
     * Use [respond] to send the response.
     *
     * @param pathId
     * @param headerid
     * @param queryid
     * @param call The Ktor application call
     */
    public suspend fun getById(
        headerid: String?,
        pathId: Long?,
        queryid: Long?,
        call: ApplicationCall,
    )

    public companion object {
        /**
         * Mounts all routes for the MinMaxTest resource
         *
         * - GET /minMaxTest/{pathId}
         */
        public fun Route.minMaxTestRoutes(controller: MinMaxTestController) {
            `get`("/minMaxTest/{pathId}") {
                val pathId = call.parameters.getTypedOrFail<kotlin.Long>("pathId")
                val headerid = call.request.headers["headerid"]
                val queryid = call.request.queryParameters.getTyped<kotlin.Long>("queryid")
                controller.getById(headerid, pathId, queryid, call)
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

/**
 * Wrapper for received file content from multipart uploads.
 *
 * @property content The raw file content as a byte array
 * @property originalFileName The original filename from the Content-Disposition header, if provided
 * @property contentType The content type of the file, if provided
 */
public data class ReceivedFile(
    public val content: ByteArray,
    public val originalFileName: String? = null,
    public val contentType: ContentType? = null,
)
