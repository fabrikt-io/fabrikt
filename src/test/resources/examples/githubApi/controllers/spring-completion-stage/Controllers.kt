package examples.completionStage.controllers

import examples.completionStage.models.BulkEntityDetails
import examples.completionStage.models.Contributor
import examples.completionStage.models.ContributorQueryResult
import examples.completionStage.models.EventResults
import examples.completionStage.models.Organisation
import examples.completionStage.models.OrganisationQueryResult
import examples.completionStage.models.PullRequest
import examples.completionStage.models.PullRequestQueryResult
import examples.completionStage.models.Repository
import examples.completionStage.models.RepositoryQueryResult
import examples.completionStage.models.StatusQueryParam
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.PathVariable
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RequestHeader
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import org.springframework.web.bind.`annotation`.RequestParam
import java.util.UUID
import java.util.concurrent.CompletionStage
import javax.validation.Valid
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Unit
import kotlin.collections.List

@Controller
@Validated
@RequestMapping("")
public interface InternalEventsController {
    /**
     * Generate change events for a list of entities
     *
     * @param bulkEntityDetails
     */
    @RequestMapping(
        value = ["/internal/events"],
        produces = ["application/json", "application/problem+json"],
        method = [RequestMethod.POST],
        consumes = ["application/json"],
    )
    public fun post(
        @RequestBody @Valid bulkEntityDetails: BulkEntityDetails,
    ): ResponseEntity<EventResults>
}

