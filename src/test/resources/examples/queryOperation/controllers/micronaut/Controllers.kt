package examples.queryOperation.controllers

import examples.queryOperation.models.SearchRequest
import examples.queryOperation.models.SearchResult
import io.micronaut.http.HttpResponse
import io.micronaut.http.`annotation`.Body
import io.micronaut.http.`annotation`.Consumes
import io.micronaut.http.`annotation`.Controller
import io.micronaut.http.`annotation`.CustomHttpMethod
import io.micronaut.http.`annotation`.Get
import io.micronaut.http.`annotation`.Produces
import io.micronaut.security.rules.SecurityRule
import javax.validation.Valid

@Controller
public interface SearchController {
    /**
     *
     */
    @Get(uri = "/search")
    @Produces(value = ["application/json"])
    public fun getSearch(): HttpResponse<SearchResult>

    /**
     *
     *
     * @param searchRequest
     */
    @CustomHttpMethod(
        uri = "/search",
        method = "QUERY",
    )
    @Consumes(value = ["application/json"])
    @Produces(value = ["application/json"])
    public fun search(
        @Body @Valid searchRequest: SearchRequest,
    ): HttpResponse<SearchResult>
}