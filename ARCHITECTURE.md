# Fabrikt Code Generation Architecture

Developer guide for understanding the code generation pipeline and troubleshooting issues.

> **For AI Assistants**: This document helps identify where to look when debugging type resolution, model generation, or annotation issues.

## Architecture Overview

```
OpenAPI Schema → Type Resolution → Code Generation → Output
```

## Key Components

### Type Resolution (Determines WHAT type to use)
- **`KaizenParserExtensions.kt`**
  - `safeName()` - Determines the name/type to use for a schema
  - `safeType()` - Classifies schema as object/array/string/etc
  - `is*()` functions - Detection predicates for special cases
  - `findOneOfSuperInterface()` - Finds sealed interfaces a schema should implement
  
- **`KotlinTypeInfo.kt`**
  - `from()` - Converts OpenAPI schema to Kotlin type
  - `getParameterizedTypeForArray()` - Resolves array element types
  
- **`ModelNameRegistry.kt`**
  - Manages type name registration and lookup
  - Calls `safeName()` to generate names
  - `preRegisterInlineSchema()` - Pre-computes names for inline oneOf schemas
  - `getBySchema()` - Retrieves pre-registered names by schema reference

### Code Generation (Creates Kotlin code)
- **`ModelGenerator.kt`**
  - Decides WHICH models to generate
  - Creates sealed interfaces, data classes, enums
  
- **`PropertyUtils.kt`**
  - Adds properties to classes
  - Applies annotations based on `PolymorphyType`

### Annotations
- **`JacksonAnnotations.kt`** / **`JacksonMetadata.kt`**
  - `@JsonProperty`, `@JsonSubTypes`, etc.

## Common Issues

| Symptom | Where to Look |
|---------|---------------|
| Property has wrong type | `KotlinTypeInfo.from()`, `getParameterizedTypeForArray()`, `safeName()` |
| Type doesn't exist / wrong name | `safeName()`, `ModelGenerator` filters, `ModelNameRegistry` |
| Missing annotations | `PropertyUtils.addToClass()` |
| Wrong sealed interface | `findOneOfSuperInterface()`, `isOneOfSuperInterface*()` |
| Class implements wrong interface | `findOneOfSuperInterface()`, `ModelNameRegistry.getBySchema()` |

## Key Principles

**Type resolution happens BEFORE generation.**
- Fix "wrong type" issues in `KotlinTypeInfo` or `safeName()`
- Fix "missing model" issues in `ModelGenerator`
- Don't duplicate existing detection logic - reuse `is*()` functions

**Inline oneOf schemas need special handling.**
- Inline oneOf schemas don't have a `name` property
- `ModelNameRegistry.preRegisterInlineSchema()` pre-computes their names during `findOneOfSuperInterface()`
- `ModelNameRegistry.getBySchema()` retrieves the pre-computed name during generation
- This ensures the interface name matches what's used in `implements` clauses

