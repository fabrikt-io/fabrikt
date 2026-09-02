package examples.cookieParameters.client

import examples.cookieParameters.models.CookiePreferences
import examples.cookieParameters.models.DisplayMode
import org.springframework.web.bind.`annotation`.CookieValue
import org.springframework.web.bind.`annotation`.PathVariable
import org.springframework.web.bind.`annotation`.RequestHeader
import org.springframework.web.bind.`annotation`.RequestParam
import org.springframework.web.service.`annotation`.HttpExchange
import kotlin.Any
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map

@Suppress("unused")
public interface CookiesClient {
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
    @HttpExchange(
        url = "/cookies/{id}",
        method = "GET",
        accept = ["application/json"],
    )
    public fun getCookiePreferences(
        @PathVariable("id") id: String,
        @CookieValue("sessionId", required = true) sessionId: String,
        @CookieValue("displayMode", required = true) displayMode: DisplayMode,
        @CookieValue("features", required = true) features: List<String>,
        @CookieValue("locale", required = false) locale: String? = null,
        @CookieValue("scopes", required = false) scopes: List<String>? = null,
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    ): CookiePreferences
}
