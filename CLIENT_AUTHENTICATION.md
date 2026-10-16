# Authentication in generated clients

Fabrikt can generate opt-in authentication helpers for header API keys, HTTP Bearer and OAuth2 security schemes declared in an OpenAPI specification. Helpers are supported by OkHttp, OpenFeign, Spring HTTP Interface and Ktor clients. They delegate to the ordinary generated client methods, which remain available.

Enable the relevant option through `--http-client-opts` or the corresponding playground control:

| Security scheme | Option | Generated helper | Provider signature |
| --- | --- | --- | --- |
| Header API key | `OPENAPI_API_KEY_AUTHENTICATION` | `<operation>WithApiKey` | `(schemeName: String) -> String?` |
| HTTP Bearer | `OPENAPI_BEARER_AUTHENTICATION` | `<operation>WithBearerToken` | `(schemeName: String) -> String?` |
| OAuth2 | `OPENAPI_OAUTH2_AUTHENTICATION` | `<operation>WithOAuth2Token` | `(schemeName: String, requiredScopes: Set<String>) -> String?` |

These options can be enabled independently or together. Without these options, generated clients do not gain authentication helpers. These options concern requests sent by generated clients; headers used to fetch an API specification are configured separately with `--auth` (see [README](README.md#configuration-options)).

## Header API keys and explicit security alternatives

Enable `OPENAPI_API_KEY_AUTHENTICATION` for `type: apiKey` schemes with `in: header`. The generated helper obtains each key from `apiKeyProvider` using the security scheme component name, and sends it under the declared header name. Key values are supplied at runtime and are never embedded in the generated sources. Global security is inherited, operation security replaces it, and `security: []` generates no helper.

Enable `OPENAPI_OAUTH2_AUTHENTICATION` or `OPENAPI_BEARER_AUTHENTICATION` as well to support alternatives and combinations with those schemes. For operations that reference API-key security, each helper represents a complete supported alternative, with required providers for its credential kinds. Separate entries are OR; multiple schemes in one entry are AND:

```yaml
components:
  securitySchemes:
    Key:
      type: apiKey
      in: header
      name: X-API-Key
    OAuth:
      type: oauth2
      flows:
        clientCredentials:
          tokenUrl: https://example.com/token
          scopes:
            read:documents: Read documents
# API key OR OAuth2
security:
  - Key: []
  - OAuth: [read:documents]
```

Choose the desired alternative explicitly:

```kotlin
client.getDocumentsWithApiKey(apiKeyProvider = { scheme -> keyStore.keyFor(scheme) })
client.getDocumentsWithOAuth2Token(oauth2TokenProvider = { scheme, scopes -> tokenService.accessToken(scheme, scopes) })
```

For an AND requirement, declare both schemes in the same entry:

```yaml
security:
  - Key: []
    OAuth: [read:documents]
```

Only the combined helper is generated for that entry:

```kotlin
client.getDocumentsWithApiKeyAndOAuth2Token(
    apiKeyProvider = { scheme -> keyStore.keyFor(scheme) },
    oauth2TokenProvider = { scheme, scopes -> tokenService.accessToken(scheme, scopes) },
)
```

The helper sends both `X-API-Key` and `Authorization: Bearer …` on the same request. Multiple API-key schemes in one entry are also supported when their header names differ. Alternatives requiring the same credential kinds share one helper, which tries complete alternatives in specification order. Scope sets remain separate; partial credentials from different alternatives are never combined. Providers return `null` or blank to indicate unavailable credentials. If no complete alternative is available, the helper throws before calling the transport, even when existing headers contain credentials. Control characters in provider values are rejected without including those values in the error message.

A helper only replaces headers it supplies, matching existing names case-insensitively and preserving other headers. Existing client/transport configuration can still add credentials; helpers do not strip those credentials or validate them. Anonymous alternatives remain available through the ordinary generated client method; explicitly choosing an authentication helper still requires its credentials. Ktor helpers copy the supplied `ApiConfiguration` and preserve its other settings.

Query and cookie API keys are not supported by this option. Unsupported schemes, disabled authentication kinds and combinations that require multiple values for the same header are skipped as a complete alternative with a warning; they never become a helper for just the supported subset. Supply the full credentials through the ordinary client method for those cases. The option does not implement token acquisition, refresh or credential validation. Existing Bearer/OAuth2 helper generation remains unchanged when this option is disabled, and operations without API-key security retain their existing helpers.

## HTTP Bearer

Declare an HTTP Bearer scheme and apply it globally or to an operation:

```yaml
components:
  securitySchemes:
    BearerAuth:
      type: http
      scheme: bearer
security:
  - BearerAuth: []
```

For an operation named `getPets`, the caller supplies the token through its generated helper:

```kotlin
client.getPetsWithBearerToken(
    bearerTokenProvider = { schemeName -> tokenStore.tokenFor(schemeName) },
)
```

The provider receives the security scheme's component name (`BearerAuth` here). Return the token itself, without a `Bearer ` prefix. A non-blank token becomes the `Authorization: Bearer <token>` header. Fabrikt does not acquire, refresh or validate the token, and `bearerFormat` does not change the provider contract.

## OAuth2 access tokens and scopes

An OAuth2 security scheme advertises available scopes through its flows. The security requirement on an operation selects the scopes needed for that operation:

```yaml
components:
  securitySchemes:
    PetOAuth:
      type: oauth2
      flows:
        clientCredentials:
          tokenUrl: https://example.com/oauth/token
          scopes:
            read:pets: Read pets
            write:pets: Update pets
paths:
  /pets:
    get:
      operationId: getPets
      security:
        - PetOAuth: [read:pets]
      responses:
        '200':
          description: OK
```

```kotlin
client.getPetsWithOAuth2Token(
    oauth2TokenProvider = { schemeName, requiredScopes ->
        tokenService.accessToken(schemeName, requiredScopes)
    },
)
```

Here, the provider receives `PetOAuth` and `setOf("read:pets")`, not every scope advertised by the flow. An empty scope requirement produces an empty set. The caller decides how to obtain a suitable token, including acquisition, consent, caching and refresh. The provider may obtain a token with additional scopes; Fabrikt neither inspects its contents nor verifies the scopes it grants.

Return the access token without a `Bearer ` prefix. The helper sends a non-blank result as `Authorization: Bearer <token>`. OpenAPI 3.0, 3.1 and 3.2 security requirements follow the same provider contract. Flow metadata, including device authorization, does not cause Fabrikt to implement a token acquisition flow.

## Security requirements and alternatives

This section describes the standalone Bearer/OAuth2 helpers. Operations with API-key security use the explicit alternatives described above when `OPENAPI_API_KEY_AUTHENTICATION` is enabled.

Operation-level security replaces global security. An operation without its own security declaration inherits the global requirements. Explicit `security: []` removes those requirements and generates no authentication helper for that operation.

Separate entries in the security array are alternatives (OR). Helpers try supported standalone alternatives in specification order and use the first non-blank token returned by the provider:

```yaml
security:
  - PetOAuth: [read:pets]
  - PetOAuth: [write:pets]
```

The OAuth2 provider is first called with `setOf("read:pets")`. If it returns `null` or a blank string, the helper tries `setOf("write:pets")`. Scope sets from separate alternatives are not combined. Bearer helpers similarly try their standalone Bearer schemes in order.

If every alternative requires a supported standalone scheme of the helper's kind, a missing token throws before delegating to the client method. An anonymous alternative makes the token optional:

```yaml
security:
  - {}
  - PetOAuth: [read:pets]
```

When a token is optional and the provider returns no usable token, the helper delegates with the existing headers or client configuration intact, including any existing `Authorization` header. When the provider returns a token, its `Authorization` header replaces an existing header with that exact name while retaining other headers. Ktor helpers apply this through a copy of the call's `ApiConfiguration`.

An alternative using another kind of scheme also makes the helper's token optional. The caller must configure credentials for that alternative through the ordinary client headers or transport. Neither helper automatically obtains API keys, Basic credentials or credentials for other security schemes.

## Combined requirements

Multiple schemes inside one requirement must all be satisfied (AND):

```yaml
security:
  - PetOAuth: [read:pets]
    ApiKey: []
```

A single token does not satisfy this combination. Without `OPENAPI_API_KEY_AUTHENTICATION`, Fabrikt warns and skips combined requirements when considering token helpers. If no supported standalone alternative remains, it generates no helper. Use the ordinary client method with the complete credentials, or add a standalone alternative only if the API actually accepts it.

Enabling the Bearer and OAuth2 options together does not combine their providers into one request. Each helper supports its own standalone alternatives. A mixed Bearer/OAuth2 requirement, or a requirement containing multiple OAuth2 schemes, is still a combined requirement and is not fulfilled automatically.

## Further authentication support

The header API-key helpers address [#721](https://github.com/fabrikt-io/fabrikt/issues/721). The OAuth2 helpers described above address [#722](https://github.com/fabrikt-io/fabrikt/issues/722). Additional authentication support is tracked in the following issues. These are proposed extensions; Fabrikt does not currently generate dedicated helpers or options for them. The issues describe the intended scope, without promising a release date or final API.

| Proposed support | Tracking issue |
| --- | --- |
| HTTP Basic authentication | [#723](https://github.com/fabrikt-io/fabrikt/issues/723) |
| OpenID Connect access tokens and operation scopes | [#724](https://github.com/fabrikt-io/fabrikt/issues/724) |
| API keys in query parameters | [#725](https://github.com/fabrikt-io/fabrikt/issues/725) |
| API keys in cookies | [#726](https://github.com/fabrikt-io/fabrikt/issues/726) |
| Explicit mutual TLS transport requirements | [#727](https://github.com/fabrikt-io/fabrikt/issues/727) |
| Hooks for additional HTTP authentication schemes | [#728](https://github.com/fabrikt-io/fabrikt/issues/728) |

Until dedicated support is available, callers can configure credentials through the ordinary client methods or their underlying transport. This requires applying the API's security requirements themselves, including alternatives and combinations.
