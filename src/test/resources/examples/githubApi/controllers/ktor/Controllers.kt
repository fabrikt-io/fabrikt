package examples.githubApi.controllers

import examples.githubApi.models.BulkEntityDetails
import examples.githubApi.models.Contributor
import examples.githubApi.models.ContributorQueryResult
import examples.githubApi.models.Event
import examples.githubApi.models.EventResults
import examples.githubApi.models.Organisation
import examples.githubApi.models.OrganisationQueryResult
import examples.githubApi.models.PullRequest
import examples.githubApi.models.PullRequestQueryResult
import examples.githubApi.models.Repository
import examples.githubApi.models.RepositoryQueryResult
import examples.githubApi.models.StatusQueryParam
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
import io.ktor.server.routing.put
import io.ktor.util.converters.ConversionService
import io.ktor.util.converters.DefaultConversionService
import io.ktor.util.reflect.typeInfo
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List

public interface InternalEventsController {
    /**
     * Generate change events for a list of entities
     *
     * Route is expected to respond with [examples.githubApi.models.EventResults].
     * Use [examples.githubApi.controllers.TypedApplicationCall.respondTyped] to send the response.
     *
     * @param bulkEntityDetails
     * @param call Decorated ApplicationCall with additional typed respond methods
     */
    public suspend fun post(
        bulkEntityDetails: BulkEntityDetails,
        call: TypedApplicationCall<EventResults>,
    )

