package examples.githubApi.controllers

import examples.githubApi.models.BulkEntityDetails
import examples.githubApi.models.Contributor
import examples.githubApi.models.ContributorQueryResult
import examples.githubApi.models.EventResults
import examples.githubApi.models.Organisation
import examples.githubApi.models.OrganisationQueryResult
import examples.githubApi.models.PullRequest
import examples.githubApi.models.PullRequestQueryResult
import examples.githubApi.models.Repository
import examples.githubApi.models.RepositoryQueryResult
import examples.githubApi.models.StatusQueryParam
import io.micronaut.http.HttpResponse
import io.micronaut.http.`annotation`.Body
import io.micronaut.http.`annotation`.Consumes
import io.micronaut.http.`annotation`.Controller
import io.micronaut.http.`annotation`.Delete
import io.micronaut.http.`annotation`.Get
import io.micronaut.http.`annotation`.Header
import io.micronaut.http.`annotation`.PathVariable
import io.micronaut.http.`annotation`.Post
import io.micronaut.http.`annotation`.Produces
import io.micronaut.http.`annotation`.Put
import io.micronaut.http.`annotation`.QueryValue
import io.micronaut.security.rules.SecurityRule
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Unit
import kotlin.collections.List

@Controller
public interface InternalEventsController {
    /**
     * Generate change events for a list of entities
     *
     * @param bulkEntityDetails
     */
    @Post(uri = "/internal/events")
    @Consumes(value = ["application/json"])
    @Produces(value = ["application/json", "application/problem+json"])
    public fun post(
        @Body @Valid bulkEntityDetails: BulkEntityDetails,
    ): HttpResponse<EventResults>
}

@Controller
public interface ContributorsController {
    /**
     * Page through all the Contributor resources matching the query filters
     *
     * @param limit Upper bound for number of results to be returned from query api. A default limit
     * will be applied if this is not present
     *
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     *
     * @param includeInactive A query parameter to request both active and inactive entities
     * @param cursor An encoded value which represents a point in the data from where pagination will
     * commence. The pagination direction (either forward or backward) will also be encoded within the
     * cursor value. The cursor value will always be generated server side
     *
     */
    @Get(uri = "/contributors")
    @Produces(value = ["application/json"])
    public fun searchContributors(
        @Min(1) @Max(100) @QueryValue(value = "limit", defaultValue = "10") limit: Int,
        @Header(value = "X-Flow-Id") xFlowId: String?,
        @QueryValue(value = "include_inactive") includeInactive: Boolean?,
        @QueryValue(value = "cursor") cursor: String?,
    ): HttpResponse<ContributorQueryResult>

    /**
     * Create a new Contributor
     *
     * @param contributor
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     *
     * @param idempotencyKey This unique identifier can be used to allow for clients to safely retry
     * requests without accidentally performing the same operation twice. This is useful in cases such as
     * network failures. This Id should remain the same for a given operation, so that the server can use
     * it to recognise subsequent retries of the same request. Clients should be careful in using this
     * key as subsequent requests with the same key return the same result. How the key is generated is
     * up to the client, but it is suggested to use UUID (v4), or any other random string with enough
     * entropy to avoid collisions.
     *
     */
    @Post(uri = "/contributors")
    @Consumes(value = ["application/json"])
    public fun createContributor(
        @Body @Valid contributor: Contributor,
        @Header(value = "X-Flow-Id") xFlowId: String?,
        @Header(value = "Idempotency-Key") idempotencyKey: UUID?,
    ): HttpResponse<Unit>

    /**
     * Get a Contributor by ID
     *
     * @param id The unique id for the resource
     * @param status Changes the behavior of GET | HEAD based on the value of the status property.
     * Will return a 404 if the status property does not match the query parameter."
     *
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     *
     * @param ifNoneMatch The RFC7232 If-None-Match header field in a request requires the server to
     * only operate on the resource if it does not match any of the provided entity-tags. If the provided
     * entity-tag is `*`, it is required that the resource does not exist at all.
     *
     */
    @Get(uri = "/contributors/{id}")
    @Produces(value = ["application/json"])
    public fun getContributor(
        @PathVariable(value = "id") id: String,
        @QueryValue(value = "status", defaultValue = "all") status: StatusQueryParam,
        @Header(value = "X-Flow-Id") xFlowId: String?,
        @Header(value = "If-None-Match") ifNoneMatch: String?,
    ): HttpResponse<Contributor>

