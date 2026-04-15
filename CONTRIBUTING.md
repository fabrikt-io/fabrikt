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

## Tests

Fabrikt uses generated code snapshots as its primary test mechanism. If your change affects code generation output, regenerate the examples:

```bash
./gradlew test -Doverwrite.sources=true
```

Commit the updated snapshots alongside your change — they form the living documentation of what fabrikt produces.

## Pull Request Checklist

- [ ] `./gradlew build` passes
- [ ] Generated snapshots updated if output changed
- [ ] New behaviour covered by a test or snapshot

## Reporting Issues

Please use [GitHub Issues](https://github.com/fabrikt-io/fabrikt/issues). Include your OpenAPI spec snippet and the fabrikt version where possible.
