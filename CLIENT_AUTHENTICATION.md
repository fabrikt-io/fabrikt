# Authentication in generated clients

> **Replaces the per-operation auth helpers.** The `OPENAPI_API_KEY_AUTHENTICATION`, `OPENAPI_BEARER_AUTHENTICATION` and `OPENAPI_OAUTH2_AUTHENTICATION` options, and the `<operation>WithApiKey` / `<operation>WithBearerToken` / `<operation>WithOAuth2Token` helper functions, are removed. See [MIGRATION.md](MIGRATION.md).

Fabrikt can emit an opt-in **security declaration**: what security schemes the specification declares, what each operation effectively requires, and a pure resolver that turns caller-supplied credentials into request headers. It is contract data only.

Enable it with `--http-client-opts OPENAPI_SECURITY` (or the playground checkbox). It emits one file, `<base-package>.client.ApiSecurity.kt`, identical for every `--http-client-target`: OkHttp, OpenFeign, Spring HTTP Interface and Ktor all get the same declaration. This option concerns requests sent by generated clients; the headers used to *fetch* an API specification are configured separately with `--auth` (see [README](README.md#configuration-options)).

## What is generated

```kotlin
sealed interface SecurityScheme {
    data class Http(val scheme: String, val bearerFormat: String? = null) : SecurityScheme
    data class ApiKey(val location: Location, val name: String) : SecurityScheme
    data class OAuth2(val flows: Map<Flow, OAuth2Flow>) : SecurityScheme
    data class OpenIdConnect(val openIdConnectUrl: String) : SecurityScheme
}
enum class Location { HEADER, QUERY, COOKIE }
enum class Flow { CLIENT_CREDENTIALS, AUTHORIZATION_CODE, IMPLICIT, PASSWORD }
data class OAuth2Flow(val authorizationUrl: String?, val tokenUrl: String?, val refreshUrl: String?, val scopes: Map<String, String>)

data class SecurityRequirement(val alternatives: List<Map<String, Set<String>>>)

sealed interface Credential {
    data class Token(val token: String) : Credential
    data class Basic(val username: String, val password: String) : Credential
    data class ApiKey(val key: String) : Credential
}

object ApiSecurity {
    val schemes: Map<String, SecurityScheme>
    val requirements: Map<String, SecurityRequirement>
    val requirementsByPath: Map<String, SecurityRequirement>
    fun resolveFor(method: String, path: String, provider: (scheme: String, scopes: Set<String>) -> Credential?): Map<String, String>
}

fun SecurityRequirement.resolve(
    schemes: Map<String, SecurityScheme>,
    provider: (scheme: String, scopes: Set<String>) -> Credential?,
): Map<String, String>
```

### `schemes`

Keyed by the security-scheme component name, verbatim from `components.securitySchemes`. An OAuth2 scheme exposes only the flows the specification declares. A scheme with no credential a header resolver can produce (for example `type: mutualTLS`) is omitted with a warning, so requirements referencing it never resolve.

### `requirements` and `requirementsByPath`

`requirements` is keyed by the raw `operationId`. Keys are therefore invariant under `GROUP_BY_TAG`, `RESPONSE_MEDIA_TYPE_FUNCTIONS` and `--operation-id-transform`. An operation without an `operationId` is keyed by its uppercase verb, a space and the specification path template, for example `GET /documents/{id}`. Every operation has an entry; an operation that requires no credentials has `SecurityRequirement(emptyList())`.

`requirementsByPath` holds the same requirements keyed by `"<VERB> <path template>"`, for interceptors that only know the method and path.

Both maps use the operation's *effective* security: an operation with its own `security` uses it, an operation without one inherits the document-level `security`.

```yaml
security:
  - ApiKeyAuth: []                       # applied to every operation without its own `security`
paths:
  /documents:
    get:                                 # inherits ApiKeyAuth
      operationId: getDocuments
      responses: { '200': { description: OK } }
    post:
      operationId: createDocument
      security:                          # AND: both are needed for this one alternative
        - ApiKeyAuth: []
          OAuth: [read:documents]
      responses: { '201': { description: Created } }
  /documents/{id}:
    get:
      operationId: get_document_by_id
      security:                          # OR: two alternatives, in this order
        - BearerAuth: []
        - BasicAuth: []
      responses: { '200': { description: OK } }
    delete:
      operationId: deleteDocument
      security: []                       # explicit `[]` means no credentials
      responses: { '204': { description: Deleted } }
```

Extra entries in the security array are alternatives (OR); several schemes inside one entry must all be satisfied (AND); `{}` is an anonymous alternative and stays an `emptyMap()` inside `alternatives`.

## Resolving requirements to headers

`SecurityRequirement.resolve` iterates `alternatives` in specification order. Each alternative resolves every `(scheme, scopes)` pair through the `provider`; a `null` credential makes that alternative incomplete, and so does a credential that does not match the scheme. The first complete alternative returns its header map; if none completes, the result is `emptyMap()`.

| Scheme | Credential | Header |
| --- | --- | --- |
| `ApiKey` with `Location.HEADER` | `Credential.ApiKey` | `<name>: <key>` |
| `ApiKey` with `Location.QUERY` or `Location.COOKIE` | — | never complete |
| `Http(scheme = "bearer")`, `OAuth2`, `OpenIdConnect` | `Credential.Token` | `Authorization: Bearer <token>` |
| `Http(scheme = "basic")` | `Credential.Basic` | `Authorization: Basic <base64(username:password)>` with ISO-8859-1 |
| any other `Http` scheme | — | never complete |

An empty alternative (`{}` or `security: []`) never completes, so it resolves to no headers.

`ApiSecurity.resolveFor(method, path, provider)` looks up an equivalent requirement for a concrete request. It first tries an exact `"<METHOD> <path>"` match against `requirementsByPath`, which is what a transport-level interceptor with the request template wants. Otherwise it aligns each template of the same verb against the **trailing** segments of `path`, treating `{param}` segments as wildcards, preferring literal segments, then the fewest wildcards. Matching from the end is what makes it independent of the target's base path: strip the host and query, but keep any path prefix.

```kotlin
val headers = ApiSecurity.resolveFor("GET", "/v2/api/documents/42") { scheme, scopes ->
    when (scheme) {
        "OAuth" -> tokenService.accessToken(scopes).let(Credential::Token)
        else -> keyStore.keyFor(scheme)?.let(Credential::ApiKey)
    }
}
// matches "GET /documents/{id}" -> mapOf("Authorization" to "Bearer …")
```

Return `null` from the provider when a credential is unavailable; return the token itself, without a `Bearer ` prefix.

## Consuming the declaration

The declaration is never applied automatically: a generated client does not gain a second auth channel. Feed the resolved headers through the parameters the generated methods already have.

**OkHttp** (`additionalHeaders: Map<String, String>`):

```kotlin
client.getDocuments(additionalHeaders = ApiSecurity.requirements.getValue("getDocuments").resolve(ApiSecurity.schemes, credentials))
```

**OpenFeign** (the parameter is `@HeaderMap additionalHeaders: Map<String, String>`):

```kotlin
feignClient.getDocuments(additionalHeaders = ApiSecurity.requirements.getValue("getDocuments").resolve(ApiSecurity.schemes, credentials))
```

**Spring HTTP Interface** (the parameter is `@RequestHeader additionalHeaders: Map<String, Any>`):

```kotlin
springClient.getDocuments(additionalHeaders = ApiSecurity.requirements.getValue("getDocuments").resolve(ApiSecurity.schemes, credentials))
```

**Ktor** (`apiConfiguration: ApiConfiguration`, whose `customHeaders` is `Map<String, String>`):

```kotlin
client.getDocuments(apiConfiguration = ApiConfiguration(customHeaders = ApiSecurity.requirements.getValue("getDocuments").resolve(ApiSecurity.schemes, credentials)))
```

### Applying it to every request

To avoid passing headers per call, call `resolveFor` from the transport's own interceptor. Each of the following appends the resolved headers to the request; `credentials` is the provider shown above.

**OkHttp** — `request.method` and `request.url.encodedPath`:

```kotlin
Interceptor { chain ->
    val request = chain.request()
    val headers = ApiSecurity.resolveFor(request.method, request.url.encodedPath, credentials)
    chain.proceed(request.newBuilder().apply { headers.forEach { (name, value) -> addHeader(name, value) } }.build())
}
```

**Ktor** — a client plugin, `request.method.value` and `request.url.encodedPath`:

```kotlin
createClientPlugin("ApiSecurity") {
    onRequest { request, _ ->
        ApiSecurity.resolveFor(request.method.value, request.url.encodedPath, credentials)
            .forEach { (name, value) -> request.headers.append(name, value) }
    }
}
```

**Spring** — a `filter` (`ExchangeFilterFunction`), `request.method().name()` and `request.url().path`:

```kotlin
ExchangeFilterFunction.ofRequestProcessor { request ->
    val headers = ApiSecurity.resolveFor(request.method().name(), request.url().path, credentials)
    Mono.just(ClientRequest.from(request).headers { it.addAll(headers) }.build())
}
```

A `ClientHttpRequestInterceptor` works the same way with `request.method.name()` and `request.uri.path`. Note that a filter sees the path *with* the base URL, which the trailing-segment match tolerates; an interceptor below the base URL sees it without.

**Feign** — a `RequestInterceptor`, `template.methodMetadata().template()`:

```kotlin
RequestInterceptor { template ->
    ApiSecurity.resolveFor(template.method(), template.methodMetadata().template(), credentials)
        .forEach { (name, value) -> template.header(name, value) }
}
```

## Out of scope

The declaration describes credentials it can turn into headers. Everything else belongs to the native client:

| Concern | Where it belongs |
| --- | --- |
| HTTP Basic | OkHttp `Authenticator` + `Credentials.basic`, Feign `BasicAuthRequestInterceptor`, Spring `ExchangeFilterFunctions.basicAuthentication`, Ktor `Auth` |
| API keys in query parameters or cookies | the client's URL/query builder and cookie jar |
| Mutual TLS, proxies, custom signature schemes | the transport's own configuration |
| Token acquisition, refresh, caching, consent | the application, behind the `provider` |

`Credential.Basic` exists so that a resolved `Http(scheme = "basic")` alternative can produce a header from a caller-supplied pair, for callers that prefer to resolve everything themselves. Using the native Basic support above is generally the better choice: it also applies to requests Fabrikt does not generate.

Cookie and query API keys are mirrored in the dictionary (`Location.COOKIE`, `Location.QUERY`) so requirements stay faithful to the specification, but they never resolve to a header; such an alternative is always incomplete, and resolution falls through to the next alternative.
