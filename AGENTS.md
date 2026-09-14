# Fabrikt — Agent Instructions

Fabrikt fabricates Kotlin code from OpenAPI 3 specifications. Kotlin, JVM 17, Gradle wrapper.
User-facing docs, CLI options, and feature lists live in README.md — reference it, never copy from it.

## First-time setup

- The full build needs a JDK 17 installation. The root project targets Java 17 via `sourceCompatibility`/`targetCompatibility`, and the `playground` subproject declares `kotlin { jvmToolchain(17) }`; without JDK 17 Gradle fails with `Cannot find a Java installation ... matching: {languageVersion=17}`. Install JDK 17 and make it discoverable (e.g. `sdk install java 17.0.20-tem`).
- `CLAUDE.md` is a small pointer file; `AGENTS.md` is the single source of truth. On Windows, if symlinks are checked out as plain text, read `AGENTS.md` directly and treat `.claude/skills` as equivalent to `.agents/skills`.

## Commands

- Build: `./gradlew clean build`
- Test: `./gradlew test`
- Single test class: `./gradlew :test --tests "com.cjbooms.fabrikt.generators.ModelGeneratorTest"`
- Regenerate README CLI usage: `./gradlew printCodeGenUsage`
- After changing any `@Parameter` description in `CodeGenArgs.kt`, run `printCodeGenUsage` and replace the entire README CLI usage section with its fresh output. Never hand-edit individual table rows. Verify the diff contains only the intended change.

## Layout

- `src/main/kotlin/com/cjbooms/fabrikt/cli` — entry point and CLI args
- `.../generators` — model/controller/client generators, `PropertyUtils.kt`
- `.../model` — `KotlinTypeInfo.kt`, `PropertyInfo.kt`, annotation metadata
- `.../util` — `KaizenParserExtensions.kt`, `ModelNameRegistry.kt`
- `src/test/resources/examples/` — golden files, one directory per scenario
- `end2end-tests/` — generated sample projects compiled by the build

## Generated output stability (the #1 rule)

Generated code is consumed directly by a large Kotlin community; changing what existing specs generate breaks downstream builds. Output stability > internal elegance:
- Default to NOT changing generated output. Prefer fixes that leave existing examples byte-identical.
- Justify any example diff in the PR: why the new output is more correct and worth the downstream impact.
- Never let a change alter output for unrelated specs — review the full example diff, not just your target case.

## Golden-file tests

Tests compare generated code against `src/test/resources/examples/`; never edit those files by hand. When an output change is justified (or for mass changes like a ktlint upgrade):
1. Set `SHOULD_OVERWRITE_EXAMPLES = true` in `GeneratedCodeAsserter.kt`
2. Run tests — they rewrite the example files
3. Review the diff; it must contain ONLY the intended change
4. Set the flag back to `false` and re-run to confirm green
5. Commit the updated examples with your change

`OverWriteProtectionTest` fails the build if the flag is left `true`. The flag is a deliberate-regeneration tool, not a way to silence unexpected failures — investigate unexpected diffs before regenerating.

## Working in the codegen pipeline

Read ARCHITECTURE.md first — it maps symptoms (wrong type, missing model, missing annotations, wrong sealed interface) to the owning file. Key rules:
- Type resolution happens BEFORE generation: fix "wrong type" in `KotlinTypeInfo.from()` or `KaizenParserExtensions.safeName()`/`safeType()`; fix "missing model" in `ModelGenerator`.
- Reuse the existing `is*()` predicates in `KaizenParserExtensions.kt` (e.g. `isOneOfSuperInterface*()`) — never duplicate detection logic.
- Inline oneOf schemas get their names from `ModelNameRegistry.preRegisterInlineSchema()` / `getBySchema()`.

## Parser/generator boundary

