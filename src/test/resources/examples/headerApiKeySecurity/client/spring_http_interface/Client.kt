package examples.headerApiKeySecurity.client

import org.springframework.web.bind.`annotation`.RequestHeader
import org.springframework.web.bind.`annotation`.RequestParam
import org.springframework.web.service.`annotation`.HttpExchange
import java.nio.charset.Charset
import java.util.Base64
import kotlin.Any
import kotlin.Pair
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Map
import kotlin.collections.Set
import kotlin.text.Charsets

@Suppress("unused")
public interface DocumentsClient {
    /**
     *
     */
    @HttpExchange(
        url = "/documents",
        method = "GET",
    )
    public fun getDocuments(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )

    public fun getDocumentsWithApiKey(
        apiKeyProvider: (String) -> String?,
        additionalHeaders: Map<String, Any> = emptyMap(),
        additionalQueryParameters: Map<String, Any> = emptyMap(),
    ) {
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
        additionalHeaders: Map<String, Any> = emptyMap(),
        additionalQueryParameters: Map<String, Any> = emptyMap(),
    ) {
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
    @HttpExchange(
        url = "/documents",
        method = "POST",
    )
    public fun createDocument(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )

    public fun createDocumentWithApiKeyAndOAuth2Token(
        apiKeyProvider: (String) -> String?,
        oauth2TokenProvider: (String, Set<String>) -> String?,
        additionalHeaders: Map<String, Any> = emptyMap(),
        additionalQueryParameters: Map<String, Any> = emptyMap(),
    ) {
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
    @HttpExchange(
        url = "/documents",
        method = "PUT",
    )
    public fun replaceDocument(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )

    public fun replaceDocumentWithApiKey(
        apiKeyProvider: (String) -> String?,
        additionalHeaders: Map<String, Any> = emptyMap(),
        additionalQueryParameters: Map<String, Any> = emptyMap(),
    ) {
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
    @HttpExchange(
        url = "/documents",
        method = "DELETE",
    )
    public fun deleteDocument(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )

    /**
     *
     */
    @HttpExchange(
        url = "/documents",
        method = "PATCH",
    )
    public fun updateDocument(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )

    public fun updateDocumentWithApiKeyAndOAuth2Token(
        apiKeyProvider: (String) -> String?,
        oauth2TokenProvider: (String, Set<String>) -> String?,
        additionalHeaders: Map<String, Any> = emptyMap(),
        additionalQueryParameters: Map<String, Any> = emptyMap(),
    ) {
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
public interface OptionalClient {
    /**
     *
     */
    @HttpExchange(
        url = "/optional",
        method = "GET",
    )
    public fun getOptional(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )

    public fun getOptionalWithApiKey(
        apiKeyProvider: (String) -> String?,
        additionalHeaders: Map<String, Any> = emptyMap(),
        additionalQueryParameters: Map<String, Any> = emptyMap(),
    ) {
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
public interface MultipleClient {
    /**
     *
     */
    @HttpExchange(
        url = "/multiple",
        method = "GET",
    )
    public fun getMultiple(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )

    public fun getMultipleWithApiKeyAndOAuth2Token(
        apiKeyProvider: (String) -> String?,
        oauth2TokenProvider: (String, Set<String>) -> String?,
        additionalHeaders: Map<String, Any> = emptyMap(),
        additionalQueryParameters: Map<String, Any> = emptyMap(),
    ) {
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
public interface BearerClient {
    /**
     *
     */
    @HttpExchange(
        url = "/bearer",
        method = "GET",
    )
    public fun getBearer(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )

    public fun getBearerWithApiKeyAndBearerToken(
        apiKeyProvider: (String) -> String?,
        bearerTokenProvider: (String) -> String?,
        additionalHeaders: Map<String, Any> = emptyMap(),
        additionalQueryParameters: Map<String, Any> = emptyMap(),
    ) {
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
public interface BasicClient {
    /**
     *
     */
    @HttpExchange(
        url = "/basic",
        method = "GET",
    )
    public fun getBasic(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )

    public fun getBasicWithBasicCredentials(
        basicCredentialsProvider: (String) -> Pair<String, String>?,
        basicCredentialsCharset: Charset = Charsets.ISO_8859_1,
        additionalHeaders: Map<String, Any> = emptyMap(),
        additionalQueryParameters: Map<String, Any> = emptyMap(),
    ) {
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
    @HttpExchange(
        url = "/basic",
        method = "POST",
    )
    public fun createBasic(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )

    public fun createBasicWithApiKeyAndBasicCredentials(
        apiKeyProvider: (String) -> String?,
        basicCredentialsProvider: (String) -> Pair<String, String>?,
        basicCredentialsCharset: Charset = Charsets.ISO_8859_1,
        additionalHeaders: Map<String, Any> = emptyMap(),
        additionalQueryParameters: Map<String, Any> = emptyMap(),
    ) {
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
    @HttpExchange(
        url = "/basic",
        method = "PUT",
    )
    public fun replaceBasic(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )

    public fun replaceBasicWithBasicCredentials(
        basicCredentialsProvider: (String) -> Pair<String, String>?,
        basicCredentialsCharset: Charset = Charsets.ISO_8859_1,
        additionalHeaders: Map<String, Any> = emptyMap(),
        additionalQueryParameters: Map<String, Any> = emptyMap(),
    ) {
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
    @HttpExchange(
        url = "/basic",
        method = "DELETE",
    )
    public fun deleteBasic(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )
}

@Suppress("unused")
public interface BasicAlternativesClient {
    /**
     *
     */
    @HttpExchange(
        url = "/basic-alternatives",
        method = "GET",
    )
    public fun getBasicAlternatives(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )

    public fun getBasicAlternativesWithBasicCredentials(
        basicCredentialsProvider: (String) -> Pair<String, String>?,
        basicCredentialsCharset: Charset = Charsets.ISO_8859_1,
        additionalHeaders: Map<String, Any> = emptyMap(),
        additionalQueryParameters: Map<String, Any> = emptyMap(),
    ) {
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
        additionalHeaders: Map<String, Any> = emptyMap(),
        additionalQueryParameters: Map<String, Any> = emptyMap(),
    ) {
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
        additionalHeaders: Map<String, Any> = emptyMap(),
        additionalQueryParameters: Map<String, Any> = emptyMap(),
    ) {
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
        additionalHeaders: Map<String, Any> = emptyMap(),
        additionalQueryParameters: Map<String, Any> = emptyMap(),
    ) {
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
    @HttpExchange(
        url = "/basic-alternatives",
        method = "POST",
    )
    public fun createBasicAlternatives(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )

    /**
     *
     */
    @HttpExchange(
        url = "/basic-alternatives",
        method = "PUT",
    )
    public fun replaceBasicAlternatives(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )

    /**
     *
     */
    @HttpExchange(
        url = "/basic-alternatives",
        method = "DELETE",
    )
    public fun deleteBasicAlternatives(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )

    public fun deleteBasicAlternativesWithApiKeyAndBasicCredentials(
        apiKeyProvider: (String) -> String?,
        basicCredentialsProvider: (String) -> Pair<String, String>?,
        basicCredentialsCharset: Charset = Charsets.ISO_8859_1,
        additionalHeaders: Map<String, Any> = emptyMap(),
        additionalQueryParameters: Map<String, Any> = emptyMap(),
    ) {
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
    @HttpExchange(
        url = "/basic-alternatives",
        method = "PATCH",
    )
    public fun updateBasicAlternatives(
        @RequestHeader additionalHeaders: Map<String, Any> = emptyMap(),
        @RequestParam additionalQueryParameters: Map<String, Any> = emptyMap(),
    )
}
