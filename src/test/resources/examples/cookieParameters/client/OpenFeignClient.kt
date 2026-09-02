package examples.cookieParameters.client

import examples.cookieParameters.models.CookiePreferences
import examples.cookieParameters.models.DisplayMode
import feign.HeaderMap
import feign.Headers
import feign.Param
import feign.QueryMap
import feign.RequestLine
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.jvm.JvmSynthetic

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
    public fun getCookiePreferences(
        id: String,
        sessionId: String,
        displayMode: DisplayMode,
        features: List<String>,
        locale: String? = null,
        scopes: List<String>? = null,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): CookiePreferences =
        getCookiePreferencesWithCookieHeader(
            id = id,
            cookieHeader =
                buildList {
                    add("sessionId=" + sessionId)
                    add("displayMode=" + displayMode.value)
                    features.forEach { add("features=" + it) }
                    locale?.let { add("locale=" + it) }
                    scopes?.let { values -> add("scopes=" + values.joinToString(",")) }
                }.joinToString("; "),
            additionalHeaders = additionalHeaders,
            additionalQueryParameters = additionalQueryParameters,
        )

    @RequestLine("GET /cookies/{id}")
    @Headers(
        "Cookie: {cookieHeader}",
        "Accept: application/json",
    )
    @JvmSynthetic
    public fun getCookiePreferencesWithCookieHeader(
        @Param("id") id: String,
        @Param("cookieHeader") cookieHeader: String,
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    ): CookiePreferences
}
