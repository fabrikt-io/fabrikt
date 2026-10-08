package examples.oauth2Security.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.Map
import kotlin.collections.Set
import kotlin.jvm.Throws

@Suppress("unused")
public class ProtectedClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     *
     */
    @Throws(ApiException::class)
    public fun getProtected(
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/protected"
                .toHttpUrl()
                .newBuilder()
                .also { builder -> additionalQueryParameters.forEach { builder.queryParam(it.key, it.value) } }
                .build()

        val headerBuilder = Headers.Builder()
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

    public fun getProtectedWithOAuth2Token(
        oauth2TokenProvider: (String, Set<String>) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val oauth2Token =
            listOf("OAuth2" to setOf("read:pets")).firstNotNullOfOrNull {
                (
                    scheme,
                    scopes,
                ),
                ->
                oauth2TokenProvider(scheme, scopes)?.takeIf { it.isNotBlank() }
            }
        checkNotNull(oauth2Token) { "An OAuth2 access token is required for this operation" }
        val oauth2Headers =
            oauth2Token?.let { additionalHeaders + ("Authorization" to "Bearer $it") }
                ?: additionalHeaders
        return getProtected(
            additionalHeaders = oauth2Headers,
            additionalQueryParameters = additionalQueryParameters,
        )
    }
}

@Suppress("unused")
public class AnonymousClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     *
     */
    @Throws(ApiException::class)
    public fun getAnonymous(
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/anonymous"
                .toHttpUrl()
                .newBuilder()
                .also { builder -> additionalQueryParameters.forEach { builder.queryParam(it.key, it.value) } }
                .build()

        val headerBuilder = Headers.Builder()
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

@Suppress("unused")
public class OptionalClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     *
     */
    @Throws(ApiException::class)
    public fun getOptional(
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/optional"
                .toHttpUrl()
                .newBuilder()
                .also { builder -> additionalQueryParameters.forEach { builder.queryParam(it.key, it.value) } }
                .build()

        val headerBuilder = Headers.Builder()
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

    public fun getOptionalWithOAuth2Token(
        oauth2TokenProvider: (String, Set<String>) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val oauth2Token =
            listOf("OAuth2" to setOf("read:pets")).firstNotNullOfOrNull {
                (
                    scheme,
                    scopes,
                ),
                ->
                oauth2TokenProvider(scheme, scopes)?.takeIf { it.isNotBlank() }
            }
        val oauth2Headers =
            oauth2Token?.let { additionalHeaders + ("Authorization" to "Bearer $it") }
                ?: additionalHeaders
        return getOptional(
            additionalHeaders = oauth2Headers,
            additionalQueryParameters = additionalQueryParameters,
        )
    }
}

@Suppress("unused")
public class CombinedClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     *
     */
    @Throws(ApiException::class)
    public fun getCombined(
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/combined"
                .toHttpUrl()
                .newBuilder()
                .also { builder -> additionalQueryParameters.forEach { builder.queryParam(it.key, it.value) } }
                .build()

        val headerBuilder = Headers.Builder()
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

@Suppress("unused")
public class AlternativeClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     *
     */
    @Throws(ApiException::class)
    public fun getAlternative(
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/alternative"
                .toHttpUrl()
                .newBuilder()
                .also { builder -> additionalQueryParameters.forEach { builder.queryParam(it.key, it.value) } }
                .build()

        val headerBuilder = Headers.Builder()
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

    public fun getAlternativeWithOAuth2Token(
        oauth2TokenProvider: (String, Set<String>) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val oauth2Token =
            listOf("OAuth2" to setOf("read:pets")).firstNotNullOfOrNull {
                (
                    scheme,
                    scopes,
                ),
                ->
                oauth2TokenProvider(scheme, scopes)?.takeIf { it.isNotBlank() }
            }
        val oauth2Headers =
            oauth2Token?.let { additionalHeaders + ("Authorization" to "Bearer $it") }
                ?: additionalHeaders
        return getAlternative(
            additionalHeaders = oauth2Headers,
            additionalQueryParameters = additionalQueryParameters,
        )
    }
}

@Suppress("unused")
public class ScopeAlternativesClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     *
     */
    @Throws(ApiException::class)
    public fun getScopeAlternatives(
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/scope-alternatives"
                .toHttpUrl()
                .newBuilder()
                .also { builder -> additionalQueryParameters.forEach { builder.queryParam(it.key, it.value) } }
                .build()

        val headerBuilder = Headers.Builder()
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

    public fun getScopeAlternativesWithOAuth2Token(
        oauth2TokenProvider: (String, Set<String>) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val oauth2Token =
            listOf(
                "OAuth2" to setOf("read:pets"),
                "OAuth2" to
                    setOf("write:pets"),
            ).firstNotNullOfOrNull { (scheme, scopes) ->
                oauth2TokenProvider(
                    scheme,
                    scopes,
                )?.takeIf { it.isNotBlank() }
            }
        checkNotNull(oauth2Token) { "An OAuth2 access token is required for this operation" }
        val oauth2Headers =
            oauth2Token?.let { additionalHeaders + ("Authorization" to "Bearer $it") }
                ?: additionalHeaders
        return getScopeAlternatives(
            additionalHeaders = oauth2Headers,
            additionalQueryParameters = additionalQueryParameters,
        )
    }
}

@Suppress("unused")
public class UnscopedClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     *
     */
    @Throws(ApiException::class)
    public fun getUnscoped(
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/unscoped"
                .toHttpUrl()
                .newBuilder()
                .also { builder -> additionalQueryParameters.forEach { builder.queryParam(it.key, it.value) } }
                .build()

        val headerBuilder = Headers.Builder()
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

    public fun getUnscopedWithOAuth2Token(
        oauth2TokenProvider: (String, Set<String>) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val oauth2Token =
            listOf("OAuth2" to setOf<String>()).firstNotNullOfOrNull { (scheme, scopes) ->
                oauth2TokenProvider(scheme, scopes)?.takeIf { it.isNotBlank() }
            }
        checkNotNull(oauth2Token) { "An OAuth2 access token is required for this operation" }
        val oauth2Headers =
            oauth2Token?.let { additionalHeaders + ("Authorization" to "Bearer $it") }
                ?: additionalHeaders
        return getUnscoped(
            additionalHeaders = oauth2Headers,
            additionalQueryParameters = additionalQueryParameters,
        )
    }
}

@Suppress("unused")
public class SchemesClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     *
     */
    @Throws(ApiException::class)
    public fun getSchemes(
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/schemes"
                .toHttpUrl()
                .newBuilder()
                .also { builder -> additionalQueryParameters.forEach { builder.queryParam(it.key, it.value) } }
                .build()

        val headerBuilder = Headers.Builder()
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

    public fun getSchemesWithOAuth2Token(
        oauth2TokenProvider: (String, Set<String>) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val oauth2Token =
            listOf(
                "OAuth2" to setOf("read:pets"),
                "OtherOAuth2" to
                    setOf("write:pets"),
            ).firstNotNullOfOrNull { (scheme, scopes) ->
                oauth2TokenProvider(
                    scheme,
                    scopes,
                )?.takeIf { it.isNotBlank() }
            }
        checkNotNull(oauth2Token) { "An OAuth2 access token is required for this operation" }
        val oauth2Headers =
            oauth2Token?.let { additionalHeaders + ("Authorization" to "Bearer $it") }
                ?: additionalHeaders
        return getSchemes(
            additionalHeaders = oauth2Headers,
            additionalQueryParameters = additionalQueryParameters,
        )
    }
}
