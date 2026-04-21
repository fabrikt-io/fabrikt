package examples.singleAllOf.controllers

import examples.singleAllOf.models.Result
import io.micronaut.http.HttpResponse
import io.micronaut.http.`annotation`.Controller
import io.micronaut.http.`annotation`.Get
import io.micronaut.http.`annotation`.Produces
import io.micronaut.security.rules.SecurityRule

@Controller
public interface TestController {
    /**
     *
     */
    @Get(uri = "/test")
    @Produces(value = ["application/json"])
    public fun test(): HttpResponse<Result?>
}