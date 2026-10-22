# Fabrikt Code Generation Architecture

Developer guide for understanding the code generation pipeline and troubleshooting issues.

> **For AI Assistants**: This document helps identify where to look when debugging type resolution, model generation, or annotation issues.

## Architecture Overview

```
OpenAPI Schema → Type Resolution → Code Generation → Output
```

## Key Components

### HTTP operation discovery

`OpenApiPath.operations` exposes HTTP operations through the parser facade. It supplements Kaizen's operation list with `QUERY`, parsed as a fragment anchored to the original path's document URL and JSON pointer. Inline schemas therefore keep their source locations and schema-semantics callback, while relative references share the document's reference manager. Generators consume the same `OpenApiOperation` facade for every method.

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

### Schema Composition Relationships

`OpenApi3Document.allOfComponentSchemas(schema)` returns named components from the same document included directly or transitively through `allOf`, excluding the schema itself. Components are identified and deduplicated by their resolved JSON references, so distinct schemas with identical content remain distinct. The query follows nested composition and terminates on cycles; matching property names, properties, array items, and `oneOf`/`anyOf` alternatives do not establish an `allOf` relationship. Composed `allOf`/`oneOf` normalization exposes these relationships on its generated concrete alternatives, rather than on the union itself.

This query records schema inclusion only. It does not establish Kotlin property compatibility or cause models to implement interfaces; those generation decisions must also account for property types, nullability, and request/response filtering.

Targeted external model selection asks `OpenApi3Document.schemaNamesWithPolymorphicSubtypes()` for the named descendants required by a discriminator-bearing schema. The facade delegates this query to `SourceSchemaPolymorphism`, which follows the source model's resolved reference identities and `allOf` relationships, including aliases and cycles. The generator receives only component names; parser representations remain behind the facade.

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


## Directional model generation

`REQUEST_RESPONSE_MODELS` runs the existing model generator once per `ModelDirection`. Each pass owns its direction and external-reference discovery; neither pass changes a process-global direction. Endpoint helpers choose the request or response direction explicitly, and type resolution carries it through aliases, collection elements, property references and model-name lookup.

The model-name registry stores undirected base names and applies the supplied direction only when returning a name. Directional `SchemaInfo` and `PropertyInfo` instances resolve their types with their own direction, so cached types from the source document are never reused as directional types or overwritten by another pass. Property settings carry the same direction through composed and nested models.

Direction is a generation concern, independent of the OpenAPI version and parser representation. Schema interpretation remains behind the OpenAPI facade. These changes do not make other global generator settings or registries safe for concurrent generation.