    public companion object {
        /**
         * Mounts all routes for the InternalEvents resource
         *
         * - POST /internal/events Generate change events for a list of entities
         */
        public fun Route.internalEventsRoutes(controller: InternalEventsController) {
            post("/internal/events") {
                val bulkEntityDetails = call.receive<BulkEntityDetails>()
                controller.post(bulkEntityDetails, TypedApplicationCall(call))
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

public interface InternalEventsStreamController {
    /**
     * Stream events for an entity
     *
     * Route is expected to respond with [kotlin.collections.List<examples.githubApi.models.Event>].
     * Use [examples.githubApi.controllers.TypedApplicationCall.respondTyped] to send the response.
     *
     * @param entityId
     * @param call Decorated ApplicationCall with additional typed respond methods
     */
    public suspend fun `get`(
        entityId: Any,
        call: TypedApplicationCall<List<Event>>,
    )

    public companion object {
        /**
         * Mounts all routes for the InternalEventsStream resource
         *
         * - GET /internal/events/{entity-id}/stream Stream events for an entity
         */
        public fun Route.internalEventsStreamRoutes(controller: InternalEventsStreamController) {
            `get`("/internal/events/{entity-id}/stream") {
                val entityId =
                    call.parameters.getTypedOrFail<kotlin.Any>(
                        "entity-id",
                        call.application.conversionService,
                    )
                controller.get(entityId, TypedApplicationCall(call))
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

public interface ContributorsController {
    /**
     * Page through all the Contributor resources matching the query filters
     *
     * Route is expected to respond with [examples.githubApi.models.ContributorQueryResult].
     * Use [examples.githubApi.controllers.TypedApplicationCall.respondTyped] to send the response.
     *
     * @param limit Upper bound for number of results to be returned from query api. A default limit
     * will be applied if this is not present
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     * @param includeInactive A query parameter to request both active and inactive entities
     * @param cursor An encoded value which represents a point in the data from where pagination will
     * commence. The pagination direction (either forward or backward) will also be encoded within the
     * cursor value. The cursor value will always be generated server side
     * @param call Decorated ApplicationCall with additional typed respond methods
     */
    public suspend fun searchContributors(
        xFlowId: String?,
        limit: Int?,
        includeInactive: Boolean?,
        cursor: String?,
        call: TypedApplicationCall<ContributorQueryResult>,
    )

    /**
     * Create a new Contributor
     *
     * Route is expected to respond with status 201.
     * Use [respond] to send the response.
     *
     * @param contributor
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     * @param idempotencyKey This unique identifier can be used to allow for clients to safely retry
     * requests without accidentally performing the same operation twice. This is useful in cases such as
     * network failures. This Id should remain the same for a given operation, so that the server can use
     * it to recognise subsequent retries of the same request. Clients should be careful in using this
     * key as subsequent requests with the same key return the same result. How the key is generated is
     * up to the client, but it is suggested to use UUID (v4), or any other random string with enough
     * entropy to avoid collisions.
     * @param call The Ktor application call
     */
    public suspend fun createContributor(
        xFlowId: String?,
        idempotencyKey: String?,
        contributor: Contributor,
        call: ApplicationCall,
    )

    /**
     * Get a Contributor by ID
     *
     * Route is expected to respond with [examples.githubApi.models.Contributor].
     * Use [examples.githubApi.controllers.TypedApplicationCall.respondTyped] to send the response.
     *
     * @param id The unique id for the resource
     * @param status Changes the behavior of GET | HEAD based on the value of the status property.
     * Will return a 404 if the status property does not match the query parameter."
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     * @param ifNoneMatch The RFC7232 If-None-Match header field in a request requires the server to
     * only operate on the resource if it does not match any of the provided entity-tags. If the provided
     * entity-tag is `*`, it is required that the resource does not exist at all.
     * @param call Decorated ApplicationCall with additional typed respond methods
     */
    public suspend fun getContributor(
        xFlowId: String?,
        ifNoneMatch: String?,
        id: String,
        status: StatusQueryParam?,
        call: TypedApplicationCall<Contributor>,
    )

    /**
     * Update an existing Contributor
     *
     * Route is expected to respond with status 204.
     * Use [respond] to send the response.
     *
     * @param contributor
     * @param id The unique id for the resource
     * @param ifMatch The RFC7232 If-Match header field in a request requires the server to only
     * operate on the resource that matches at least one of the provided entity-tags. This allows clients
     * express a precondition that prevent the method from being applied if there have been any changes
     * to the resource.
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     * @param idempotencyKey This unique identifier can be used to allow for clients to safely retry
     * requests without accidentally performing the same operation twice. This is useful in cases such as
     * network failures. This Id should remain the same for a given operation, so that the server can use
     * it to recognise subsequent retries of the same request. Clients should be careful in using this
     * key as subsequent requests with the same key return the same result. How the key is generated is
     * up to the client, but it is suggested to use UUID (v4), or any other random string with enough
     * entropy to avoid collisions.
     * @param call The Ktor application call
     */
    public suspend fun putById(
        ifMatch: String,
        xFlowId: String?,
        idempotencyKey: String?,
        id: String,
        contributor: Contributor,
        call: ApplicationCall,
    )

    public companion object {
        /**
         * Mounts all routes for the Contributors resource
         *
         * - GET /contributors Page through all the Contributor resources matching the query filters
         * - POST /contributors Create a new Contributor
         * - GET /contributors/{id} Get a Contributor by ID
         * - PUT /contributors/{id} Update an existing Contributor
         */
        public fun Route.contributorsRoutes(controller: ContributorsController) {
            `get`("/contributors") {
                val xFlowId = call.request.headers["X-Flow-Id"]
                val limit = call.request.queryParameters.getTyped<kotlin.Int>("limit")
                val includeInactive =
                    call.request.queryParameters.getTyped<kotlin.Boolean>("include_inactive")
                val cursor = call.request.queryParameters.getTyped<kotlin.String>("cursor")
                controller.searchContributors(
                    xFlowId,
                    limit,
                    includeInactive,
                    cursor,
                    TypedApplicationCall(call),
                )
            }
            post("/contributors") {
                val xFlowId = call.request.headers["X-Flow-Id"]
                val idempotencyKey = call.request.headers["Idempotency-Key"]
                val contributor = call.receive<Contributor>()
                controller.createContributor(xFlowId, idempotencyKey, contributor, call)
            }
            `get`("/contributors/{id}") {
                val id = call.parameters.getTypedOrFail<kotlin.String>("id")
                val xFlowId = call.request.headers["X-Flow-Id"]
                val ifNoneMatch = call.request.headers["If-None-Match"]
                val status =
                    call.request.queryParameters.getTyped<examples.githubApi.models.StatusQueryParam>(
                        "status",
                        call.application.conversionService,
                    )
                controller.getContributor(xFlowId, ifNoneMatch, id, status, TypedApplicationCall(call))
            }
            put("/contributors/{id}") {
                val id = call.parameters.getTypedOrFail<kotlin.String>("id")
                val ifMatch = call.request.headers.getOrFail("If-Match")
                val xFlowId = call.request.headers["X-Flow-Id"]
                val idempotencyKey = call.request.headers["Idempotency-Key"]
                val contributor = call.receive<Contributor>()
                controller.putById(ifMatch, xFlowId, idempotencyKey, id, contributor, call)
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

public interface OrganisationsController {
    /**
     * Page through all the Organisation resources matching the query filters
     *
     * Route is expected to respond with [examples.githubApi.models.OrganisationQueryResult].
     * Use [examples.githubApi.controllers.TypedApplicationCall.respondTyped] to send the response.
     *
     * @param limit Upper bound for number of results to be returned from query api. A default limit
     * will be applied if this is not present
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     * @param includeInactive A query parameter to request both active and inactive entities
     * @param cursor An encoded value which represents a point in the data from where pagination will
     * commence. The pagination direction (either forward or backward) will also be encoded within the
     * cursor value. The cursor value will always be generated server side
     * @param call Decorated ApplicationCall with additional typed respond methods
     */
    public suspend fun `get`(
        xFlowId: String?,
        limit: Int?,
        includeInactive: Boolean?,
        cursor: String?,
        call: TypedApplicationCall<OrganisationQueryResult>,
    )

    /**
     * Create a new Organisation
     *
     * Route is expected to respond with status 201.
     * Use [respond] to send the response.
     *
     * @param organisation
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     * @param idempotencyKey This unique identifier can be used to allow for clients to safely retry
     * requests without accidentally performing the same operation twice. This is useful in cases such as
     * network failures. This Id should remain the same for a given operation, so that the server can use
     * it to recognise subsequent retries of the same request. Clients should be careful in using this
     * key as subsequent requests with the same key return the same result. How the key is generated is
     * up to the client, but it is suggested to use UUID (v4), or any other random string with enough
     * entropy to avoid collisions.
     * @param call The Ktor application call
     */
    public suspend fun post(
        xFlowId: String?,
        idempotencyKey: String?,
        organisation: Organisation,
        call: ApplicationCall,
    )

    /**
     * Get a Organisation by ID
     *
     * Route is expected to respond with [examples.githubApi.models.Organisation].
     * Use [examples.githubApi.controllers.TypedApplicationCall.respondTyped] to send the response.
     *
     * @param id The unique id for the resource
     * @param status Changes the behavior of GET | HEAD based on the value of the status property.
     * Will return a 404 if the status property does not match the query parameter."
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     * @param ifNoneMatch The RFC7232 If-None-Match header field in a request requires the server to
     * only operate on the resource if it does not match any of the provided entity-tags. If the provided
     * entity-tag is `*`, it is required that the resource does not exist at all.
     * @param call Decorated ApplicationCall with additional typed respond methods
     */
    public suspend fun getById(
        xFlowId: String?,
        ifNoneMatch: String?,
        id: String,
        status: StatusQueryParam?,
        call: TypedApplicationCall<Organisation>,
    )

    /**
     * Update an existing Organisation
     *
     * Route is expected to respond with status 204.
     * Use [respond] to send the response.
     *
     * @param organisation
     * @param id The unique id for the resource
     * @param ifMatch The RFC7232 If-Match header field in a request requires the server to only
     * operate on the resource that matches at least one of the provided entity-tags. This allows clients
     * express a precondition that prevent the method from being applied if there have been any changes
     * to the resource.
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     * @param idempotencyKey This unique identifier can be used to allow for clients to safely retry
     * requests without accidentally performing the same operation twice. This is useful in cases such as
     * network failures. This Id should remain the same for a given operation, so that the server can use
     * it to recognise subsequent retries of the same request. Clients should be careful in using this
     * key as subsequent requests with the same key return the same result. How the key is generated is
     * up to the client, but it is suggested to use UUID (v4), or any other random string with enough
     * entropy to avoid collisions.
     * @param call The Ktor application call
     */
    public suspend fun putById(
        ifMatch: String,
        xFlowId: String?,
        idempotencyKey: String?,
        id: String,
        organisation: Organisation,
        call: ApplicationCall,
    )

    public companion object {
        /**
         * Mounts all routes for the Organisations resource
         *
         * - GET /organisations Page through all the Organisation resources matching the query filters
         * - POST /organisations Create a new Organisation
         * - GET /organisations/{id} Get a Organisation by ID
         * - PUT /organisations/{id} Update an existing Organisation
         */
        public fun Route.organisationsRoutes(controller: OrganisationsController) {
            `get`("/organisations") {
                val xFlowId = call.request.headers["X-Flow-Id"]
                val limit = call.request.queryParameters.getTyped<kotlin.Int>("limit")
                val includeInactive =
                    call.request.queryParameters.getTyped<kotlin.Boolean>("include_inactive")
                val cursor = call.request.queryParameters.getTyped<kotlin.String>("cursor")
                controller.get(xFlowId, limit, includeInactive, cursor, TypedApplicationCall(call))
            }
            post("/organisations") {
                val xFlowId = call.request.headers["X-Flow-Id"]
                val idempotencyKey = call.request.headers["Idempotency-Key"]
                val organisation = call.receive<Organisation>()
                controller.post(xFlowId, idempotencyKey, organisation, call)
            }
            `get`("/organisations/{id}") {
                val id = call.parameters.getTypedOrFail<kotlin.String>("id")
                val xFlowId = call.request.headers["X-Flow-Id"]
                val ifNoneMatch = call.request.headers["If-None-Match"]
                val status =
                    call.request.queryParameters.getTyped<examples.githubApi.models.StatusQueryParam>(
                        "status",
                        call.application.conversionService,
                    )
                controller.getById(xFlowId, ifNoneMatch, id, status, TypedApplicationCall(call))
            }
            put("/organisations/{id}") {
                val id = call.parameters.getTypedOrFail<kotlin.String>("id")
                val ifMatch = call.request.headers.getOrFail("If-Match")
                val xFlowId = call.request.headers["X-Flow-Id"]
                val idempotencyKey = call.request.headers["Idempotency-Key"]
                val organisation = call.receive<Organisation>()
                controller.putById(ifMatch, xFlowId, idempotencyKey, id, organisation, call)
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

public interface OrganisationsContributorsController {
    /**
     * Page through all the Contributor resources for this parent Organisation matching the query
     * filters
     *
     * Route is expected to respond with [examples.githubApi.models.ContributorQueryResult].
     * Use [examples.githubApi.controllers.TypedApplicationCall.respondTyped] to send the response.
     *
     * @param parentId The unique id for the parent resource
     * @param limit Upper bound for number of results to be returned from query api. A default limit
     * will be applied if this is not present
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     * @param includeInactive A query parameter to request both active and inactive entities
     * @param cursor An encoded value which represents a point in the data from where pagination will
     * commence. The pagination direction (either forward or backward) will also be encoded within the
     * cursor value. The cursor value will always be generated server side
     * @param call Decorated ApplicationCall with additional typed respond methods
     */
    public suspend fun `get`(
        xFlowId: String?,
        parentId: String,
        limit: Int?,
        includeInactive: Boolean?,
        cursor: String?,
        call: TypedApplicationCall<ContributorQueryResult>,
    )

    /**
     * Get a Contributor for this Organisation by ID
     *
     * Route is expected to respond with [examples.githubApi.models.Contributor].
     * Use [examples.githubApi.controllers.TypedApplicationCall.respondTyped] to send the response.
     *
     * @param parentId The unique id for the parent resource
     * @param id The unique id for the resource
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     * @param ifNoneMatch The RFC7232 If-None-Match header field in a request requires the server to
     * only operate on the resource if it does not match any of the provided entity-tags. If the provided
     * entity-tag is `*`, it is required that the resource does not exist at all.
     * @param call Decorated ApplicationCall with additional typed respond methods
     */
    public suspend fun getById(
        xFlowId: String?,
        ifNoneMatch: String?,
        parentId: String,
        id: String,
        call: TypedApplicationCall<Contributor>,
    )

    /**
     * Add an existing Contributor to this Organisation
     *
     * Route is expected to respond with status 204.
     * Use [respond] to send the response.
     *
     * @param parentId The unique id for the parent resource
     * @param id The unique id for the resource
     * @param ifMatch The RFC7232 If-Match header field in a request requires the server to only
     * operate on the resource that matches at least one of the provided entity-tags. This allows clients
     * express a precondition that prevent the method from being applied if there have been any changes
     * to the resource.
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     * @param idempotencyKey This unique identifier can be used to allow for clients to safely retry
     * requests without accidentally performing the same operation twice. This is useful in cases such as
     * network failures. This Id should remain the same for a given operation, so that the server can use
     * it to recognise subsequent retries of the same request. Clients should be careful in using this
     * key as subsequent requests with the same key return the same result. How the key is generated is
     * up to the client, but it is suggested to use UUID (v4), or any other random string with enough
     * entropy to avoid collisions.
     * @param call The Ktor application call
     */
    public suspend fun putById(
        ifMatch: String,
        xFlowId: String?,
        idempotencyKey: String?,
        parentId: String,
        id: String,
        call: ApplicationCall,
    )

    /**
     * Remove Contributor from this Organisation. Does not delete the underlying Contributor.
     *
     * Route is expected to respond with status 200.
     * Use [respond] to send the response.
     *
     * @param parentId The unique id for the parent resource
     * @param id The unique id for the resource
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     * @param call The Ktor application call
     */
    public suspend fun deleteById(
        xFlowId: String?,
        parentId: String,
        id: String,
        call: ApplicationCall,
    )

    public companion object {
        /**
         * Mounts all routes for the OrganisationsContributors resource
         *
         * - GET /organisations/{parent-id}/contributors Page through all the Contributor resources for
         * this parent Organisation matching the query filters
         * - GET /organisations/{parent-id}/contributors/{id} Get a Contributor for this Organisation by
         * ID
         * - PUT /organisations/{parent-id}/contributors/{id} Add an existing Contributor to this
         * Organisation
         * - DELETE /organisations/{parent-id}/contributors/{id} Remove Contributor from this
         * Organisation. Does not delete the underlying Contributor.
         */
        public fun Route.organisationsContributorsRoutes(controller: OrganisationsContributorsController) {
            `get`("/organisations/{parent-id}/contributors") {
                val parentId = call.parameters.getTypedOrFail<kotlin.String>("parent-id")
                val xFlowId = call.request.headers["X-Flow-Id"]
                val limit = call.request.queryParameters.getTyped<kotlin.Int>("limit")
                val includeInactive =
                    call.request.queryParameters.getTyped<kotlin.Boolean>("include_inactive")
                val cursor = call.request.queryParameters.getTyped<kotlin.String>("cursor")
                controller.get(
                    xFlowId,
                    parentId,
                    limit,
                    includeInactive,
                    cursor,
                    TypedApplicationCall(call),
                )
            }
            `get`("/organisations/{parent-id}/contributors/{id}") {
                val parentId = call.parameters.getTypedOrFail<kotlin.String>("parent-id")
                val id = call.parameters.getTypedOrFail<kotlin.String>("id")
                val xFlowId = call.request.headers["X-Flow-Id"]
                val ifNoneMatch = call.request.headers["If-None-Match"]
                controller.getById(xFlowId, ifNoneMatch, parentId, id, TypedApplicationCall(call))
            }
            put("/organisations/{parent-id}/contributors/{id}") {
                val parentId = call.parameters.getTypedOrFail<kotlin.String>("parent-id")
                val id = call.parameters.getTypedOrFail<kotlin.String>("id")
                val ifMatch = call.request.headers.getOrFail("If-Match")
                val xFlowId = call.request.headers["X-Flow-Id"]
                val idempotencyKey = call.request.headers["Idempotency-Key"]
                controller.putById(ifMatch, xFlowId, idempotencyKey, parentId, id, call)
            }
            delete("/organisations/{parent-id}/contributors/{id}") {
                val parentId = call.parameters.getTypedOrFail<kotlin.String>("parent-id")
                val id = call.parameters.getTypedOrFail<kotlin.String>("id")
                val xFlowId = call.request.headers["X-Flow-Id"]
                controller.deleteById(xFlowId, parentId, id, call)
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

public interface RepositoriesController {
    /**
     * Page through all the Repository resources matching the query filters
     *
     * Route is expected to respond with [examples.githubApi.models.RepositoryQueryResult].
     * Use [examples.githubApi.controllers.TypedApplicationCall.respondTyped] to send the response.
     *
     * @param limit Upper bound for number of results to be returned from query api. A default limit
     * will be applied if this is not present
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     * @param slug Filters resources by the [slug] property. Accepts comma-delimited values
     * @param name Filters resources by the [name] property. Accepts comma-delimited values
     * @param includeInactive A query parameter to request both active and inactive entities
     * @param cursor An encoded value which represents a point in the data from where pagination will
     * commence. The pagination direction (either forward or backward) will also be encoded within the
     * cursor value. The cursor value will always be generated server side
     * @param call Decorated ApplicationCall with additional typed respond methods
     */
    public suspend fun `get`(
        xFlowId: String?,
        limit: Int?,
        slug: List<String>?,
        name: List<String>?,
        includeInactive: Boolean?,
        cursor: String?,
        call: TypedApplicationCall<RepositoryQueryResult>,
    )

    /**
     * Create a new Repository
     *
     * Route is expected to respond with status 201.
     * Use [respond] to send the response.
     *
     * @param repository
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     * @param idempotencyKey This unique identifier can be used to allow for clients to safely retry
     * requests without accidentally performing the same operation twice. This is useful in cases such as
     * network failures. This Id should remain the same for a given operation, so that the server can use
     * it to recognise subsequent retries of the same request. Clients should be careful in using this
     * key as subsequent requests with the same key return the same result. How the key is generated is
     * up to the client, but it is suggested to use UUID (v4), or any other random string with enough
     * entropy to avoid collisions.
     * @param call The Ktor application call
     */
    public suspend fun post(
        xFlowId: String?,
        idempotencyKey: String?,
        repository: Repository,
        call: ApplicationCall,
    )

    /**
     * Get a Repository by ID
     *
     * Route is expected to respond with [examples.githubApi.models.Repository].
     * Use [examples.githubApi.controllers.TypedApplicationCall.respondTyped] to send the response.
     *
     * @param id The unique id for the resource
     * @param status Changes the behavior of GET | HEAD based on the value of the status property.
     * Will return a 404 if the status property does not match the query parameter."
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     * @param ifNoneMatch The RFC7232 If-None-Match header field in a request requires the server to
     * only operate on the resource if it does not match any of the provided entity-tags. If the provided
     * entity-tag is `*`, it is required that the resource does not exist at all.
     * @param call Decorated ApplicationCall with additional typed respond methods
     */
    public suspend fun getById(
        xFlowId: String?,
        ifNoneMatch: String?,
        id: String,
        status: StatusQueryParam?,
        call: TypedApplicationCall<Repository>,
    )

    /**
     * Update an existing Repository
     *
     * Route is expected to respond with status 204.
     * Use [respond] to send the response.
     *
     * @param repository
     * @param id The unique id for the resource
     * @param ifMatch The RFC7232 If-Match header field in a request requires the server to only
     * operate on the resource that matches at least one of the provided entity-tags. This allows clients
     * express a precondition that prevent the method from being applied if there have been any changes
     * to the resource.
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     * @param idempotencyKey This unique identifier can be used to allow for clients to safely retry
     * requests without accidentally performing the same operation twice. This is useful in cases such as
     * network failures. This Id should remain the same for a given operation, so that the server can use
     * it to recognise subsequent retries of the same request. Clients should be careful in using this
     * key as subsequent requests with the same key return the same result. How the key is generated is
     * up to the client, but it is suggested to use UUID (v4), or any other random string with enough
     * entropy to avoid collisions.
     * @param call The Ktor application call
     */
    public suspend fun putById(
        ifMatch: String,
        xFlowId: String?,
        idempotencyKey: String?,
        id: String,
        repository: Repository,
        call: ApplicationCall,
    )

    public companion object {
        /**
         * Mounts all routes for the Repositories resource
         *
         * - GET /repositories Page through all the Repository resources matching the query filters
         * - POST /repositories Create a new Repository
         * - GET /repositories/{id} Get a Repository by ID
         * - PUT /repositories/{id} Update an existing Repository
         */
        public fun Route.repositoriesRoutes(controller: RepositoriesController) {
            `get`("/repositories") {
                val xFlowId = call.request.headers["X-Flow-Id"]
                val limit = call.request.queryParameters.getTyped<kotlin.Int>("limit")
                val slug =
                    call.request.queryParameters.getTyped<kotlin.collections.List<kotlin.String>>("slug")
                val name =
                    call.request.queryParameters.getTyped<kotlin.collections.List<kotlin.String>>("name")
                val includeInactive =
                    call.request.queryParameters.getTyped<kotlin.Boolean>("include_inactive")
                val cursor = call.request.queryParameters.getTyped<kotlin.String>("cursor")
                controller.get(
                    xFlowId,
                    limit,
                    slug,
                    name,
                    includeInactive,
                    cursor,
                    TypedApplicationCall(call),
                )
            }
            post("/repositories") {
                val xFlowId = call.request.headers["X-Flow-Id"]
                val idempotencyKey = call.request.headers["Idempotency-Key"]
                val repository = call.receive<Repository>()
                controller.post(xFlowId, idempotencyKey, repository, call)
            }
            `get`("/repositories/{id}") {
                val id = call.parameters.getTypedOrFail<kotlin.String>("id")
                val xFlowId = call.request.headers["X-Flow-Id"]
                val ifNoneMatch = call.request.headers["If-None-Match"]
                val status =
                    call.request.queryParameters.getTyped<examples.githubApi.models.StatusQueryParam>(
                        "status",
                        call.application.conversionService,
                    )
                controller.getById(xFlowId, ifNoneMatch, id, status, TypedApplicationCall(call))
            }
            put("/repositories/{id}") {
                val id = call.parameters.getTypedOrFail<kotlin.String>("id")
                val ifMatch = call.request.headers.getOrFail("If-Match")
                val xFlowId = call.request.headers["X-Flow-Id"]
                val idempotencyKey = call.request.headers["Idempotency-Key"]
                val repository = call.receive<Repository>()
                controller.putById(ifMatch, xFlowId, idempotencyKey, id, repository, call)
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

public interface RepositoriesPullRequestsController {
    /**
     * Page through all the PullRequest resources for this parent Repository matching the query
     * filters
     *
     * Route is expected to respond with [examples.githubApi.models.PullRequestQueryResult].
     * Use [examples.githubApi.controllers.TypedApplicationCall.respondTyped] to send the response.
     *
     * @param parentId The unique id for the parent resource
     * @param limit Upper bound for number of results to be returned from query api. A default limit
     * will be applied if this is not present
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     * @param includeInactive A query parameter to request both active and inactive entities
     * @param cursor An encoded value which represents a point in the data from where pagination will
     * commence. The pagination direction (either forward or backward) will also be encoded within the
     * cursor value. The cursor value will always be generated server side
     * @param call Decorated ApplicationCall with additional typed respond methods
     */
    public suspend fun `get`(
        xFlowId: String?,
        parentId: String,
        limit: Int?,
        includeInactive: Boolean?,
        cursor: String?,
        call: TypedApplicationCall<PullRequestQueryResult>,
    )

    /**
     * Create a new PullRequest for this parent Repository
     *
     * Route is expected to respond with status 201.
     * Use [respond] to send the response.
     *
     * @param pullRequest
     * @param parentId The unique id for the parent resource
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     * @param idempotencyKey This unique identifier can be used to allow for clients to safely retry
     * requests without accidentally performing the same operation twice. This is useful in cases such as
     * network failures. This Id should remain the same for a given operation, so that the server can use
     * it to recognise subsequent retries of the same request. Clients should be careful in using this
     * key as subsequent requests with the same key return the same result. How the key is generated is
     * up to the client, but it is suggested to use UUID (v4), or any other random string with enough
     * entropy to avoid collisions.
     * @param call The Ktor application call
     */
    public suspend fun post(
        xFlowId: String?,
        idempotencyKey: String?,
        parentId: String,
        pullRequest: PullRequest,
        call: ApplicationCall,
    )

    /**
     * Get a PullRequest for this Repository by ID
     *
     * Route is expected to respond with [examples.githubApi.models.PullRequest].
     * Use [examples.githubApi.controllers.TypedApplicationCall.respondTyped] to send the response.
     *
     * @param parentId The unique id for the parent resource
     * @param id The unique id for the resource
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     * @param ifNoneMatch The RFC7232 If-None-Match header field in a request requires the server to
     * only operate on the resource if it does not match any of the provided entity-tags. If the provided
     * entity-tag is `*`, it is required that the resource does not exist at all.
     * @param call Decorated ApplicationCall with additional typed respond methods
     */
    public suspend fun getById(
        xFlowId: String?,
        ifNoneMatch: String?,
        parentId: String,
        id: String,
        call: TypedApplicationCall<PullRequest>,
    )

    /**
     * Update the PullRequest owned by this Repository
     *
     * Route is expected to respond with status 204.
     * Use [respond] to send the response.
     *
     * @param pullRequest
     * @param parentId The unique id for the parent resource
     * @param id The unique id for the resource
     * @param ifMatch The RFC7232 If-Match header field in a request requires the server to only
     * operate on the resource that matches at least one of the provided entity-tags. This allows clients
     * express a precondition that prevent the method from being applied if there have been any changes
     * to the resource.
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     * @param idempotencyKey This unique identifier can be used to allow for clients to safely retry
     * requests without accidentally performing the same operation twice. This is useful in cases such as
     * network failures. This Id should remain the same for a given operation, so that the server can use
     * it to recognise subsequent retries of the same request. Clients should be careful in using this
     * key as subsequent requests with the same key return the same result. How the key is generated is
     * up to the client, but it is suggested to use UUID (v4), or any other random string with enough
     * entropy to avoid collisions.
     * @param call The Ktor application call
     */
    public suspend fun putById(
        ifMatch: String,
        xFlowId: String?,
        idempotencyKey: String?,
        parentId: String,
        id: String,
        pullRequest: PullRequest,
        call: ApplicationCall,
    )

    public companion object {
        /**
         * Mounts all routes for the RepositoriesPullRequests resource
         *
         * - GET /repositories/{parent-id}/pull-requests Page through all the PullRequest resources for
         * this parent Repository matching the query filters
         * - POST /repositories/{parent-id}/pull-requests Create a new PullRequest for this parent
         * Repository
         * - GET /repositories/{parent-id}/pull-requests/{id} Get a PullRequest for this Repository by
         * ID
         * - PUT /repositories/{parent-id}/pull-requests/{id} Update the PullRequest owned by this
         * Repository
         */
        public fun Route.repositoriesPullRequestsRoutes(controller: RepositoriesPullRequestsController) {
            `get`("/repositories/{parent-id}/pull-requests") {
                val parentId = call.parameters.getTypedOrFail<kotlin.String>("parent-id")
                val xFlowId = call.request.headers["X-Flow-Id"]
                val limit = call.request.queryParameters.getTyped<kotlin.Int>("limit")
                val includeInactive =
                    call.request.queryParameters.getTyped<kotlin.Boolean>("include_inactive")
                val cursor = call.request.queryParameters.getTyped<kotlin.String>("cursor")
                controller.get(
                    xFlowId,
                    parentId,
                    limit,
                    includeInactive,
                    cursor,
                    TypedApplicationCall(call),
                )
            }
            post("/repositories/{parent-id}/pull-requests") {
                val parentId = call.parameters.getTypedOrFail<kotlin.String>("parent-id")
                val xFlowId = call.request.headers["X-Flow-Id"]
                val idempotencyKey = call.request.headers["Idempotency-Key"]
                val pullRequest = call.receive<PullRequest>()
                controller.post(xFlowId, idempotencyKey, parentId, pullRequest, call)
            }
            `get`("/repositories/{parent-id}/pull-requests/{id}") {
                val parentId = call.parameters.getTypedOrFail<kotlin.String>("parent-id")
                val id = call.parameters.getTypedOrFail<kotlin.String>("id")
                val xFlowId = call.request.headers["X-Flow-Id"]
                val ifNoneMatch = call.request.headers["If-None-Match"]
                controller.getById(xFlowId, ifNoneMatch, parentId, id, TypedApplicationCall(call))
            }
            put("/repositories/{parent-id}/pull-requests/{id}") {
                val parentId = call.parameters.getTypedOrFail<kotlin.String>("parent-id")
                val id = call.parameters.getTypedOrFail<kotlin.String>("id")
                val ifMatch = call.request.headers.getOrFail("If-Match")
                val xFlowId = call.request.headers["X-Flow-Id"]
                val idempotencyKey = call.request.headers["Idempotency-Key"]
                val pullRequest = call.receive<PullRequest>()
                controller.putById(ifMatch, xFlowId, idempotencyKey, parentId, id, pullRequest, call)
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
