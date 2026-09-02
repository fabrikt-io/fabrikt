package examples.cookieParameters.controllers

import examples.cookieParameters.models.CookiePreferences
import examples.cookieParameters.models.DisplayMode
import io.micronaut.http.HttpResponse
import io.micronaut.http.`annotation`.Controller
import io.micronaut.http.`annotation`.CookieValue
import io.micronaut.http.`annotation`.Get
import io.micronaut.http.`annotation`.PathVariable
import io.micronaut.http.`annotation`.Produces
import io.micronaut.security.rules.SecurityRule
import javax.validation.Valid
import kotlin.String
import kotlin.collections.List

@Controller
public interface CookiesController {
    /**
     *
     *
     * @param id
     * @param sessionId
     * @param displayMode
     * @param features
     * @param locale
     * @param scopes
     */
    @Get(uri = "/cookies/{id}")
    @Produces(value = ["application/json"])
    public fun getCookiePreferences(
        @PathVariable(value = "id") id: String,
        @CookieValue(value = "sessionId") sessionId: String,
        @CookieValue(value = "displayMode") displayMode: DisplayMode,
        @Valid @CookieValue(value = "features") features: List<String>,
        @CookieValue(value = "locale") locale: String?,
        @Valid @CookieValue(value = "scopes") scopes: List<String>?,
    ): HttpResponse<CookiePreferences>
}