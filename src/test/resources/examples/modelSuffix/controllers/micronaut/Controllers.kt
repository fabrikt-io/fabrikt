package examples.modelSuffix.controllers

import examples.modelSuffix.models.ModeParameterDto
import examples.modelSuffix.models.RootTypeDto
import io.micronaut.http.HttpResponse
import io.micronaut.http.`annotation`.Body
import io.micronaut.http.`annotation`.Consumes
import io.micronaut.http.`annotation`.Controller
import io.micronaut.http.`annotation`.Get
import io.micronaut.http.`annotation`.Post
import io.micronaut.http.`annotation`.Produces
import io.micronaut.http.`annotation`.QueryValue
import io.micronaut.security.rules.SecurityRule
import javax.validation.Valid

@Controller
public interface ExampleController {
    /**
     *
     *
     * @param mode
     */
    @Get(uri = "/example")
    @Produces(value = ["application/json"])
    public fun `get`(
        @QueryValue(value = "mode") mode: ModeParameterDto,
    ): HttpResponse<RootTypeDto>

    /**
     *
     *
     * @param rootType
     */
    @Post(uri = "/example")
    @Consumes(value = ["application/json"])
    @Produces(value = ["application/json"])
    public fun post(
        @Body @Valid rootType: RootTypeDto?,
    ): HttpResponse<RootTypeDto>
}