    /**
     * Update an existing Contributor
     *
     * @param contributor
     * @param id The unique id for the resource
     * @param ifMatch The RFC7232 If-Match header field in a request requires the server to only
     * operate on the resource that matches at least one of the provided entity-tags. This allows clients
     * express a precondition that prevent the method from being applied if there have been any changes
     * to the resource.
     *
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     *
     * @param idempotencyKey This unique identifier can be used to allow for clients to safely retry
     * requests without accidentally performing the same operation twice. This is useful in cases such as
     * network failures. This Id should remain the same for a given operation, so that the server can use
     * it to recognise subsequent retries of the same request. Clients should be careful in using this
     * key as subsequent requests with the same key return the same result. How the key is generated is
     * up to the client, but it is suggested to use UUID (v4), or any other random string with enough
     * entropy to avoid collisions.
     *
     */
    @Put(uri = "/contributors/{id}")
    @Consumes(value = ["application/json"])
    public fun putById(
        @Body @Valid contributor: Contributor,
        @PathVariable(value = "id") id: String,
        @Header(value = "If-Match") ifMatch: String,
        @Header(value = "X-Flow-Id") xFlowId: String?,
        @Header(value = "Idempotency-Key") idempotencyKey: UUID?,
    ): HttpResponse<Unit>
}

@Controller
public interface OrganisationsController {
    /**
     * Page through all the Organisation resources matching the query filters
     *
     * @param limit Upper bound for number of results to be returned from query api. A default limit
     * will be applied if this is not present
     *
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     *
     * @param includeInactive A query parameter to request both active and inactive entities
     * @param cursor An encoded value which represents a point in the data from where pagination will
     * commence. The pagination direction (either forward or backward) will also be encoded within the
     * cursor value. The cursor value will always be generated server side
     *
     */
    @Get(uri = "/organisations")
    @Produces(value = ["application/json"])
    public fun `get`(
        @Min(1) @Max(100) @QueryValue(value = "limit", defaultValue = "10") limit: Int,
        @Header(value = "X-Flow-Id") xFlowId: String?,
        @QueryValue(value = "include_inactive") includeInactive: Boolean?,
        @QueryValue(value = "cursor") cursor: String?,
    ): HttpResponse<OrganisationQueryResult>

    /**
     * Create a new Organisation
     *
     * @param organisation
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     *
     * @param idempotencyKey This unique identifier can be used to allow for clients to safely retry
     * requests without accidentally performing the same operation twice. This is useful in cases such as
     * network failures. This Id should remain the same for a given operation, so that the server can use
     * it to recognise subsequent retries of the same request. Clients should be careful in using this
     * key as subsequent requests with the same key return the same result. How the key is generated is
     * up to the client, but it is suggested to use UUID (v4), or any other random string with enough
     * entropy to avoid collisions.
     *
     */
    @Post(uri = "/organisations")
    @Consumes(value = ["application/json"])
    public fun post(
        @Body @Valid organisation: Organisation,
        @Header(value = "X-Flow-Id") xFlowId: String?,
        @Header(value = "Idempotency-Key") idempotencyKey: UUID?,
    ): HttpResponse<Unit>

    /**
     * Get a Organisation by ID
     *
     * @param id The unique id for the resource
     * @param status Changes the behavior of GET | HEAD based on the value of the status property.
     * Will return a 404 if the status property does not match the query parameter."
     *
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     *
     * @param ifNoneMatch The RFC7232 If-None-Match header field in a request requires the server to
     * only operate on the resource if it does not match any of the provided entity-tags. If the provided
     * entity-tag is `*`, it is required that the resource does not exist at all.
     *
     */
    @Get(uri = "/organisations/{id}")
    @Produces(value = ["application/json"])
    public fun getById(
        @PathVariable(value = "id") id: String,
        @QueryValue(value = "status", defaultValue = "all") status: StatusQueryParam,
        @Header(value = "X-Flow-Id") xFlowId: String?,
        @Header(value = "If-None-Match") ifNoneMatch: String?,
    ): HttpResponse<Organisation>

