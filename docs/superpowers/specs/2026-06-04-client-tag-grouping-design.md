# Client Tag Grouping Design

## Goal

Add an opt-in client code generation option that groups generated client types by the first OpenAPI tag instead of by path segment, so specs that are already organized by controller/tag can produce one client type per controller-style tag.

## Problem

Fabrikt already supports controller generation grouped by first tag through `ControllerCodeGenOptionType.GROUP_BY_TAG`, but client generators still group endpoints by path segment. That means a spec that is intentionally organized by tags can still produce multiple generated client types for what users consider one controller.

For open-source consumers, changing the default behavior would be risky. The feature should therefore be opt-in and consistent with the controller grouping semantics that already exist in the codebase.

## Non-Goals

1. Do not change the default client grouping behavior.
2. Do not introduce different grouping rules for different client targets.
3. Do not change method naming, signatures, annotations, or support library generation except where grouping changes which methods land in which client type.

## Proposed Design

### Public API

Add a new `ClientCodeGenOptionType.GROUP_BY_TAG` option.

Behavior:

1. When the option is absent, client generators continue grouping by path segment exactly as they do today.
2. When the option is present, client generators group routes by the first operation tag.
3. If a route has no operation tags, it falls back to the current path-segment grouping.
4. If operations on the same path do not share the same primary tag, the grouping remains deterministic by reusing the existing "alphabetically first verb wins" rule already used for controllers.

This keeps the user-facing API small and aligns clients with the controller feature users already understand.

### Internal Design

Reuse the existing grouping utilities in `KaizenParserExtensions.kt` instead of duplicating tag routing logic in each client generator.

The preferred shape is:

1. keep the existing path-segment grouping helper,
2. keep the existing first-tag grouping helper,
3. add a small client-facing selector that chooses between them based on `Set<ClientCodeGenOptionType>`.

That selector can live either:

1. in `KaizenParserExtensions.kt` as another extension/helper, or
2. in `ClientGeneratorUtils.kt` as a thin wrapper over existing OpenAPI grouping helpers.

The important constraint is that all client generators use the same selection logic.

### Affected Generators

Apply the option consistently to all current client generators that group by path segment:

1. `OkHttpSimpleClientGenerator`
2. `OkHttpEnhancedClientGenerator`
3. `OpenFeignInterfaceGenerator`
4. `SpringHttpInterfaceGenerator`
5. `KtorClientGenerator`

The change should be limited to the collection step that decides `(resourceName, paths)` for each generated client type. The downstream function generation logic should remain intact.

### CLI and Playground Surface

Because client options already flow through `ClientCodeGenOptionType`, the new enum value should automatically surface through existing option plumbing, but it still needs to be verified across:

1. CLI parsing via `--http-client-opts`
2. generated usage text / README docs
3. playground settings serialization and deserialization

If any of those paths require explicit test updates, add them.

## Testing Strategy

Use the existing `src/test/resources/examples/tagGrouping/api.yaml` fixture to validate the new behavior instead of creating a new example schema.

### Generator Tests

Update generator tests so the `tagGrouping` example is exercised with `ClientCodeGenOptionType.GROUP_BY_TAG` for:

1. OkHttp simple client
2. OkHttp resilience4j client
3. OpenFeign client
4. Spring HTTP Interface client
5. Ktor client

For each target, add expected generated output fixtures under the existing example structure so assertions continue to compare full generated source.

### Regression Coverage

Keep the current non-tag-grouped fixtures and tests unchanged so the default path-based behavior remains covered.

This should prove both:

1. the new option changes grouping when enabled, and
2. existing users see no change when the option is not enabled.

## Documentation

Update the client option documentation so the new option is visible anywhere `ClientCodeGenOptionType` is surfaced, especially the README section generated from CLI usage.

The documentation wording should mirror the controller option closely, but be explicit that it applies to generated clients rather than controllers.

## Risks and Mitigations

### Risk: mixed tags on the same path

Mitigation: reuse the controller grouping rule so behavior is deterministic and already familiar inside the codebase.

### Risk: inconsistent support across client targets

Mitigation: implement the grouping switch once and apply it to every current client generator in the same change.

### Risk: accidental breaking change

Mitigation: keep the feature opt-in and preserve all current tests for default behavior.

## Expected Outcome

With `ClientCodeGenOptionType.GROUP_BY_TAG` enabled, Fabrikt will generate client types aligned with OpenAPI tags/controllers instead of path segments, while preserving the current default behavior and keeping implementation complexity low by reusing existing grouping logic.
