package examples.multipartFormData.controllers

import examples.multipartFormData.models.UploadResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.MissingRequestParameterException
import io.ktor.server.plugins.ParameterConversionException
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.util.converters.ConversionService
import io.ktor.util.converters.DefaultConversionService
import io.ktor.util.reflect.typeInfo
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import kotlin.Any
import kotlin.ByteArray
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List

public interface FilesUploadController {
    /**
     * Upload a single file
     *
     * Route is expected to respond with [examples.multipartFormData.models.UploadResponse].
     * Use [examples.multipartFormData.controllers.TypedApplicationCall.respondTyped] to send the
     * response.
     *
     * @param file The file to upload
     * @param description Optional description of the file
     * @param call Decorated ApplicationCall with additional typed respond methods
     */
    public suspend fun uploadFile(
        `file`: ReceivedFile,
        description: String?,
        call: TypedApplicationCall<UploadResponse>,
    )

    public companion object {
        /**
         * Mounts all routes for the FilesUpload resource
         *
         * - POST /files/upload Upload a single file
         */
        public fun Route.filesUploadRoutes(controller: FilesUploadController) {
            post("/files/upload") {
                val multipartData = call.receiveMultipart()
                var fileReceived: ReceivedFile? = null
                var descriptionValue: String? = null
                multipartData.forEachPart { part ->
                    when (part.name) {
                        "file" ->
                            if (part is PartData.FileItem) {
                                fileReceived =
                                    ReceivedFile(
                                        part.provider().readRemaining().readByteArray(),
                                        part.originalFileName,
                                        part.contentType,
                                    )
                            }
                        "description" -> if (part is PartData.FormItem) descriptionValue = part.value
                    }
                }
                val file =
                    fileReceived ?: throw
                        BadRequestException("Missing required multipart part: file")
                val description = descriptionValue
                controller.uploadFile(file, description, TypedApplicationCall(call))
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

public interface FilesUploadMultipleController {
    /**
     * Upload multiple files
     *
     * Route is expected to respond with [examples.multipartFormData.models.UploadResponse].
     * Use [examples.multipartFormData.controllers.TypedApplicationCall.respondTyped] to send the
     * response.
     *
     * @param files The files to upload
     * @param category Category for the uploaded files
     * @param call Decorated ApplicationCall with additional typed respond methods
     */
    public suspend fun uploadMultipleFiles(
        files: List<ReceivedFile>,
        category: String,
        call: TypedApplicationCall<UploadResponse>,
    )

    public companion object {
        /**
         * Mounts all routes for the FilesUploadMultiple resource
         *
         * - POST /files/upload-multiple Upload multiple files
         */
        public fun Route.filesUploadMultipleRoutes(controller: FilesUploadMultipleController) {
            post("/files/upload-multiple") {
                val multipartData = call.receiveMultipart()
                val filesParts = mutableListOf<ReceivedFile>()
                var categoryValue: String? = null
                multipartData.forEachPart { part ->
                    when (part.name) {
                        "files" ->
                            if (part is PartData.FileItem) {
                                filesParts.add(
                                    ReceivedFile(
                                        part.provider().readRemaining().readByteArray(),
                                        part.originalFileName,
                                        part.contentType,
                                    ),
                                )
                            }
                        "category" -> if (part is PartData.FormItem) categoryValue = part.value
                    }
                }
                val files = filesParts.toList()
                val category =
                    categoryValue ?: throw
                        BadRequestException("Missing required multipart part: category")
                controller.uploadMultipleFiles(files, category, TypedApplicationCall(call))
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