    /**
     * Update an existing Organisation
     *
     * @param organisation
     * @param id The unique id for the resource
     * @param ifMatch The RFC7232 If-Match header field in a request requires the server to only
     * operate on the resource that matches at least one of the provided entity-tags. This allows clients
     * express a precondition that prevent the method from being applied if there have been any changes
     * to the resource.
     *
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     *
     * @param idempotencyKey This unique identifier can be used to allow for clients to safely retry
     * requests without accidentally performing the same operation twice. This is useful in cases such as
     * network failures. This Id should remain the same for a given operation, so that the server can use
     * it to recognise subsequent retries of the same request. Clients should be careful in using this
     * key as subsequent requests with the same key return the same result. How the key is generated is
     * up to the client, but it is suggested to use UUID (v4), or any other random string with enough
     * entropy to avoid collisions.
     *
     */
    @Put(uri = "/organisations/{id}")
    @Consumes(value = ["application/json"])
    public fun putById(
        @Body @Valid organisation: Organisation,
        @PathVariable(value = "id") id: String,
        @Header(value = "If-Match") ifMatch: String,
        @Header(value = "X-Flow-Id") xFlowId: String?,
        @Header(value = "Idempotency-Key") idempotencyKey: UUID?,
    ): HttpResponse<Unit>
}

@Controller
public interface OrganisationsContributorsController {
    /**
     * Page through all the Contributor resources for this parent Organisation matching the query
     * filters
     *
     * @param parentId The unique id for the parent resource
     * @param limit Upper bound for number of results to be returned from query api. A default limit
     * will be applied if this is not present
     *
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     *
     * @param includeInactive A query parameter to request both active and inactive entities
     * @param cursor An encoded value which represents a point in the data from where pagination will
     * commence. The pagination direction (either forward or backward) will also be encoded within the
     * cursor value. The cursor value will always be generated server side
     *
     */
    @Get(uri = "/organisations/{parent-id}/contributors")
    @Produces(value = ["application/json"])
    public fun `get`(
        @PathVariable(value = "parent-id") parentId: String,
        @Min(1) @Max(100) @QueryValue(value = "limit", defaultValue = "10") limit: Int,
        @Header(value = "X-Flow-Id") xFlowId: String?,
        @QueryValue(value = "include_inactive") includeInactive: Boolean?,
        @QueryValue(value = "cursor") cursor: String?,
    ): HttpResponse<ContributorQueryResult>

    /**
     * Get a Contributor for this Organisation by ID
     *
     * @param parentId The unique id for the parent resource
     * @param id The unique id for the resource
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     *
     * @param ifNoneMatch The RFC7232 If-None-Match header field in a request requires the server to
     * only operate on the resource if it does not match any of the provided entity-tags. If the provided
     * entity-tag is `*`, it is required that the resource does not exist at all.
     *
     */
    @Get(uri = "/organisations/{parent-id}/contributors/{id}")
    @Produces(value = ["application/json"])
    public fun getById(
        @PathVariable(value = "parent-id") parentId: String,
        @PathVariable(value = "id") id: String,
        @Header(value = "X-Flow-Id") xFlowId: String?,
        @Header(value = "If-None-Match") ifNoneMatch: String?,
    ): HttpResponse<Contributor>

    /**
     * Add an existing Contributor to this Organisation
     *
     * @param parentId The unique id for the parent resource
     * @param id The unique id for the resource
     * @param ifMatch The RFC7232 If-Match header field in a request requires the server to only
     * operate on the resource that matches at least one of the provided entity-tags. This allows clients
     * express a precondition that prevent the method from being applied if there have been any changes
     * to the resource.
     *
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     *
     * @param idempotencyKey This unique identifier can be used to allow for clients to safely retry
     * requests without accidentally performing the same operation twice. This is useful in cases such as
     * network failures. This Id should remain the same for a given operation, so that the server can use
     * it to recognise subsequent retries of the same request. Clients should be careful in using this
     * key as subsequent requests with the same key return the same result. How the key is generated is
     * up to the client, but it is suggested to use UUID (v4), or any other random string with enough
     * entropy to avoid collisions.
     *
     */
    @Put(uri = "/organisations/{parent-id}/contributors/{id}")
    public fun putById(
        @PathVariable(value = "parent-id") parentId: String,
        @PathVariable(value = "id") id: String,
        @Header(value = "If-Match") ifMatch: String,
        @Header(value = "X-Flow-Id") xFlowId: String?,
        @Header(value = "Idempotency-Key") idempotencyKey: UUID?,
    ): HttpResponse<Unit>

    /**
     * Remove Contributor from this Organisation. Does not delete the underlying Contributor.
     *
     * @param parentId The unique id for the parent resource
     * @param id The unique id for the resource
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     *
     */
    @Delete(uri = "/organisations/{parent-id}/contributors/{id}")
    public fun deleteById(
        @PathVariable(value = "parent-id") parentId: String,
        @PathVariable(value = "id") id: String,
        @Header(value = "X-Flow-Id") xFlowId: String?,
    ): HttpResponse<Unit>
}

