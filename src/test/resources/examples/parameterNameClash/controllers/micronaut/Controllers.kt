package examples.parameterNameClash.controllers

import examples.parameterNameClash.models.SomeObject
import io.micronaut.http.HttpResponse
import io.micronaut.http.`annotation`.Body
import io.micronaut.http.`annotation`.Consumes
import io.micronaut.http.`annotation`.Controller
import io.micronaut.http.`annotation`.Get
import io.micronaut.http.`annotation`.PathVariable
import io.micronaut.http.`annotation`.Post
import io.micronaut.http.`annotation`.QueryValue
import io.micronaut.security.rules.SecurityRule
import javax.validation.Valid
import kotlin.String
import kotlin.Unit

@Controller
public interface ExampleController {
    /**
     * Returns 100% of the matching records
     * The merchant key must be prefixed by `key%3D`.
     * A literal %S is prose here, not a format specifier.
     *
     * @param pathB Identifier, percent-encoded (%3D is '=')
     * @param queryB
     */
    @Get(uri = "/example/{b}")
    public fun getById(
        @PathVariable(value = "pathB") pathB: String,
        @QueryValue(value = "queryB")
        queryB: String,
    ): HttpResponse<Unit>

    /**
     *
     *
     * @param bodySomeObject example
     * @param querySomeObject
     */
    @Post(uri = "/example")
    @Consumes(value = ["application/json"])
    public fun post(
        @Body @Valid bodySomeObject: SomeObject?,
        @QueryValue(value = "querySomeObject")
        querySomeObject: String,
    ): HttpResponse<Unit>
}