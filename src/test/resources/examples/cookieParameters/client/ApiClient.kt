package examples.cookieParameters.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import examples.cookieParameters.models.CookiePreferences
import examples.cookieParameters.models.DisplayMode
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.jvm.Throws

@Suppress("unused")
public class CookiesClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
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
    @Throws(ApiException::class)
    public fun getCookiePreferences(
        id: String,
        sessionId: String,
        displayMode: DisplayMode,
        features: List<String>,
        locale: String? = null,
        scopes: List<String>? = null,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<CookiePreferences> {
        val httpUrl: HttpUrl =
            "$baseUrl/cookies/{id}"
                .pathParam("{id}" to id)
                .toHttpUrl()
                .newBuilder()
                .also { builder -> additionalQueryParameters.forEach { builder.queryParam(it.key, it.value) } }
                .build()

        val headerBuilder = Headers.Builder()
        val cookieValues =
            buildList {
                sessionId.let { add("sessionId=" + it) }
                displayMode.let { add("displayMode=" + it.value) }
                features.forEach { add("features=" + it) }
                locale?.let { add("locale=" + it) }
                scopes?.let { add("scopes=" + it.joinToString(",")) }
            }
        if (cookieValues.isNotEmpty()) headerBuilder.add("Cookie", cookieValues.joinToString("; "))
        additionalHeaders.forEach { headerBuilder.header(it.key, it.value) }
        val httpHeaders: Headers = headerBuilder.build()

        val request: Request =
            Request
                .Builder()
                .url(httpUrl)
                .headers(httpHeaders)
                .get()
                .build()

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }
}
