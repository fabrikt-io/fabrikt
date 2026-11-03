package examples.headerApiKeySecurity.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.nio.charset.Charset
import java.util.Base64
import kotlin.Pair
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.Map
import kotlin.collections.Set
import kotlin.jvm.Throws
import kotlin.text.Charsets

@Suppress("unused")
public class DocumentsClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     *
     */
    @Throws(ApiException::class)
    public fun getDocuments(
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/documents"
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

    public fun getDocumentsWithApiKey(
        apiKeyProvider: (String) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val credentialCache = mutableMapOf<Pair<String, Set<String>>, String?>()
        val authenticationHeaders =
            sequenceOf<() -> Map<String, String>?>(
                {
                    if (!credentialCache.containsKey("Key" to setOf<String>())) {
                        credentialCache["Key" to setOf<String>()] = apiKeyProvider("Key")?.takeIf { it.isNotBlank() }
                    }
                    val credential = credentialCache["Key" to setOf<String>()]
                    require(
                        credential == null ||
                            credential.none {
                                it.code < 32 || it.code == 127
                            },
                    ) { "Authentication credentials must not contain control characters" }
                    if (credential != null) {
                        mapOf(
                            "X-API-Key" to credential!!,
                        )
                    } else {
                        null
                    }
                },
            ).firstNotNullOfOrNull { it() }
        checkNotNull(authenticationHeaders) { "Credentials for a complete security alternative are required for this operation" }
        val authenticatedConfiguration =
            additionalHeaders.filterKeys { existing -> authenticationHeaders.keys.none { it.equals(existing, ignoreCase = true) } } +
                authenticationHeaders
        return getDocuments(
            additionalHeaders = authenticatedConfiguration,
            additionalQueryParameters = additionalQueryParameters,
        )
    }

    public fun getDocumentsWithOAuth2Token(
        oauth2TokenProvider: (String, Set<String>) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val credentialCache = mutableMapOf<Pair<String, Set<String>>, String?>()
        val authenticationHeaders =
            sequenceOf<() -> Map<String, String>?>(
                {
                    if (!credentialCache.containsKey("OAuth" to setOf<String>("read:documents"))) {
                        credentialCache["OAuth" to setOf<String>("read:documents")] =
                            oauth2TokenProvider("OAuth", setOf<String>("read:documents"))?.takeIf { it.isNotBlank() }
                    }
                    val credential = credentialCache["OAuth" to setOf<String>("read:documents")]
                    require(
                        credential == null ||
                            credential.none {
                                it.code < 32 || it.code == 127
                            },
                    ) { "Authentication credentials must not contain control characters" }
                    if (credential != null) {
                        mapOf(
                            "Authorization" to ("Bearer " + credential!!),
                        )
                    } else {
                        null
                    }
                },
            ).firstNotNullOfOrNull { it() }
        checkNotNull(authenticationHeaders) { "Credentials for a complete security alternative are required for this operation" }
        val authenticatedConfiguration =
            additionalHeaders.filterKeys { existing -> authenticationHeaders.keys.none { it.equals(existing, ignoreCase = true) } } +
                authenticationHeaders
        return getDocuments(
            additionalHeaders = authenticatedConfiguration,
            additionalQueryParameters = additionalQueryParameters,
        )
    }

    /**
     *
     */
    @Throws(ApiException::class)
    public fun createDocument(
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/documents"
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
                .post(ByteArray(0).toRequestBody())
                .build()

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }

    public fun createDocumentWithApiKeyAndOAuth2Token(
        apiKeyProvider: (String) -> String?,
        oauth2TokenProvider: (String, Set<String>) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val credentialCache = mutableMapOf<Pair<String, Set<String>>, String?>()
        val authenticationHeaders =
            sequenceOf<() -> Map<String, String>?>(
                {
                    if (!credentialCache.containsKey("Key" to setOf<String>())) {
                        credentialCache["Key" to setOf<String>()] = apiKeyProvider("Key")?.takeIf { it.isNotBlank() }
                    }
                    val credential = credentialCache["Key" to setOf<String>()]
                    require(
                        credential == null ||
                            credential.none {
                                it.code < 32 || it.code == 127
                            },
                    ) { "Authentication credentials must not contain control characters" }
                    if (!credentialCache.containsKey("OAuth" to setOf<String>("write:documents"))) {
                        credentialCache["OAuth" to setOf<String>("write:documents")] =
                            oauth2TokenProvider("OAuth", setOf<String>("write:documents"))?.takeIf { it.isNotBlank() }
                    }
                    val credentialExtra = credentialCache["OAuth" to setOf<String>("write:documents")]
                    require(
                        credentialExtra == null ||
                            credentialExtra.none {
                                it.code < 32 || it.code == 127
                            },
                    ) { "Authentication credentials must not contain control characters" }
                    if (credential != null && credentialExtra != null) {
                        mapOf(
                            "X-API-Key" to credential!!,
                            "Authorization" to ("Bearer " + credentialExtra!!),
                        )
                    } else {
                        null
                    }
                },
            ).firstNotNullOfOrNull { it() }
        checkNotNull(authenticationHeaders) { "Credentials for a complete security alternative are required for this operation" }
        val authenticatedConfiguration =
            additionalHeaders.filterKeys { existing -> authenticationHeaders.keys.none { it.equals(existing, ignoreCase = true) } } +
                authenticationHeaders
        return createDocument(
            additionalHeaders = authenticatedConfiguration,
            additionalQueryParameters = additionalQueryParameters,
        )
    }

    /**
     *
     */
    @Throws(ApiException::class)
    public fun replaceDocument(
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/documents"
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
                .put(ByteArray(0).toRequestBody())
                .build()

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }

    public fun replaceDocumentWithApiKey(
        apiKeyProvider: (String) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val credentialCache = mutableMapOf<Pair<String, Set<String>>, String?>()
        val authenticationHeaders =
            sequenceOf<() -> Map<String, String>?>(
                {
                    if (!credentialCache.containsKey("Key" to setOf<String>())) {
                        credentialCache["Key" to setOf<String>()] = apiKeyProvider("Key")?.takeIf { it.isNotBlank() }
                    }
                    val credential = credentialCache["Key" to setOf<String>()]
                    require(
                        credential == null ||
                            credential.none {
                                it.code < 32 || it.code == 127
                            },
                    ) { "Authentication credentials must not contain control characters" }
                    if (credential != null) {
                        mapOf(
                            "X-API-Key" to credential!!,
                        )
                    } else {
                        null
                    }
                },
            ).firstNotNullOfOrNull { it() }
        checkNotNull(authenticationHeaders) { "Credentials for a complete security alternative are required for this operation" }
        val authenticatedConfiguration =
            additionalHeaders.filterKeys { existing -> authenticationHeaders.keys.none { it.equals(existing, ignoreCase = true) } } +
                authenticationHeaders
        return replaceDocument(
            additionalHeaders = authenticatedConfiguration,
            additionalQueryParameters = additionalQueryParameters,
        )
    }

    /**
     *
     */
    @Throws(ApiException::class)
    public fun deleteDocument(
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/documents"
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
                .delete()
                .build()

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }

    /**
     *
     */
    @Throws(ApiException::class)
    public fun updateDocument(
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/documents"
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
                .patch(ByteArray(0).toRequestBody())
                .build()

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }

    public fun updateDocumentWithApiKeyAndOAuth2Token(
        apiKeyProvider: (String) -> String?,
        oauth2TokenProvider: (String, Set<String>) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val credentialCache = mutableMapOf<Pair<String, Set<String>>, String?>()
        val authenticationHeaders =
            sequenceOf<() -> Map<String, String>?>(
                {
                    if (!credentialCache.containsKey("Key" to setOf<String>())) {
                        credentialCache["Key" to setOf<String>()] = apiKeyProvider("Key")?.takeIf { it.isNotBlank() }
                    }
                    val credential = credentialCache["Key" to setOf<String>()]
                    require(
                        credential == null ||
                            credential.none {
                                it.code < 32 || it.code == 127
                            },
                    ) { "Authentication credentials must not contain control characters" }
                    if (!credentialCache.containsKey("OAuth" to setOf<String>("write:documents"))) {
                        credentialCache["OAuth" to setOf<String>("write:documents")] =
                            oauth2TokenProvider("OAuth", setOf<String>("write:documents"))?.takeIf { it.isNotBlank() }
                    }
                    val credentialExtra = credentialCache["OAuth" to setOf<String>("write:documents")]
                    require(
                        credentialExtra == null ||
                            credentialExtra.none {
                                it.code < 32 || it.code == 127
                            },
                    ) { "Authentication credentials must not contain control characters" }
                    if (credential != null && credentialExtra != null) {
                        mapOf(
                            "X-API-Key" to credential!!,
                            "Authorization" to ("Bearer " + credentialExtra!!),
                        )
                    } else {
                        null
                    }
                },
                {
                    if (!credentialCache.containsKey("OtherKey" to setOf<String>())) {
                        credentialCache["OtherKey" to setOf<String>()] = apiKeyProvider("OtherKey")?.takeIf { it.isNotBlank() }
                    }
                    val credentialExtraExtra = credentialCache["OtherKey" to setOf<String>()]
                    require(
                        credentialExtraExtra == null ||
                            credentialExtraExtra.none {
                                it.code < 32 || it.code == 127
                            },
                    ) { "Authentication credentials must not contain control characters" }
                    if (!credentialCache.containsKey("OAuth" to setOf<String>("admin:documents"))) {
                        credentialCache["OAuth" to setOf<String>("admin:documents")] =
                            oauth2TokenProvider("OAuth", setOf<String>("admin:documents"))?.takeIf { it.isNotBlank() }
                    }
                    val credentialExtraExtraExtra = credentialCache["OAuth" to setOf<String>("admin:documents")]
                    require(
                        credentialExtraExtraExtra == null ||
                            credentialExtraExtraExtra.none {
                                it.code < 32 || it.code == 127
                            },
                    ) { "Authentication credentials must not contain control characters" }
                    if (credentialExtraExtra != null && credentialExtraExtraExtra != null) {
                        mapOf(
                            "X-Other-Key" to credentialExtraExtra!!,
                            "Authorization" to ("Bearer " + credentialExtraExtraExtra!!),
                        )
                    } else {
                        null
                    }
                },
            ).firstNotNullOfOrNull { it() }
        checkNotNull(authenticationHeaders) { "Credentials for a complete security alternative are required for this operation" }
        val authenticatedConfiguration =
            additionalHeaders.filterKeys { existing -> authenticationHeaders.keys.none { it.equals(existing, ignoreCase = true) } } +
                authenticationHeaders
        return updateDocument(
            additionalHeaders = authenticatedConfiguration,
            additionalQueryParameters = additionalQueryParameters,
        )
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

    public fun getOptionalWithApiKey(
        apiKeyProvider: (String) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val credentialCache = mutableMapOf<Pair<String, Set<String>>, String?>()
        val authenticationHeaders =
            sequenceOf<() -> Map<String, String>?>(
                {
                    if (!credentialCache.containsKey("Key" to setOf<String>())) {
                        credentialCache["Key" to setOf<String>()] = apiKeyProvider("Key")?.takeIf { it.isNotBlank() }
                    }
                    val credential = credentialCache["Key" to setOf<String>()]
                    require(
                        credential == null ||
                            credential.none {
                                it.code < 32 || it.code == 127
                            },
                    ) { "Authentication credentials must not contain control characters" }
                    if (credential != null) {
                        mapOf(
                            "X-API-Key" to credential!!,
                        )
                    } else {
                        null
                    }
                },
            ).firstNotNullOfOrNull { it() }
        checkNotNull(authenticationHeaders) { "Credentials for a complete security alternative are required for this operation" }
        val authenticatedConfiguration =
            additionalHeaders.filterKeys { existing -> authenticationHeaders.keys.none { it.equals(existing, ignoreCase = true) } } +
                authenticationHeaders
        return getOptional(
            additionalHeaders = authenticatedConfiguration,
            additionalQueryParameters = additionalQueryParameters,
        )
    }
}

@Suppress("unused")
public class MultipleClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     *
     */
    @Throws(ApiException::class)
    public fun getMultiple(
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/multiple"
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

    public fun getMultipleWithApiKeyAndOAuth2Token(
        apiKeyProvider: (String) -> String?,
        oauth2TokenProvider: (String, Set<String>) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val credentialCache = mutableMapOf<Pair<String, Set<String>>, String?>()
        val authenticationHeaders =
            sequenceOf<() -> Map<String, String>?>(
                {
                    if (!credentialCache.containsKey("Key" to setOf<String>())) {
                        credentialCache["Key" to setOf<String>()] = apiKeyProvider("Key")?.takeIf { it.isNotBlank() }
                    }
                    val credential = credentialCache["Key" to setOf<String>()]
                    require(
                        credential == null ||
                            credential.none {
                                it.code < 32 || it.code == 127
                            },
                    ) { "Authentication credentials must not contain control characters" }
                    if (!credentialCache.containsKey("OtherKey" to setOf<String>())) {
                        credentialCache["OtherKey" to setOf<String>()] = apiKeyProvider("OtherKey")?.takeIf { it.isNotBlank() }
                    }
                    val credentialExtra = credentialCache["OtherKey" to setOf<String>()]
                    require(
                        credentialExtra == null ||
                            credentialExtra.none {
                                it.code < 32 || it.code == 127
                            },
                    ) { "Authentication credentials must not contain control characters" }
                    if (!credentialCache.containsKey("OAuth" to setOf<String>())) {
                        credentialCache["OAuth" to setOf<String>()] =
                            oauth2TokenProvider("OAuth", setOf<String>())?.takeIf { it.isNotBlank() }
                    }
                    val credentialExtraExtra = credentialCache["OAuth" to setOf<String>()]
                    require(
                        credentialExtraExtra == null ||
                            credentialExtraExtra.none {
                                it.code < 32 || it.code == 127
                            },
                    ) { "Authentication credentials must not contain control characters" }
                    if (credential != null && credentialExtra != null && credentialExtraExtra != null) {
                        mapOf(
                            "X-API-Key" to credential!!,
                            "X-Other-Key" to credentialExtra!!,
                            "Authorization" to ("Bearer " + credentialExtraExtra!!),
                        )
                    } else {
                        null
                    }
                },
            ).firstNotNullOfOrNull { it() }
        checkNotNull(authenticationHeaders) { "Credentials for a complete security alternative are required for this operation" }
        val authenticatedConfiguration =
            additionalHeaders.filterKeys { existing -> authenticationHeaders.keys.none { it.equals(existing, ignoreCase = true) } } +
                authenticationHeaders
        return getMultiple(
            additionalHeaders = authenticatedConfiguration,
            additionalQueryParameters = additionalQueryParameters,
        )
    }
}

@Suppress("unused")
public class BearerClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     *
     */
    @Throws(ApiException::class)
    public fun getBearer(
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/bearer"
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

    public fun getBearerWithApiKeyAndBearerToken(
        apiKeyProvider: (String) -> String?,
        bearerTokenProvider: (String) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val credentialCache = mutableMapOf<Pair<String, Set<String>>, String?>()
        val authenticationHeaders =
            sequenceOf<() -> Map<String, String>?>(
                {
                    if (!credentialCache.containsKey("Key" to setOf<String>())) {
                        credentialCache["Key" to setOf<String>()] = apiKeyProvider("Key")?.takeIf { it.isNotBlank() }
                    }
                    val credential = credentialCache["Key" to setOf<String>()]
                    require(
                        credential == null ||
                            credential.none {
                                it.code < 32 || it.code == 127
                            },
                    ) { "Authentication credentials must not contain control characters" }
                    if (!credentialCache.containsKey("Bearer" to setOf<String>())) {
                        credentialCache["Bearer" to setOf<String>()] = bearerTokenProvider("Bearer")?.takeIf { it.isNotBlank() }
                    }
                    val credentialExtra = credentialCache["Bearer" to setOf<String>()]
                    require(
                        credentialExtra == null ||
                            credentialExtra.none {
                                it.code < 32 || it.code == 127
                            },
                    ) { "Authentication credentials must not contain control characters" }
                    if (credential != null && credentialExtra != null) {
                        mapOf(
                            "X-API-Key" to credential!!,
                            "Authorization" to ("Bearer " + credentialExtra!!),
                        )
                    } else {
                        null
                    }
                },
            ).firstNotNullOfOrNull { it() }
        checkNotNull(authenticationHeaders) { "Credentials for a complete security alternative are required for this operation" }
        val authenticatedConfiguration =
            additionalHeaders.filterKeys { existing -> authenticationHeaders.keys.none { it.equals(existing, ignoreCase = true) } } +
                authenticationHeaders
        return getBearer(
            additionalHeaders = authenticatedConfiguration,
            additionalQueryParameters = additionalQueryParameters,
        )
    }
}

@Suppress("unused")
public class BasicClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     *
     */
    @Throws(ApiException::class)
    public fun getBasic(
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/basic"
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

    public fun getBasicWithBasicCredentials(
        basicCredentialsProvider: (String) -> Pair<String, String>?,
        basicCredentialsCharset: Charset = Charsets.ISO_8859_1,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val credentialCache = mutableMapOf<Pair<String, Set<String>>, String?>()
        val authenticationHeaders =
            sequenceOf<() -> Map<String, String>?>(
                {
                    if (!credentialCache.containsKey("Basic" to setOf<String>())) {
                        credentialCache["Basic" to setOf<String>()] =
                            basicCredentialsProvider("Basic")?.let { (username, password) ->
                                require(
                                    ':' !in username &&
                                        (username + password).none {
                                            it.code < 32 || it.code == 127
                                        },
                                ) { "Basic credentials must not contain control characters or a colon in the username" }
                                val userPass = username + ":" + password
                                require(
                                    basicCredentialsCharset.newEncoder().canEncode(userPass),
                                ) { "Basic credentials cannot be encoded with the selected charset" }
                                Base64.getEncoder().encodeToString(userPass.toByteArray(basicCredentialsCharset))
                            }
                    }
                    val credential = credentialCache["Basic" to setOf<String>()]
                    require(
                        credential == null ||
                            credential.none {
                                it.code < 32 || it.code == 127
                            },
                    ) { "Authentication credentials must not contain control characters" }
                    if (credential != null) {
                        mapOf(
                            "Authorization" to ("Basic " + credential!!),
                        )
                    } else {
                        null
                    }
                },
            ).firstNotNullOfOrNull { it() }
        checkNotNull(authenticationHeaders) { "Credentials for a complete security alternative are required for this operation" }
        val authenticatedConfiguration =
            additionalHeaders.filterKeys { existing -> authenticationHeaders.keys.none { it.equals(existing, ignoreCase = true) } } +
                authenticationHeaders
        return getBasic(
            additionalHeaders = authenticatedConfiguration,
            additionalQueryParameters = additionalQueryParameters,
        )
    }

    /**
     *
     */
    @Throws(ApiException::class)
    public fun createBasic(
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/basic"
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
                .post(ByteArray(0).toRequestBody())
                .build()

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }

    public fun createBasicWithApiKeyAndBasicCredentials(
        apiKeyProvider: (String) -> String?,
        basicCredentialsProvider: (String) -> Pair<String, String>?,
        basicCredentialsCharset: Charset = Charsets.ISO_8859_1,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val credentialCache = mutableMapOf<Pair<String, Set<String>>, String?>()
        val authenticationHeaders =
            sequenceOf<() -> Map<String, String>?>(
                {
                    if (!credentialCache.containsKey("Basic" to setOf<String>())) {
                        credentialCache["Basic" to setOf<String>()] =
                            basicCredentialsProvider("Basic")?.let { (username, password) ->
                                require(
                                    ':' !in username &&
                                        (username + password).none {
                                            it.code < 32 || it.code == 127
                                        },
                                ) { "Basic credentials must not contain control characters or a colon in the username" }
                                val userPass = username + ":" + password
                                require(
                                    basicCredentialsCharset.newEncoder().canEncode(userPass),
                                ) { "Basic credentials cannot be encoded with the selected charset" }
                                Base64.getEncoder().encodeToString(userPass.toByteArray(basicCredentialsCharset))
                            }
                    }
                    val credential = credentialCache["Basic" to setOf<String>()]
                    require(
                        credential == null ||
                            credential.none {
                                it.code < 32 || it.code == 127
                            },
                    ) { "Authentication credentials must not contain control characters" }
                    if (!credentialCache.containsKey("Key" to setOf<String>())) {
                        credentialCache["Key" to setOf<String>()] = apiKeyProvider("Key")?.takeIf { it.isNotBlank() }
                    }
                    val credentialExtra = credentialCache["Key" to setOf<String>()]
                    require(
                        credentialExtra == null ||
                            credentialExtra.none {
                                it.code < 32 || it.code == 127
                            },
                    ) { "Authentication credentials must not contain control characters" }
                    if (credential != null && credentialExtra != null) {
                        mapOf(
                            "Authorization" to ("Basic " + credential!!),
                            "X-API-Key" to credentialExtra!!,
                        )
                    } else {
                        null
                    }
                },
            ).firstNotNullOfOrNull { it() }
        checkNotNull(authenticationHeaders) { "Credentials for a complete security alternative are required for this operation" }
        val authenticatedConfiguration =
            additionalHeaders.filterKeys { existing -> authenticationHeaders.keys.none { it.equals(existing, ignoreCase = true) } } +
                authenticationHeaders
        return createBasic(
            additionalHeaders = authenticatedConfiguration,
            additionalQueryParameters = additionalQueryParameters,
        )
    }

    /**
     *
     */
    @Throws(ApiException::class)
    public fun replaceBasic(
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/basic"
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
                .put(ByteArray(0).toRequestBody())
                .build()

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }

    public fun replaceBasicWithBasicCredentials(
        basicCredentialsProvider: (String) -> Pair<String, String>?,
        basicCredentialsCharset: Charset = Charsets.ISO_8859_1,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val credentialCache = mutableMapOf<Pair<String, Set<String>>, String?>()
        val authenticationHeaders =
            sequenceOf<() -> Map<String, String>?>(
                {
                    if (!credentialCache.containsKey("Basic" to setOf<String>())) {
                        credentialCache["Basic" to setOf<String>()] =
                            basicCredentialsProvider("Basic")?.let { (username, password) ->
                                require(
                                    ':' !in username &&
                                        (username + password).none {
                                            it.code < 32 || it.code == 127
                                        },
                                ) { "Basic credentials must not contain control characters or a colon in the username" }
                                val userPass = username + ":" + password
                                require(
                                    basicCredentialsCharset.newEncoder().canEncode(userPass),
                                ) { "Basic credentials cannot be encoded with the selected charset" }
                                Base64.getEncoder().encodeToString(userPass.toByteArray(basicCredentialsCharset))
                            }
                    }
                    val credential = credentialCache["Basic" to setOf<String>()]
                    require(
                        credential == null ||
                            credential.none {
                                it.code < 32 || it.code == 127
                            },
                    ) { "Authentication credentials must not contain control characters" }
                    if (credential != null) {
                        mapOf(
                            "Authorization" to ("Basic " + credential!!),
                        )
                    } else {
                        null
                    }
                },
                {
                    if (!credentialCache.containsKey("OtherBasic" to setOf<String>())) {
                        credentialCache["OtherBasic" to setOf<String>()] =
                            basicCredentialsProvider("OtherBasic")?.let { (username, password) ->
                                require(
                                    ':' !in username &&
                                        (username + password).none {
                                            it.code < 32 || it.code == 127
                                        },
                                ) { "Basic credentials must not contain control characters or a colon in the username" }
                                val userPass = username + ":" + password
                                require(
                                    basicCredentialsCharset.newEncoder().canEncode(userPass),
                                ) { "Basic credentials cannot be encoded with the selected charset" }
                                Base64.getEncoder().encodeToString(userPass.toByteArray(basicCredentialsCharset))
                            }
                    }
                    val credentialExtra = credentialCache["OtherBasic" to setOf<String>()]
                    require(
                        credentialExtra == null ||
                            credentialExtra.none {
                                it.code < 32 || it.code == 127
                            },
                    ) { "Authentication credentials must not contain control characters" }
                    if (credentialExtra != null) {
                        mapOf(
                            "Authorization" to ("Basic " + credentialExtra!!),
                        )
                    } else {
                        null
                    }
                },
            ).firstNotNullOfOrNull { it() }
        checkNotNull(authenticationHeaders) { "Credentials for a complete security alternative are required for this operation" }
        val authenticatedConfiguration =
            additionalHeaders.filterKeys { existing -> authenticationHeaders.keys.none { it.equals(existing, ignoreCase = true) } } +
                authenticationHeaders
        return replaceBasic(
            additionalHeaders = authenticatedConfiguration,
            additionalQueryParameters = additionalQueryParameters,
        )
    }

    /**
     *
     */
    @Throws(ApiException::class)
    public fun deleteBasic(
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/basic"
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
                .delete()
                .build()

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }
}

@Suppress("unused")
public class BasicAlternativesClient(
    private val objectMapper: ObjectMapper,
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) {
    /**
     *
     */
    @Throws(ApiException::class)
    public fun getBasicAlternatives(
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/basic-alternatives"
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

    public fun getBasicAlternativesWithBasicCredentials(
        basicCredentialsProvider: (String) -> Pair<String, String>?,
        basicCredentialsCharset: Charset = Charsets.ISO_8859_1,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val credentialCache = mutableMapOf<Pair<String, Set<String>>, String?>()
        val authenticationHeaders =
            sequenceOf<() -> Map<String, String>?>(
                {
                    if (!credentialCache.containsKey("Basic" to setOf<String>())) {
                        credentialCache["Basic" to setOf<String>()] =
                            basicCredentialsProvider("Basic")?.let { (username, password) ->
                                require(
                                    ':' !in username &&
                                        (username + password).none {
                                            it.code < 32 || it.code == 127
                                        },
                                ) { "Basic credentials must not contain control characters or a colon in the username" }
                                val userPass = username + ":" + password
                                require(
                                    basicCredentialsCharset.newEncoder().canEncode(userPass),
                                ) { "Basic credentials cannot be encoded with the selected charset" }
                                Base64.getEncoder().encodeToString(userPass.toByteArray(basicCredentialsCharset))
                            }
                    }
                    val credential = credentialCache["Basic" to setOf<String>()]
                    require(
                        credential == null ||
                            credential.none {
                                it.code < 32 || it.code == 127
                            },
                    ) { "Authentication credentials must not contain control characters" }
                    if (credential != null) {
                        mapOf(
                            "Authorization" to ("Basic " + credential!!),
                        )
                    } else {
                        null
                    }
                },
            ).firstNotNullOfOrNull { it() }
        checkNotNull(authenticationHeaders) { "Credentials for a complete security alternative are required for this operation" }
        val authenticatedConfiguration =
            additionalHeaders.filterKeys { existing -> authenticationHeaders.keys.none { it.equals(existing, ignoreCase = true) } } +
                authenticationHeaders
        return getBasicAlternatives(
            additionalHeaders = authenticatedConfiguration,
            additionalQueryParameters = additionalQueryParameters,
        )
    }

    public fun getBasicAlternativesWithBearerToken(
        bearerTokenProvider: (String) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val credentialCache = mutableMapOf<Pair<String, Set<String>>, String?>()
        val authenticationHeaders =
            sequenceOf<() -> Map<String, String>?>(
                {
                    if (!credentialCache.containsKey("Bearer" to setOf<String>())) {
                        credentialCache["Bearer" to setOf<String>()] = bearerTokenProvider("Bearer")?.takeIf { it.isNotBlank() }
                    }
                    val credential = credentialCache["Bearer" to setOf<String>()]
                    require(
                        credential == null ||
                            credential.none {
                                it.code < 32 || it.code == 127
                            },
                    ) { "Authentication credentials must not contain control characters" }
                    if (credential != null) {
                        mapOf(
                            "Authorization" to ("Bearer " + credential!!),
                        )
                    } else {
                        null
                    }
                },
            ).firstNotNullOfOrNull { it() }
        checkNotNull(authenticationHeaders) { "Credentials for a complete security alternative are required for this operation" }
        val authenticatedConfiguration =
            additionalHeaders.filterKeys { existing -> authenticationHeaders.keys.none { it.equals(existing, ignoreCase = true) } } +
                authenticationHeaders
        return getBasicAlternatives(
            additionalHeaders = authenticatedConfiguration,
            additionalQueryParameters = additionalQueryParameters,
        )
    }

    public fun getBasicAlternativesWithOAuth2Token(
        oauth2TokenProvider: (String, Set<String>) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val credentialCache = mutableMapOf<Pair<String, Set<String>>, String?>()
        val authenticationHeaders =
            sequenceOf<() -> Map<String, String>?>(
                {
                    if (!credentialCache.containsKey("OAuth" to setOf<String>("read:documents"))) {
                        credentialCache["OAuth" to setOf<String>("read:documents")] =
                            oauth2TokenProvider("OAuth", setOf<String>("read:documents"))?.takeIf { it.isNotBlank() }
                    }
                    val credential = credentialCache["OAuth" to setOf<String>("read:documents")]
                    require(
                        credential == null ||
                            credential.none {
                                it.code < 32 || it.code == 127
                            },
                    ) { "Authentication credentials must not contain control characters" }
                    if (credential != null) {
                        mapOf(
                            "Authorization" to ("Bearer " + credential!!),
                        )
                    } else {
                        null
                    }
                },
            ).firstNotNullOfOrNull { it() }
        checkNotNull(authenticationHeaders) { "Credentials for a complete security alternative are required for this operation" }
        val authenticatedConfiguration =
            additionalHeaders.filterKeys { existing -> authenticationHeaders.keys.none { it.equals(existing, ignoreCase = true) } } +
                authenticationHeaders
        return getBasicAlternatives(
            additionalHeaders = authenticatedConfiguration,
            additionalQueryParameters = additionalQueryParameters,
        )
    }

    public fun getBasicAlternativesWithApiKey(
        apiKeyProvider: (String) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val credentialCache = mutableMapOf<Pair<String, Set<String>>, String?>()
        val authenticationHeaders =
            sequenceOf<() -> Map<String, String>?>(
                {
                    if (!credentialCache.containsKey("Key" to setOf<String>())) {
                        credentialCache["Key" to setOf<String>()] = apiKeyProvider("Key")?.takeIf { it.isNotBlank() }
                    }
                    val credential = credentialCache["Key" to setOf<String>()]
                    require(
                        credential == null ||
                            credential.none {
                                it.code < 32 || it.code == 127
                            },
                    ) { "Authentication credentials must not contain control characters" }
                    if (credential != null) {
                        mapOf(
                            "X-API-Key" to credential!!,
                        )
                    } else {
                        null
                    }
                },
            ).firstNotNullOfOrNull { it() }
        checkNotNull(authenticationHeaders) { "Credentials for a complete security alternative are required for this operation" }
        val authenticatedConfiguration =
            additionalHeaders.filterKeys { existing -> authenticationHeaders.keys.none { it.equals(existing, ignoreCase = true) } } +
                authenticationHeaders
        return getBasicAlternatives(
            additionalHeaders = authenticatedConfiguration,
            additionalQueryParameters = additionalQueryParameters,
        )
    }

    /**
     *
     */
    @Throws(ApiException::class)
    public fun createBasicAlternatives(
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/basic-alternatives"
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
                .post(ByteArray(0).toRequestBody())
                .build()

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }

    /**
     *
     */
    @Throws(ApiException::class)
    public fun replaceBasicAlternatives(
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/basic-alternatives"
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
                .put(ByteArray(0).toRequestBody())
                .build()

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }

    /**
     *
     */
    @Throws(ApiException::class)
    public fun deleteBasicAlternatives(
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/basic-alternatives"
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
                .delete()
                .build()

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }

    public fun deleteBasicAlternativesWithApiKeyAndBasicCredentials(
        apiKeyProvider: (String) -> String?,
        basicCredentialsProvider: (String) -> Pair<String, String>?,
        basicCredentialsCharset: Charset = Charsets.ISO_8859_1,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val credentialCache = mutableMapOf<Pair<String, Set<String>>, String?>()
        val authenticationHeaders =
            sequenceOf<() -> Map<String, String>?>(
                {
                    if (!credentialCache.containsKey("Basic" to setOf<String>())) {
                        credentialCache["Basic" to setOf<String>()] =
                            basicCredentialsProvider("Basic")?.let { (username, password) ->
                                require(
                                    ':' !in username &&
                                        (username + password).none {
                                            it.code < 32 || it.code == 127
                                        },
                                ) { "Basic credentials must not contain control characters or a colon in the username" }
                                val userPass = username + ":" + password
                                require(
                                    basicCredentialsCharset.newEncoder().canEncode(userPass),
                                ) { "Basic credentials cannot be encoded with the selected charset" }
                                Base64.getEncoder().encodeToString(userPass.toByteArray(basicCredentialsCharset))
                            }
                    }
                    val credential = credentialCache["Basic" to setOf<String>()]
                    require(
                        credential == null ||
                            credential.none {
                                it.code < 32 || it.code == 127
                            },
                    ) { "Authentication credentials must not contain control characters" }
                    if (!credentialCache.containsKey("Key" to setOf<String>())) {
                        credentialCache["Key" to setOf<String>()] = apiKeyProvider("Key")?.takeIf { it.isNotBlank() }
                    }
                    val credentialExtra = credentialCache["Key" to setOf<String>()]
                    require(
                        credentialExtra == null ||
                            credentialExtra.none {
                                it.code < 32 || it.code == 127
                            },
                    ) { "Authentication credentials must not contain control characters" }
                    if (credential != null && credentialExtra != null) {
                        mapOf(
                            "Authorization" to ("Basic " + credential!!),
                            "X-API-Key" to credentialExtra!!,
                        )
                    } else {
                        null
                    }
                },
                {
                    if (!credentialCache.containsKey("OtherBasic" to setOf<String>())) {
                        credentialCache["OtherBasic" to setOf<String>()] =
                            basicCredentialsProvider("OtherBasic")?.let { (username, password) ->
                                require(
                                    ':' !in username &&
                                        (username + password).none {
                                            it.code < 32 || it.code == 127
                                        },
                                ) { "Basic credentials must not contain control characters or a colon in the username" }
                                val userPass = username + ":" + password
                                require(
                                    basicCredentialsCharset.newEncoder().canEncode(userPass),
                                ) { "Basic credentials cannot be encoded with the selected charset" }
                                Base64.getEncoder().encodeToString(userPass.toByteArray(basicCredentialsCharset))
                            }
                    }
                    val credentialExtraExtra = credentialCache["OtherBasic" to setOf<String>()]
                    require(
                        credentialExtraExtra == null ||
                            credentialExtraExtra.none {
                                it.code < 32 || it.code == 127
                            },
                    ) { "Authentication credentials must not contain control characters" }
                    if (!credentialCache.containsKey("OtherKey" to setOf<String>())) {
                        credentialCache["OtherKey" to setOf<String>()] = apiKeyProvider("OtherKey")?.takeIf { it.isNotBlank() }
                    }
                    val credentialExtraExtraExtra = credentialCache["OtherKey" to setOf<String>()]
                    require(
                        credentialExtraExtraExtra == null ||
                            credentialExtraExtraExtra.none {
                                it.code < 32 || it.code == 127
                            },
                    ) { "Authentication credentials must not contain control characters" }
                    if (credentialExtraExtra != null && credentialExtraExtraExtra != null) {
                        mapOf(
                            "Authorization" to ("Basic " + credentialExtraExtra!!),
                            "X-Other-Key" to credentialExtraExtraExtra!!,
                        )
                    } else {
                        null
                    }
                },
            ).firstNotNullOfOrNull { it() }
        checkNotNull(authenticationHeaders) { "Credentials for a complete security alternative are required for this operation" }
        val authenticatedConfiguration =
            additionalHeaders.filterKeys { existing -> authenticationHeaders.keys.none { it.equals(existing, ignoreCase = true) } } +
                authenticationHeaders
        return deleteBasicAlternatives(
            additionalHeaders = authenticatedConfiguration,
            additionalQueryParameters = additionalQueryParameters,
        )
    }

    /**
     *
     */
    @Throws(ApiException::class)
    public fun updateBasicAlternatives(
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
    ): ApiResponse<Unit> {
        val httpUrl: HttpUrl =
            "$baseUrl/basic-alternatives"
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
                .patch(ByteArray(0).toRequestBody())
                .build()

        return request.execute(okHttpClient, objectMapper, jacksonTypeRef())
    }
}
