package com.cjbooms.fabrikt

import com.example.apisecurity.client.ApiSecurity
import com.example.apisecurity.client.Credential
import com.example.apisecurity.client.Flow
import com.example.apisecurity.client.Location
import com.example.apisecurity.client.OAuth2Flow
import com.example.apisecurity.client.SecurityRequirement
import com.example.apisecurity.client.SecurityScheme
import com.example.apisecurity.client.resolve
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ApiSecurityTest {
    private val noCredentials: (String, Set<String>) -> Credential? = { _, _ -> null }

    @Test
    fun `scheme dictionary mirrors the specification`() {
        assertThat(ApiSecurity.schemes.keys)
            .containsExactlyInAnyOrder("ApiKeyAuth", "BasicAuth", "BearerAuth", "OAuth", "OpenId", "QueryKey")

        val apiKey = ApiSecurity.schemes.getValue("ApiKeyAuth") as SecurityScheme.ApiKey
        assertThat(apiKey.location).isEqualTo(Location.HEADER)
        assertThat(apiKey.name).isEqualTo("X-API-Key")

        val queryKey = ApiSecurity.schemes.getValue("QueryKey") as SecurityScheme.ApiKey
        assertThat(queryKey.location).isEqualTo(Location.QUERY)

        val bearer = ApiSecurity.schemes.getValue("BearerAuth") as SecurityScheme.Http
        assertThat(bearer.scheme).isEqualTo("bearer")
        assertThat(bearer.bearerFormat).isEqualTo("JWT")
        assertThat((ApiSecurity.schemes.getValue("BasicAuth") as SecurityScheme.Http).bearerFormat).isNull()

        val oidc = ApiSecurity.schemes.getValue("OpenId") as SecurityScheme.OpenIdConnect
        assertThat(oidc.openIdConnectUrl).isEqualTo("https://example.com/.well-known/openid-configuration")
    }

    @Test
    fun `oauth2 flows expose only declared flows with their urls and scopes`() {
        val oauth = ApiSecurity.schemes.getValue("OAuth") as SecurityScheme.OAuth2

        assertThat(oauth.flows.keys).containsExactlyInAnyOrder(Flow.CLIENT_CREDENTIALS, Flow.AUTHORIZATION_CODE)
        val clientCredentials: OAuth2Flow = oauth.flows.getValue(Flow.CLIENT_CREDENTIALS)
        assertThat(clientCredentials.authorizationUrl).isNull()
        assertThat(clientCredentials.tokenUrl).isEqualTo("https://example.com/oauth/token")
        assertThat(clientCredentials.refreshUrl).isNull()
        assertThat(clientCredentials.scopes)
            .containsEntry("read:documents", "Read documents")
            .containsEntry("write:documents", "Write documents")

        val authorizationCode: OAuth2Flow = oauth.flows.getValue(Flow.AUTHORIZATION_CODE)
        assertThat(authorizationCode.authorizationUrl).isEqualTo("https://example.com/oauth/authorize")
        assertThat(authorizationCode.refreshUrl).isEqualTo("https://example.com/oauth/refresh")
        assertThat(authorizationCode.scopes).containsOnlyKeys("read:documents")
    }

    @Test
    fun `requirements are keyed by operation id and record effective alternatives`() {
        assertThat(ApiSecurity.requirements.keys)
            .containsExactlyInAnyOrder(
                "getDocuments",
                "createDocument",
                "get_document_by_id",
                "deleteDocument",
                "getSession",
                "getReports",
                "getStatus",
                "GET /anonymous",
            )

        // Global inheritance.
        assertThat(ApiSecurity.requirements.getValue("getDocuments").alternatives)
            .containsExactly(mapOf("ApiKeyAuth" to emptySet<String>()))

        // Operation override with an AND requirement.
        assertThat(ApiSecurity.requirements.getValue("createDocument").alternatives)
            .containsExactly(mapOf("ApiKeyAuth" to emptySet(), "OAuth" to setOf("read:documents", "write:documents")))

        // OR requirement.
        assertThat(ApiSecurity.requirements.getValue("get_document_by_id").alternatives)
            .containsExactly(mapOf("BearerAuth" to emptySet()), mapOf("BasicAuth" to emptySet()))

        // Explicit `security: []`.
        assertThat(ApiSecurity.requirements.getValue("deleteDocument").alternatives).isEmpty()

        // Anonymous alternative stays an empty map.
        assertThat(ApiSecurity.requirements.getValue("getStatus").alternatives)
            .containsExactly(emptyMap(), mapOf("OAuth" to setOf("read:documents")))
    }

    @Test
    fun `requirements by path use the uppercase verb and the path template`() {
        assertThat(ApiSecurity.requirementsByPath.keys)
            .contains("GET /documents", "POST /documents", "GET /documents/{id}", "DELETE /documents/{id}")
        assertThat(ApiSecurity.requirementsByPath.getValue("GET /documents/{id}").alternatives)
            .containsExactly(mapOf("BearerAuth" to emptySet()), mapOf("BasicAuth" to emptySet()))
    }

    @Test
    fun `header credentials resolve to request headers`() {
        val requirement: SecurityRequirement = ApiSecurity.requirements.getValue("getDocuments")

        assertThat(requirement.resolve(ApiSecurity.schemes, noCredentials)).isEmpty()
        assertThat(requirement.resolve(ApiSecurity.schemes) { _, _ -> Credential.ApiKey("secret") })
            .containsExactlyEntriesOf(mapOf("X-API-Key" to "secret"))
    }

    @Test
    fun `alternatives are tried in specification order and require every credential`() {
        val requirement: SecurityRequirement = ApiSecurity.requirements.getValue("get_document_by_id")

        assertThat(requirement.resolve(ApiSecurity.schemes) { _, _ -> Credential.Token("t") })
            .containsExactlyEntriesOf(mapOf("Authorization" to "Bearer t"))
        assertThat(requirement.resolve(ApiSecurity.schemes) { _, _ -> Credential.Basic("user", "pass") })
            .containsExactlyEntriesOf(mapOf("Authorization" to "Basic dXNlcjpwYXNz"))
    }

    @Test
    fun `an incomplete alternative is skipped before a complete one`() {
        val requirement: SecurityRequirement = ApiSecurity.requirements.getValue("getReports")
        val requested = mutableListOf<Pair<String, Set<String>>>()

        val headers =
            requirement.resolve(ApiSecurity.schemes) { scheme, scopes ->
                requested.add(scheme to scopes)
                if (scheme == "ApiKeyAuth") Credential.ApiKey("secret") else Credential.Token("query-token")
            }

        assertThat(requested).containsExactly("QueryKey" to emptySet(), "ApiKeyAuth" to emptySet())
        assertThat(headers).containsExactlyEntriesOf(mapOf("X-API-Key" to "secret"))
    }

    @Test
    fun `oauth2 requirements pass their scopes to the provider`() {
        val requirement: SecurityRequirement = ApiSecurity.requirements.getValue("createDocument")
        val requested = mutableListOf<Pair<String, Set<String>>>()

        val headers =
            requirement.resolve(ApiSecurity.schemes) { scheme, scopes ->
                requested.add(scheme to scopes)
                if (scheme == "OAuth") Credential.Token("oauth-token") else Credential.ApiKey("secret")
            }

        assertThat(requested)
            .containsExactly(
                "ApiKeyAuth" to emptySet(),
                "OAuth" to setOf("read:documents", "write:documents"),
            )
        assertThat(headers).containsExactlyEntriesOf(mapOf("X-API-Key" to "secret", "Authorization" to "Bearer oauth-token"))
    }

    @Test
    fun `anonymous and empty requirements resolve to no headers`() {
        assertThat(ApiSecurity.requirements.getValue("deleteDocument").resolve(ApiSecurity.schemes, noCredentials)).isEmpty()
        assertThat(
            ApiSecurity.requirements
                .getValue("getStatus")
                .resolve(ApiSecurity.schemes) { _, _ -> Credential.Token("t") },
        ).containsExactlyEntriesOf(mapOf("Authorization" to "Bearer t"))
    }

    @Test
    fun `resolveFor matches an exact route and normalizes the method`() {
        assertThat(ApiSecurity.resolveFor("GET", "/documents") { _, _ -> Credential.ApiKey("secret") })
            .containsExactlyEntriesOf(mapOf("X-API-Key" to "secret"))
        assertThat(ApiSecurity.resolveFor("get", "/documents") { _, _ -> Credential.ApiKey("secret") })
            .containsExactlyEntriesOf(mapOf("X-API-Key" to "secret"))
    }

    @Test
    fun `resolveFor aligns templates against trailing segments regardless of a base path prefix`() {
        assertThat(ApiSecurity.resolveFor("GET", "/v2/api/documents/42") { _, _ -> Credential.Token("t") })
            .containsExactlyEntriesOf(mapOf("Authorization" to "Bearer t"))
        assertThat(ApiSecurity.resolveFor("GET", "/v2/api/documents") { _, _ -> Credential.ApiKey("secret") })
            .containsExactlyEntriesOf(mapOf("X-API-Key" to "secret"))
        assertThat(ApiSecurity.resolveFor("GET", "/v2/api/status") { _, _ -> Credential.Token("t") })
            .containsExactlyEntriesOf(mapOf("Authorization" to "Bearer t"))
    }

    @Test
    fun `resolveFor returns no headers for an unknown route`() {
        assertThat(ApiSecurity.resolveFor("GET", "/unknown") { _, _ -> Credential.ApiKey("secret") }).isEmpty()
        assertThat(ApiSecurity.resolveFor("GET", "/documents/42/extra") { _, _ -> Credential.ApiKey("secret") }).isEmpty()
        assertThat(ApiSecurity.resolveFor("PATCH", "/documents") { _, _ -> Credential.ApiKey("secret") }).isEmpty()
    }
}
