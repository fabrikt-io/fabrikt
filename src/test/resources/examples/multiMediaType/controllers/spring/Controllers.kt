package examples.multiMediaType.controllers

import com.fasterxml.jackson.databind.JsonNode
import examples.multiMediaType.models.QueryResult
import examples.multiMediaType.models.SuccessResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.RequestHeader
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import org.springframework.web.bind.`annotation`.RequestParam
import javax.validation.Valid
import kotlin.Int
import kotlin.String
import kotlin.collections.List

@Controller
@Validated
@RequestMapping("")
public interface ExamplePath1Controller {
    /**
     * GET example path 1
     *
     * @param explodeListQueryParam
     * @param queryParam2
     */
    @RequestMapping(
        value = ["/example-path-1"],
        produces = [
            "application/vnd.custom.media+xml", "application/vnd.custom.media+json",
            "application/problem+json",
        ],
        method = [RequestMethod.GET],
    )
    public fun `get`(
        @Valid @RequestParam(value = "explode_list_query_param", required = false)
        explodeListQueryParam: List<String>?,
        @RequestParam(value = "query_param2", required = false)
        queryParam2: Int?,
    ): ResponseEntity<QueryResult>
}

@Controller
@Validated
@RequestMapping("")
public interface ExamplePath2Controller {
    /**
     * GET example path 1
     *
     * @param explodeListQueryParam
     * @param queryParam2
     * @param accept the content type accepted by the client
     */
    @RequestMapping(
        value = ["/example-path-2"],
        produces = ["application/vnd.custom.media+xml", "application/vnd.custom.media+json"],
        method = [RequestMethod.GET],
    )
    public fun `get`(
        @Valid @RequestParam(value = "explode_list_query_param", required = false)
        explodeListQueryParam: List<String>?,
        @RequestParam(value = "query_param2", required = false) queryParam2: Int?,
        @RequestHeader(value = "Accept", required = false) accept: String?,
    ): ResponseEntity<QueryResult>
}

@Controller
@Validated
@RequestMapping("")
public interface MultipleResponseSchemasController {
    /**
     * GET with multiple response content schemas
     *
     * @param accept the content type accepted by the client
     */
    @RequestMapping(
        value = ["/multiple-response-schemas"],
        produces = ["application/json", "application/vnd.custom.media+json"],
        method = [RequestMethod.GET],
    )
    public fun `get`(
        @RequestHeader(value = "Accept", required = false) accept: String?,
    ): ResponseEntity<JsonNode>
}

@Controller
@Validated
@RequestMapping("")
public interface DifferentSuccessAndErrorResponseSchemaController {
    /**
     *
     */
    @RequestMapping(
        value = ["/different-success-and-error-response-schema"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun `get`(): ResponseEntity<SuccessResponse>
}