@Controller
public interface RepositoriesController {
    /**
     * Page through all the Repository resources matching the query filters
     *
     * @param limit Upper bound for number of results to be returned from query api. A default limit
     * will be applied if this is not present
     *
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     *
     * @param slug Filters resources by the [slug] property. Accepts comma-delimited values
     *
     * @param name Filters resources by the [name] property. Accepts comma-delimited values
     *
     * @param includeInactive A query parameter to request both active and inactive entities
     * @param cursor An encoded value which represents a point in the data from where pagination will
     * commence. The pagination direction (either forward or backward) will also be encoded within the
     * cursor value. The cursor value will always be generated server side
     *
     */
    @Get(uri = "/repositories")
    @Produces(value = ["application/json"])
    public fun `get`(
        @Min(1) @Max(100) @QueryValue(value = "limit", defaultValue = "10") limit: Int,
        @Header(value = "X-Flow-Id") xFlowId: String?,
        @Valid @QueryValue(value = "slug") slug: List<String>?,
        @Valid @QueryValue(value = "name") name: List<String>?,
        @QueryValue(value = "include_inactive") includeInactive: Boolean?,
        @QueryValue(value = "cursor") cursor: String?,
    ): HttpResponse<RepositoryQueryResult>

    /**
     * Create a new Repository
     *
     * @param repository
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     *
     * @param idempotencyKey This unique identifier can be used to allow for clients to safely retry
     * requests without accidentally performing the same operation twice. This is useful in cases such as
     * network failures. This Id should remain the same for a given operation, so that the server can use
     * it to recognise subsequent retries of the same request. Clients should be careful in using this
     * key as subsequent requests with the same key return the same result. How the key is generated is
     * up to the client, but it is suggested to use UUID (v4), or any other random string with enough
     * entropy to avoid collisions.
     *
     */
    @Post(uri = "/repositories")
    @Consumes(value = ["application/json"])
    public fun post(
        @Body @Valid repository: Repository,
        @Header(value = "X-Flow-Id") xFlowId: String?,
        @Header(value = "Idempotency-Key") idempotencyKey: UUID?,
    ): HttpResponse<Unit>

    /**
     * Get a Repository by ID
     *
     * @param id The unique id for the resource
     * @param status Changes the behavior of GET | HEAD based on the value of the status property.
     * Will return a 404 if the status property does not match the query parameter."
     *
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     *
     * @param ifNoneMatch The RFC7232 If-None-Match header field in a request requires the server to
     * only operate on the resource if it does not match any of the provided entity-tags. If the provided
     * entity-tag is `*`, it is required that the resource does not exist at all.
     *
     */
    @Get(uri = "/repositories/{id}")
    @Produces(value = ["application/json"])
    public fun getById(
        @PathVariable(value = "id") id: String,
        @QueryValue(value = "status", defaultValue = "all") status: StatusQueryParam,
        @Header(value = "X-Flow-Id") xFlowId: String?,
        @Header(value = "If-None-Match") ifNoneMatch: String?,
    ): HttpResponse<Repository>

    /**
     * Update an existing Repository
     *
     * @param repository
     * @param id The unique id for the resource
     * @param ifMatch The RFC7232 If-Match header field in a request requires the server to only
     * operate on the resource that matches at least one of the provided entity-tags. This allows clients
     * express a precondition that prevent the method from being applied if there have been any changes
     * to the resource.
     *
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     *
     * @param idempotencyKey This unique identifier can be used to allow for clients to safely retry
     * requests without accidentally performing the same operation twice. This is useful in cases such as
     * network failures. This Id should remain the same for a given operation, so that the server can use
     * it to recognise subsequent retries of the same request. Clients should be careful in using this
     * key as subsequent requests with the same key return the same result. How the key is generated is
     * up to the client, but it is suggested to use UUID (v4), or any other random string with enough
     * entropy to avoid collisions.
     *
     */
    @Put(uri = "/repositories/{id}")
    @Consumes(value = ["application/json"])
    public fun putById(
        @Body @Valid repository: Repository,
        @PathVariable(value = "id") id: String,
        @Header(value = "If-Match") ifMatch: String,
        @Header(value = "X-Flow-Id") xFlowId: String?,
        @Header(value = "Idempotency-Key") idempotencyKey: UUID?,
    ): HttpResponse<Unit>
}

