# Fabrikt Code Generation Architecture Guide

Quick reference for understanding the code generation pipeline and where to look for issues.

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
  
- **`KotlinTypeInfo.kt`**
  - `from()` - Converts OpenAPI schema to Kotlin type
  - `getParameterizedTypeForArray()` - Resolves array element types
  
- **`ModelNameRegistry.kt`**
  - Manages type name registration and lookup
  - Calls `safeName()` to generate names

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
| Property has wrong type | `KotlinTypeInfo.from()`, `safeName()` |
| Type doesn't exist | `safeName()`, `ModelGenerator` filters |
| Missing annotations | `PropertyUtils.addToClass()` |
| Wrong sealed interface | `findOneOfSuperInterface()` |

## Key Principle

**Type resolution happens BEFORE generation.**
- Fix "wrong type" issues in `KotlinTypeInfo` or `safeName()`
- Fix "missing model" issues in `ModelGenerator`
- Don't duplicate existing detection logic - reuse `is*()` functions

