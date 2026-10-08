package examples.multipleRequestMediaTypes.controllers

import examples.multipleRequestMediaTypes.models.ArrayDetailsRequestItem
import examples.multipleRequestMediaTypes.models.CountRequest
import examples.multipleRequestMediaTypes.models.DifferentInlineRequestApplicationJson
import examples.multipleRequestMediaTypes.models.DifferentInlineRequestTextJson
import examples.multipleRequestMediaTypes.models.InlineDetailsRequest
import examples.multipleRequestMediaTypes.models.RequestsDetailsRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.CookieValue
import org.springframework.web.bind.`annotation`.RequestBody
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import javax.validation.Valid
import kotlin.String
import kotlin.Unit
import kotlin.collections.List

@Controller
@Validated
@RequestMapping("")
public interface AliasController {
    /**
     *
     *
     * @param requestsDetailsRequest
     */
    @RequestMapping(
        value = ["/alias"],
        produces = [],
        method = [RequestMethod.POST],
        consumes = ["application/json", "text/json", "application/*+json"],
    )
    public fun post(
        @RequestBody @Valid requestsDetailsRequest: RequestsDetailsRequest?,
    ): ResponseEntity<Unit>
}

@Controller
@Validated
@RequestMapping("")
public interface DistinctController {
    /**
     *
     *
     * @param requestsDetailsRequest
     * @param session
     */
    @RequestMapping(
        value = ["/distinct"],
        produces = [],
        method = [RequestMethod.POST],
        consumes = ["application/json"],
    )
    public fun createDetails(
        @RequestBody @Valid requestsDetailsRequest: RequestsDetailsRequest,
        @CookieValue(value = "session", required = false) session: String?,
    ): ResponseEntity<Unit>

    /**
     *
     *
     * @param countRequest
     * @param session
     */
    @RequestMapping(
        value = ["/distinct"],
        produces = [],
        method = [RequestMethod.POST],
        consumes = ["text/json"],
    )
    public fun createDetailsTextJson(
        @RequestBody @Valid countRequest: CountRequest,
        @CookieValue(value = "session", required = false) session: String?,
    ): ResponseEntity<Unit>
}

@Controller
@Validated
@RequestMapping("")
public interface InlineController {
    /**
     *
     *
     * @param body
     */
    @RequestMapping(
        value = ["/inline"],
        produces = [],
        method = [RequestMethod.POST],
        consumes = ["application/json", "text/json"],
    )
    public fun inlineDetails(
        @RequestBody @Valid body: InlineDetailsRequest,
    ): ResponseEntity<Unit>
}

@Controller
@Validated
@RequestMapping("")
public interface DifferentInlineController {
    /**
     *
     *
     * @param requestBody
     */
    @RequestMapping(
        value = ["/different-inline"],
        produces = [],
        method = [RequestMethod.POST],
        consumes = ["application/json"],
    )
    public fun differentInline(
        @RequestBody @Valid
        requestBody: DifferentInlineRequestApplicationJson,
    ): ResponseEntity<Unit>

    /**
     *
     *
     * @param textJson
     */
    @RequestMapping(
        value = ["/different-inline"],
        produces = [],
        method = [RequestMethod.POST],
        consumes = ["text/json"],
    )
    public fun differentInlineTextJson(
        @RequestBody @Valid textJson: DifferentInlineRequestTextJson,
    ): ResponseEntity<Unit>
}

@Controller
@Validated
@RequestMapping("")
public interface ArraysController {
    /**
     *
     *
     * @param body
     */
    @RequestMapping(
        value = ["/arrays"],
        produces = [],
        method = [RequestMethod.POST],
        consumes = ["application/json", "text/json"],
    )
    public fun arrayDetails(
        @RequestBody body: List<@Valid ArrayDetailsRequestItem>,
    ): ResponseEntity<Unit>
}

@Controller
@Validated
@RequestMapping("")
public interface ComponentController {
    /**
     *
     *
     * @param requestsDetailsRequest
     */
    @RequestMapping(
        value = ["/component"],
        produces = [],
        method = [RequestMethod.POST],
        consumes = ["application/json", "text/json"],
    )
    public fun componentDetails(
        @RequestBody @Valid requestsDetailsRequest: RequestsDetailsRequest,
    ): ResponseEntity<Unit>
}
