package examples.cookieParameters.controllers

import examples.cookieParameters.models.CookiePreferences
import examples.cookieParameters.models.DisplayMode
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.validation.`annotation`.Validated
import org.springframework.web.bind.`annotation`.CookieValue
import org.springframework.web.bind.`annotation`.PathVariable
import org.springframework.web.bind.`annotation`.RequestMapping
import org.springframework.web.bind.`annotation`.RequestMethod
import javax.validation.Valid
import kotlin.String
import kotlin.collections.List

@Controller
@Validated
@RequestMapping("")
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
    @RequestMapping(
        value = ["/cookies/{id}"],
        produces = ["application/json"],
        method = [RequestMethod.GET],
    )
    public fun getCookiePreferences(
        @PathVariable(value = "id", required = true) id: String,
        @CookieValue(value = "sessionId", required = true) sessionId: String,
        @CookieValue(value = "displayMode", required = true) displayMode: DisplayMode,
        @Valid @CookieValue(value = "features", required = true) features: List<String>,
        @CookieValue(value = "locale", required = false) locale: String?,
        @Valid @CookieValue(value = "scopes", required = false) scopes: List<String>?,
    ): ResponseEntity<CookiePreferences>
}
