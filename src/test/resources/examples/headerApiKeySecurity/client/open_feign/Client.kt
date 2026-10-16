package examples.headerApiKeySecurity.client

import feign.HeaderMap
import feign.QueryMap
import feign.RequestLine
import kotlin.String
import kotlin.Suppress
import kotlin.collections.Map
import kotlin.collections.Set

@Suppress("unused")
public interface DocumentsClient {
    /**
     *
     */
    @RequestLine("GET /documents")
    public fun getDocuments(
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    )

    public fun getDocumentsWithApiKey(
        apiKeyProvider: (String) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
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
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
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
    @RequestLine("POST /documents")
    public fun createDocument(
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    )

    public fun createDocumentWithApiKeyAndOAuth2Token(
        apiKeyProvider: (String) -> String?,
        oauth2TokenProvider: (String, Set<String>) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
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
    @RequestLine("PUT /documents")
    public fun replaceDocument(
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    )

    public fun replaceDocumentWithApiKey(
        apiKeyProvider: (String) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
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
    @RequestLine("DELETE /documents")
    public fun deleteDocument(
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    )

    /**
     *
     */
    @RequestLine("PATCH /documents")
    public fun updateDocument(
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    )

    public fun updateDocumentWithApiKeyAndOAuth2Token(
        apiKeyProvider: (String) -> String?,
        oauth2TokenProvider: (String, Set<String>) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
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
    @RequestLine("GET /optional")
    public fun getOptional(
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    )

    public fun getOptionalWithApiKey(
        apiKeyProvider: (String) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
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
    @RequestLine("GET /multiple")
    public fun getMultiple(
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    )

    public fun getMultipleWithApiKeyAndOAuth2Token(
        apiKeyProvider: (String) -> String?,
        oauth2TokenProvider: (String, Set<String>) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
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
    @RequestLine("GET /bearer")
    public fun getBearer(
        @HeaderMap additionalHeaders: Map<String, String> = emptyMap(),
        @QueryMap additionalQueryParameters: Map<String, String> = emptyMap(),
    )

    public fun getBearerWithApiKeyAndBearerToken(
        apiKeyProvider: (String) -> String?,
        bearerTokenProvider: (String) -> String?,
        additionalHeaders: Map<String, String> = emptyMap(),
        additionalQueryParameters: Map<String, String> = emptyMap(),
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
