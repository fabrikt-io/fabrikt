# Contributing to Fabrikt

Thanks for taking the time to contribute!

## Getting Started

```bash
git clone git@github.com:fabrikt-io/fabrikt.git
cd fabrikt/
./gradlew clean build
```

## Making Changes

- **Bug fixes and small improvements** — open a PR directly.
- **New features or significant changes** — open an issue first to discuss the approach.

## Tests and Living Documentation

Fabrikt's tests work by comparing generated code against committed example files in `src/test/resources/examples/`. These examples are the living documentation of what fabrikt produces — they show exactly what gets generated for a given spec and set of options.

If your change affects code generation output, you need to update the committed example files. Rather than editing them by hand, set the flag in [`GeneratedCodeAsserter.kt`](src/test/kotlin/com/cjbooms/fabrikt/util/GeneratedCodeAsserter.kt):

```kotlin
const val SHOULD_OVERWRITE_EXAMPLES = true
```

Run the tests — they will rewrite the example files automatically. Then flip the flag back to `false`, run again to confirm everything passes, and commit the updated examples alongside your change. (A test exists specifically to prevent accidentally committing with the flag left on.)

## Pull Request Checklist

- [ ] `./gradlew build` passes
- [ ] Example files updated and committed if generated output changed

## Reporting Issues

Please use [GitHub Issues](https://github.com/fabrikt-io/fabrikt/issues). For generation bugs, a minimal reproduction helps enormously:

1. Reduce your spec to the smallest fragment that triggers the problem and paste it in the issue
2. Share the [playground](https://try.fabrikt.io) URL with the relevant options selected (the URL encodes the flags)
3. Describe the expected output vs what was actually generated