@Controller
@Validated
@RequestMapping("")
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
    @RequestMapping(
        value = ["/contributors"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun searchContributors(
        @Min(1) @Max(100) @RequestParam(value = "limit", required = false, defaultValue = "10")
        limit: Int,
        @RequestHeader(value = "X-Flow-Id", required = false) xFlowId: String?,
        @RequestParam(value = "include_inactive", required = false) includeInactive: Boolean?,
        @RequestParam(value = "cursor", required = false) cursor: String?,
    ): CompletionStage<ResponseEntity<ContributorQueryResult>>

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
    @RequestMapping(
        value = ["/contributors"],
        produces = [],
        method = [RequestMethod.POST],
        consumes = ["application/json"],
    )
    public fun createContributor(
        @RequestBody @Valid contributor: Contributor,
        @RequestHeader(value = "X-Flow-Id", required = false) xFlowId: String?,
        @RequestHeader(value = "Idempotency-Key", required = false) idempotencyKey: UUID?,
    ): CompletionStage<ResponseEntity<Unit>>

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
    @RequestMapping(
        value = ["/contributors/{id}"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun getContributor(
        @PathVariable(value = "id", required = true) id: String,
        @RequestParam(value = "status", required = false, defaultValue = "all")
        status: StatusQueryParam,
        @RequestHeader(value = "X-Flow-Id", required = false) xFlowId: String?,
        @RequestHeader(value = "If-None-Match", required = false) ifNoneMatch: String?,
    ): CompletionStage<ResponseEntity<Contributor>>

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
    @RequestMapping(
        value = ["/contributors/{id}"],
        produces = [],
        method = [RequestMethod.PUT],
        consumes = ["application/json"],
    )
    public fun putById(
        @RequestBody @Valid contributor: Contributor,
        @PathVariable(value = "id", required = true) id: String,
        @RequestHeader(value = "If-Match", required = true) ifMatch: String,
        @RequestHeader(value = "X-Flow-Id", required = false) xFlowId: String?,
        @RequestHeader(value = "Idempotency-Key", required = false) idempotencyKey: UUID?,
    ): CompletionStage<ResponseEntity<Unit>>
}

@Controller
@Validated
@RequestMapping("")
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
    @RequestMapping(
        value = ["/organisations"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun `get`(
        @Min(1) @Max(100) @RequestParam(value = "limit", required = false, defaultValue = "10")
        limit: Int,
        @RequestHeader(value = "X-Flow-Id", required = false) xFlowId: String?,
        @RequestParam(value = "include_inactive", required = false) includeInactive: Boolean?,
        @RequestParam(value = "cursor", required = false) cursor: String?,
    ): CompletionStage<ResponseEntity<OrganisationQueryResult>>

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
    @RequestMapping(
        value = ["/organisations"],
        produces = [],
        method = [RequestMethod.POST],
        consumes = ["application/json"],
    )
    public fun post(
        @RequestBody @Valid organisation: Organisation,
        @RequestHeader(value = "X-Flow-Id", required = false) xFlowId: String?,
        @RequestHeader(value = "Idempotency-Key", required = false) idempotencyKey: UUID?,
    ): CompletionStage<ResponseEntity<Unit>>

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
    @RequestMapping(
        value = ["/organisations/{id}"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun getById(
        @PathVariable(value = "id", required = true) id: String,
        @RequestParam(value = "status", required = false, defaultValue = "all")
        status: StatusQueryParam,
        @RequestHeader(value = "X-Flow-Id", required = false) xFlowId: String?,
        @RequestHeader(value = "If-None-Match", required = false) ifNoneMatch: String?,
    ): CompletionStage<ResponseEntity<Organisation>>

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
    @RequestMapping(
        value = ["/organisations/{id}"],
        produces = [],
        method = [RequestMethod.PUT],
        consumes = ["application/json"],
    )
    public fun putById(
        @RequestBody @Valid organisation: Organisation,
        @PathVariable(value = "id", required = true) id: String,
        @RequestHeader(value = "If-Match", required = true) ifMatch: String,
        @RequestHeader(value = "X-Flow-Id", required = false) xFlowId: String?,
        @RequestHeader(value = "Idempotency-Key", required = false) idempotencyKey: UUID?,
    ): CompletionStage<ResponseEntity<Unit>>
}

@Controller
@Validated
@RequestMapping("")
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
    @RequestMapping(
        value = ["/organisations/{parent-id}/contributors"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun `get`(
        @PathVariable(value = "parent-id", required = true) parentId: String,
        @Min(1) @Max(100) @RequestParam(value = "limit", required = false, defaultValue = "10")
        limit: Int,
        @RequestHeader(value = "X-Flow-Id", required = false) xFlowId: String?,
        @RequestParam(value = "include_inactive", required = false) includeInactive: Boolean?,
        @RequestParam(value = "cursor", required = false) cursor: String?,
    ): CompletionStage<ResponseEntity<ContributorQueryResult>>

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
    @RequestMapping(
        value = ["/organisations/{parent-id}/contributors/{id}"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun getById(
        @PathVariable(value = "parent-id", required = true) parentId: String,
        @PathVariable(value = "id", required = true) id: String,
        @RequestHeader(value = "X-Flow-Id", required = false) xFlowId: String?,
        @RequestHeader(value = "If-None-Match", required = false) ifNoneMatch: String?,
    ): CompletionStage<ResponseEntity<Contributor>>

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
    @RequestMapping(
        value = ["/organisations/{parent-id}/contributors/{id}"],
        produces = [],
        method = [RequestMethod.PUT],
    )
    public fun putById(
        @PathVariable(value = "parent-id", required = true) parentId: String,
        @PathVariable(value = "id", required = true) id: String,
        @RequestHeader(value = "If-Match", required = true) ifMatch: String,
        @RequestHeader(value = "X-Flow-Id", required = false) xFlowId: String?,
        @RequestHeader(value = "Idempotency-Key", required = false) idempotencyKey: UUID?,
    ): CompletionStage<ResponseEntity<Unit>>

    /**
     * Remove Contributor from this Organisation. Does not delete the underlying Contributor.
     *
     * @param parentId The unique id for the parent resource
     * @param id The unique id for the resource
     * @param xFlowId A custom header that will be passed onto any further requests and can be used
     * for diagnosing.
     *
     */
    @RequestMapping(
        value = ["/organisations/{parent-id}/contributors/{id}"],
        produces = [],
        method = [RequestMethod.DELETE],
    )
    public fun deleteById(
        @PathVariable(value = "parent-id", required = true) parentId: String,
        @PathVariable(value = "id", required = true) id: String,
        @RequestHeader(value = "X-Flow-Id", required = false) xFlowId: String?,
    ): CompletionStage<ResponseEntity<Unit>>
}

@Controller
@Validated
@RequestMapping("")
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
    @RequestMapping(
        value = ["/repositories"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun `get`(
        @Min(1) @Max(100) @RequestParam(value = "limit", required = false, defaultValue = "10")
        limit: Int,
        @RequestHeader(value = "X-Flow-Id", required = false) xFlowId: String?,
        @Valid @RequestParam(value = "slug", required = false) slug: List<String>?,
        @Valid @RequestParam(value = "name", required = false) name: List<String>?,
        @RequestParam(value = "include_inactive", required = false) includeInactive: Boolean?,
        @RequestParam(value = "cursor", required = false) cursor: String?,
    ): CompletionStage<ResponseEntity<RepositoryQueryResult>>

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
    @RequestMapping(
        value = ["/repositories"],
        produces = [],
        method = [RequestMethod.POST],
        consumes = ["application/json"],
    )
    public fun post(
        @RequestBody @Valid repository: Repository,
        @RequestHeader(value = "X-Flow-Id", required = false) xFlowId: String?,
        @RequestHeader(value = "Idempotency-Key", required = false) idempotencyKey: UUID?,
    ): CompletionStage<ResponseEntity<Unit>>

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
    @RequestMapping(
        value = ["/repositories/{id}"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun getById(
        @PathVariable(value = "id", required = true) id: String,
        @RequestParam(value = "status", required = false, defaultValue = "all")
        status: StatusQueryParam,
        @RequestHeader(value = "X-Flow-Id", required = false) xFlowId: String?,
        @RequestHeader(value = "If-None-Match", required = false) ifNoneMatch: String?,
    ): CompletionStage<ResponseEntity<Repository>>

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
    @RequestMapping(
        value = ["/repositories/{id}"],
        produces = [],
        method = [RequestMethod.PUT],
        consumes = ["application/json"],
    )
    public fun putById(
        @RequestBody @Valid repository: Repository,
        @PathVariable(value = "id", required = true) id: String,
        @RequestHeader(value = "If-Match", required = true) ifMatch: String,
        @RequestHeader(value = "X-Flow-Id", required = false) xFlowId: String?,
        @RequestHeader(value = "Idempotency-Key", required = false) idempotencyKey: UUID?,
    ): CompletionStage<ResponseEntity<Unit>>
}

@Controller
@Validated
@RequestMapping("")
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
    @RequestMapping(
        value = ["/repositories/{parent-id}/pull-requests"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun `get`(
        @PathVariable(value = "parent-id", required = true) parentId: String,
        @Min(1) @Max(100) @RequestParam(value = "limit", required = false, defaultValue = "10")
        limit: Int,
        @RequestHeader(value = "X-Flow-Id", required = false) xFlowId: String?,
        @RequestParam(value = "include_inactive", required = false) includeInactive: Boolean?,
        @RequestParam(value = "cursor", required = false) cursor: String?,
    ): CompletionStage<ResponseEntity<PullRequestQueryResult>>

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
    @RequestMapping(
        value = ["/repositories/{parent-id}/pull-requests"],
        produces = [],
        method = [RequestMethod.POST],
        consumes = ["application/json"],
    )
    public fun post(
        @RequestBody @Valid pullRequest: PullRequest,
        @PathVariable(value = "parent-id", required = true) parentId: String,
        @RequestHeader(value = "X-Flow-Id", required = false) xFlowId: String?,
        @RequestHeader(value = "Idempotency-Key", required = false) idempotencyKey: UUID?,
    ): CompletionStage<ResponseEntity<Unit>>

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
    @RequestMapping(
        value = ["/repositories/{parent-id}/pull-requests/{id}"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun getById(
        @PathVariable(value = "parent-id", required = true) parentId: String,
        @PathVariable(value = "id", required = true) id: String,
        @RequestHeader(value = "X-Flow-Id", required = false) xFlowId: String?,
        @RequestHeader(value = "If-None-Match", required = false) ifNoneMatch: String?,
    ): CompletionStage<ResponseEntity<PullRequest>>

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
    @RequestMapping(
        value = ["/repositories/{parent-id}/pull-requests/{id}"],
        produces = [],
        method = [RequestMethod.PUT],
        consumes = ["application/json"],
    )
    public fun putById(
        @RequestBody @Valid pullRequest: PullRequest,
        @PathVariable(value = "parent-id", required = true) parentId: String,
        @PathVariable(value = "id", required = true) id: String,
        @RequestHeader(value = "If-Match", required = true) ifMatch: String,
        @RequestHeader(value = "X-Flow-Id", required = false) xFlowId: String?,
        @RequestHeader(value = "Idempotency-Key", required = false) idempotencyKey: UUID?,
    ): CompletionStage<ResponseEntity<Unit>>
}