`OpenApiModel.kt` is the *only* place a parser-representation type (`com.cjbooms.fabrikt.parser.*` schema/document types, as opposed to a stateless entry-point call) may exist anywhere in `model/`, `generators/`, or `util/`. The invariant is dependency direction, not any specific type name: every other file depends only on the `OpenApiSchema`/`OpenApi3Document` facade (established by #678); the parser's internal schema representation never flows the other way into generation. Refactoring the parser's internal types — renaming them, restructuring `GeneratorSchema`/`SourceSchema`, adding new ones — is fine and expected as the parser evolves. The facade and entry-point names below track today's code too — if `OpenApiSchema`, `OpenApi3Document`, `OpenApiDocumentParser`, `OpenApi3ParserAdapter`, or `OpenApiInputCleaner` are ever renamed, update this section; the principle survives the rename, the literal names don't.
- **`OpenApiModel.kt`'s facade classes (`OpenApiSchema`, `OpenApi3Document`, `OpenApiOperation`, etc.) may hold a parser-representation type as an unexposed backing field — `private` or `internal`, and in practice never read from outside `OpenApiModel.kt` even where `internal` visibility would technically allow it — but never returned or exposed through a public member.** This is precedent, not new: every facade class already holds a Kaizen type this way (`internal val kaizen: Schema`; `.kaizen` is touched only inside `OpenApiModel.kt`'s own `equals()` overrides today — nothing in `generators/` or elsewhere reaches it despite having visibility to). A future step that backs the facade with a native parser type (e.g. `GeneratorSchema`) the same way is the intended migration path and does not violate this boundary. Only derived, generation-relevant answers (a `String`, a `Boolean`, another facade type) may cross out of `OpenApiModel.kt`; the backing type itself never does.
- **No other file — anywhere in `model/`, `generators/`, or `util/` — may accept as a parameter, hold as a field, return, or pattern-match against a type from `com.cjbooms.fabrikt.parser` that represents parsed schema data** (today: `GeneratorSchemaDocument`, `GeneratorSchema`, `GeneratorObjectSchema`, `GeneratorBooleanSchema`, `SourceSchema`, `GeneratorSchemaTypeClassifier`, `GeneratorSchemaTypeClassification` — illustrative of the current names, not exhaustive or fixed) — even where the type is `internal`, which makes the dependency a one-way visibility violation as well as a layering one. These consumers only ever see `OpenApiSchema`/`OpenApi3Document` and the derived answers those facades expose.
- Calling a parser *entry point* — a stateless call like `OpenApiDocumentParser.parse()`, `OpenApi3ParserAdapter.parse()`, or `OpenApiInputCleaner.cleanEmptyTypes()` — to obtain raw input that is immediately wrapped in the facade (or, for `OpenApiInputCleaner`, a `JsonNode` transform with no schema-representation type involved) is fine anywhere, including outside `OpenApiModel.kt`: `SourceApi.kt` and `YamlUtils.kt` call `OpenApiDocumentParser.parse()` this way, and `ModelGenerator.generate()` calls `OpenApi3ParserAdapter.parse()` to load an externally referenced document before wrapping it the same way. None of these hold on to anything the parser returns beyond that call; that's different from holding a parser-representation type as a field, which only `OpenApiModel.kt`'s facade classes may do.
- If a generation decision needs information only a newer/native parser can supply (e.g. classifying an OpenAPI 3.1 boolean `false` schema as uninhabitable), expose that decision as a query or field on `OpenApiSchema`/`OpenApi3Document` itself, backed by whatever parser abstraction is needed — an unexposed (`private`/`internal`) field on the facade class, per the first bullet, or a callback threaded in at construction. Generation code asks the facade a question; it never inspects parser internals to answer the question itself.
- Before merging any PR that touches schema classification or property discovery, run two greps. First, `grep -rn "^import com\.cjbooms\.fabrikt\.parser\." src/main/kotlin/com/cjbooms/fabrikt/{model,generators,util}/ | grep -v OpenApiModel.kt` — this MUST show only stateless entry-point calls (`OpenApiDocumentParser`, `OpenApi3ParserAdapter`, `OpenApiInputCleaner`); any other name is a boundary violation. Second, for `OpenApiModel.kt` itself, confirm any parser-representation-type import backs a `private`/`internal` constructor property, not a `val`/`fun` with a broader visibility and not a constructor default that resolves to a public parameter — grep alone can't fully verify this, so read the surrounding declaration. Either kind of hit is the same class of regression as adding a second parallel generator: it re-opens the seam #678 closed.

## Markdown style

README.md and other Markdown docs predate a line-wrapping convention and are mostly hard-wrapped mid-sentence. Soft-wrap prose: never break a line mid-sentence; a sentence (or several) stays on one line until it ends. Do not rewrap existing paragraphs as a side effect of other edits — rewrapping is a dedicated chore.

## Test style

JUnit 5 + AssertJ. Generator tests parameterize over example directory names (`Stream<String>` in `ModelGeneratorTest.kt`) and assert with `assertThatGenerated(...).isEqualTo(...)` / `areContainedInGenerated(...)`. New behavior = new example directory under `src/test/resources/examples/` plus a parameter entry in the matching test.

## GitHub hygiene

- Creating issues: use the templates in `.github/ISSUE_TEMPLATE/`; bug reports require a minimal spec fragment and fabrikt version.
- Reproduce generation bugs with the smallest spec fragment, added as a new example directory (see Golden-file tests).
- PRs: `./gradlew build` must pass; commit updated golden files alongside code changes.

## Boundaries

- Always: run `./gradlew build` before declaring done; review the full golden-file diff before committing regenerated examples.
- Comments: do not add comments that restate what the code or a good function name already says. Reserve comments for non-obvious external behavior a name cannot convey (e.g. a third-party library's caching contract). This applies to KDoc, inline comments, and test comments alike.
- Ask first: changes that alter output for existing specs, add dependencies, change `.github/workflows/`, or touch `end2end-tests/` / `playground/` build config.
- Never: commit with `SHOULD_OVERWRITE_EXAMPLES = true`; hand-edit files under `src/test/resources/examples/`; flip the overwrite flag to silence an unexpected failure; commit secrets or signing keys.