@Controller
public interface RepositoriesPullRequestsController {
    /**
     * Page through all the PullRequest resources for this parent Repository matching the query
     * filters
     *
     * @param parentId The unique id for the parent resource
     * @param limit Upper bound for number of results to be returned from query api. A default limit
     * will be applied if this is not present
     *
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     *
     * @param includeInactive A query parameter to request both active and inactive entities
     * @param cursor An encoded value which represents a point in the data from where pagination will
     * commence. The pagination direction (either forward or backward) will also be encoded within the
     * cursor value. The cursor value will always be generated server side
     *
     */
    @Get(uri = "/repositories/{parent-id}/pull-requests")
    @Produces(value = ["application/json"])
    public fun `get`(
        @PathVariable(value = "parent-id") parentId: String,
        @Min(1) @Max(100) @QueryValue(value = "limit", defaultValue = "10") limit: Int,
        @Header(value = "X-Flow-Id") xFlowId: String?,
        @QueryValue(value = "include_inactive") includeInactive: Boolean?,
        @QueryValue(value = "cursor") cursor: String?,
    ): HttpResponse<PullRequestQueryResult>

    /**
     * Create a new PullRequest for this parent Repository
     *
     * @param pullRequest
     * @param parentId The unique id for the parent resource
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     *
     * @param idempotencyKey This unique identifier can be used to allow for clients to safely retry
     * requests without accidentally performing the same operation twice. This is useful in cases such as
     * network failures. This Id should remain the same for a given operation, so that the server can use
     * it to recognise subsequent retries of the same request. Clients should be careful in using this
     * key as subsequent requests with the same key return the same result. How the key is generated is
     * up to the client, but it is suggested to use UUID (v4), or any other random string with enough
     * entropy to avoid collisions.
     *
     */
    @Post(uri = "/repositories/{parent-id}/pull-requests")
    @Consumes(value = ["application/json"])
    public fun post(
        @Body @Valid pullRequest: PullRequest,
        @PathVariable(value = "parent-id") parentId: String,
        @Header(value = "X-Flow-Id") xFlowId: String?,
        @Header(value = "Idempotency-Key") idempotencyKey: UUID?,
    ): HttpResponse<Unit>

    /**
     * Get a PullRequest for this Repository by ID
     *
     * @param parentId The unique id for the parent resource
     * @param id The unique id for the resource
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     *
     * @param ifNoneMatch The RFC7232 If-None-Match header field in a request requires the server to
     * only operate on the resource if it does not match any of the provided entity-tags. If the provided
     * entity-tag is `*`, it is required that the resource does not exist at all.
     *
     */
    @Get(uri = "/repositories/{parent-id}/pull-requests/{id}")
    @Produces(value = ["application/json"])
    public fun getById(
        @PathVariable(value = "parent-id") parentId: String,
        @PathVariable(value = "id") id: String,
        @Header(value = "X-Flow-Id") xFlowId: String?,
        @Header(value = "If-None-Match") ifNoneMatch: String?,
    ): HttpResponse<PullRequest>

    /**
     * Update the PullRequest owned by this Repository
     *
     * @param pullRequest
     * @param parentId The unique id for the parent resource
     * @param id The unique id for the resource
     * @param ifMatch The RFC7232 If-Match header field in a request requires the server to only
     * operate on the resource that matches at least one of the provided entity-tags. This allows clients
     * express a precondition that prevent the method from being applied if there have been any changes
     * to the resource.
     *
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     *
     * @param idempotencyKey This unique identifier can be used to allow for clients to safely retry
     * requests without accidentally performing the same operation twice. This is useful in cases such as
     * network failures. This Id should remain the same for a given operation, so that the server can use
     * it to recognise subsequent retries of the same request. Clients should be careful in using this
     * key as subsequent requests with the same key return the same result. How the key is generated is
     * up to the client, but it is suggested to use UUID (v4), or any other random string with enough
     * entropy to avoid collisions.
     *
     */
    @Put(uri = "/repositories/{parent-id}/pull-requests/{id}")
    @Consumes(value = ["application/json"])
    public fun putById(
        @Body @Valid pullRequest: PullRequest,
        @PathVariable(value = "parent-id") parentId: String,
        @PathVariable(value = "id") id: String,
        @Header(value = "If-Match") ifMatch: String,
        @Header(value = "X-Flow-Id") xFlowId: String?,
        @Header(value = "Idempotency-Key") idempotencyKey: UUID?,
    ): HttpResponse<Unit>
}