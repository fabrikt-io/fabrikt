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

**Purpose:** insulate code generation from the OpenAPI parser implementation, so the parser can be migrated or swapped without generation code changing. Everything below exists to serve that one goal.

**The rule:** a parser-representation type (`com.cjbooms.fabrikt.parser.*` schema/document types — today `GeneratorSchemaDocument`, `GeneratorSchema`, `GeneratorObjectSchema`, `GeneratorBooleanSchema`, `SourceSchema`, `GeneratorSchemaTypeClassifier`, `GeneratorSchemaTypeClassification`; illustrative, not exhaustive) may exist in exactly one place: as a private/internal, never-exposed backing field on a facade class. Today that's `OpenApiModel.kt` (`OpenApiSchema`, `OpenApi3Document`, `OpenApiOperation`, etc.) — if the facade is ever renamed or split across files, this section describes the facade layer, not that literal file; update the name, not the principle. Every other file in `model/`, `generators/`, and `util/` depends only on the facade's public members and MUST NEVER accept, hold, return, or pattern-match a parser-representation type itself, even where it's `internal` (a one-way visibility violation, not just a layering one).

**Precedent, not new:** every facade class already holds a Kaizen type this way (`internal val kaizen: Schema`) — `.kaizen` is read only inside the facade's own `equals()`/`hashCode()` overrides, never from `generators/` despite `internal` visibility technically allowing it. A future step backing the facade with a native parser type (e.g. `GeneratorSchema`) the same way is the intended migration path, not a violation.

**Two things that are fine, and why they're different from the rule above:**
- Calling a stateless parser *entry point* — `OpenApiDocumentParser.parse()`, `OpenApi3ParserAdapter.parse()`, `OpenApiInputCleaner.cleanEmptyTypes()` — to obtain input immediately wrapped in the facade, from anywhere (`SourceApi.kt`, `YamlUtils.kt`, `ModelGenerator.generate()` all do this today). None of these hold on to what the parser returns beyond that one call; that's the distinction from holding a parser-representation type as a field.
- A generation decision that only a newer/native parser can answer (e.g. classifying an OpenAPI 3.1 boolean `false` schema as uninhabitable) belongs as a query or field on the facade class itself — backed by an unexposed field or a callback bound at construction. Generation code asks the facade a question; it never inspects parser internals to answer the question itself.

**Before merging** any PR touching schema classification or property discovery, run: `grep -rn "^import com\.cjbooms\.fabrikt\.parser\." src/main/kotlin/com/cjbooms/fabrikt/{model,generators,util}/ | grep -v OpenApiModel.kt` — every hit MUST be one of the three entry points named above; anything else is the regression this section exists to prevent, the same class of mistake as adding a second parallel generator (#678's seam, reopened). For `OpenApiModel.kt` itself, a parser-representation-type import is fine only if it backs a private/internal constructor property that never surfaces on a public member — grep can't verify that distinction, so read the declaration.

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
