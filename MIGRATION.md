# Migration Guide

This guide covers breaking changes between Fabrikt major versions. Each section describes what changed and how to keep the previous behaviour.

## v27 → v28

### 1. Jackson 3 is the default serialization library

`--serialization-library` now defaults to `JACKSON_3`. Generated models use Jackson 3 annotations and the `tools.jackson.*` runtime (for example `tools.jackson.core:jackson-databind` and `tools.jackson.module:jackson-module-kotlin`).

To keep Jackson 2 output, pass `--serialization-library JACKSON_2`. The `JACKSON` value still resolves to Jackson 2 but is deprecated; prefer `JACKSON_2`.

### 2. The `RESILIENCE4J` client option was removed

`--http-client-opts RESILIENCE4J` no longer exists, and passing it fails argument parsing. The generated OkHttp circuit-breaker wrapper was removed; implement resilience (circuit breaker, retries, bulkheads) in a consumer-owned wrapper instead.

### 3. Sealed interfaces for `oneOf` are unconditional

`--http-model-opts SEALED_INTERFACES_FOR_ONE_OF` and `DISABLE_SEALED_INTERFACES_FOR_ONE_OF` were removed, and passing either fails argument parsing. Fabrikt always generates a Kotlin `sealed interface` for `oneOf`.

If you previously passed `DISABLE_SEALED_INTERFACES_FOR_ONE_OF`, remove it and update consumers to the sealed output — for example, a `oneOf` request or response body is now a sealed interface implemented by its member types.

Three generator defects that this change exposed were fixed alongside it, so some previously missing or non-compiling output is now corrected:

- inline `oneOf` request/response body schemas are emitted as sealed interfaces and implemented by their members (#766);
- a redundant no-discriminator `oneOf` over a common `allOf` parent is no longer emitted as a bogus `Any`-valued data class (#767);
- `oneOf` members whose parents are distinct but structurally identical are no longer conflated (#768).

### 4. Type-override defaults are now explicit options

The default type overrides can now be selected explicitly (for example `DATETIME_AS_OFFSETDATETIME`, `BINARY_AS_BYTEARRAY`, `URI_AS_URI`, `UUID_AS_UUID`, `ANY_AS_ANY`). No migration is required; pin them explicitly if you want to be insulated from future default changes.