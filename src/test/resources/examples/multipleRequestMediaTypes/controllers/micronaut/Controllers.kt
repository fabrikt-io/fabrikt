package examples.multipleRequestMediaTypes.controllers

import examples.multipleRequestMediaTypes.models.ArrayDetailsRequestItem
import examples.multipleRequestMediaTypes.models.CountRequest
import examples.multipleRequestMediaTypes.models.DifferentInlineRequestApplicationJson
import examples.multipleRequestMediaTypes.models.DifferentInlineRequestTextJson
import examples.multipleRequestMediaTypes.models.InlineDetailsRequest
import examples.multipleRequestMediaTypes.models.RequestsDetailsRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.`annotation`.Body
import io.micronaut.http.`annotation`.Consumes
import io.micronaut.http.`annotation`.Controller
import io.micronaut.http.`annotation`.CookieValue
import io.micronaut.http.`annotation`.Post
import io.micronaut.security.rules.SecurityRule
import javax.validation.Valid
import kotlin.String
import kotlin.Unit
import kotlin.collections.List

@Controller
public interface AliasController {
    /**
     *
     *
     * @param requestsDetailsRequest
     */
    @Post(uri = "/alias")
    @Consumes(value = ["application/json", "text/json", "application/*+json"])
    public fun post(
        @Body @Valid requestsDetailsRequest: RequestsDetailsRequest?,
    ): HttpResponse<Unit>
}

@Controller
public interface DistinctController {
    /**
     *
     *
     * @param requestsDetailsRequest
     * @param session
     */
    @Post(uri = "/distinct")
    @Consumes(value = ["application/json"])
    public fun createDetails(
        @Body @Valid requestsDetailsRequest: RequestsDetailsRequest,
        @CookieValue(value = "session") session: String?,
    ): HttpResponse<Unit>

    /**
     *
     *
     * @param countRequest
     * @param session
     */
    @Post(uri = "/distinct")
    @Consumes(value = ["text/json"])
    public fun createDetailsTextJson(
        @Body @Valid countRequest: CountRequest,
        @CookieValue(
            value =
                "session",
        ) session: String?,
    ): HttpResponse<Unit>
}

@Controller
public interface InlineController {
    /**
     *
     *
     * @param body
     */
    @Post(uri = "/inline")
    @Consumes(value = ["application/json", "text/json"])
    public fun inlineDetails(
        @Body @Valid body: InlineDetailsRequest,
    ): HttpResponse<Unit>
}

@Controller
public interface DifferentInlineController {
    /**
     *
     *
     * @param requestBody
     */
    @Post(uri = "/different-inline")
    @Consumes(value = ["application/json"])
    public fun differentInline(
        @Body @Valid requestBody: DifferentInlineRequestApplicationJson,
    ): HttpResponse<Unit>

    /**
     *
     *
     * @param textJson
     */
    @Post(uri = "/different-inline")
    @Consumes(value = ["text/json"])
    public fun differentInlineTextJson(
        @Body @Valid textJson: DifferentInlineRequestTextJson,
    ): HttpResponse<Unit>
}

@Controller
public interface ArraysController {
    /**
     *
     *
     * @param body
     */
    @Post(uri = "/arrays")
    @Consumes(value = ["application/json", "text/json"])
    public fun arrayDetails(
        @Body @Valid body: List<ArrayDetailsRequestItem>,
    ): HttpResponse<Unit>
}

@Controller
public interface ComponentController {
    /**
     *
     *
     * @param requestsDetailsRequest
     */
    @Post(uri = "/component")
    @Consumes(value = ["application/json", "text/json"])
    public fun componentDetails(
        @Body @Valid requestsDetailsRequest: RequestsDetailsRequest,
    ): HttpResponse<Unit>
}