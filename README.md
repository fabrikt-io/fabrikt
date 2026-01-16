# Fabrikt `/ˈfa-brikt/` - Kotlin code from OpenAPI 3

* [Introduction](#introduction)
* [Features](#features)
* [Examples](#examples)
* [Usage Instructions](#usage-instructions)
  * [Command Line](#command-line)
  * [Gradle w/ custom task](#gradle-w-custom-task)
  * [Gradle w/ plugin](#gradle-w-plugin)
  * [Maven](#maven)
  * [Docker](#docker)
* [Getting the Most from Fabrikt](#getting-the-most-from-fabrikt)
* [Configuration Options](#configuration-options)
* [Original Motivation](#original-motivation)
* [Building Locally](#building-locally)
* [Publishing](#publishing)
* [Specific Features](#specific-features)

## Introduction

This library was built to take advantage of the complex modeling features available in OpenAPI 3. It generates Kotlin data classes with advanced support for features such as: 
 - Null Safety
 - Inlined schema definitions
 - Enumerations 
 - Sealed Classes
 - Polymorphism (`@JsonSubTypes`)
 - Maps of Maps of Maps
 - GraalVM Native Reflection Registration
 - Json Merge Patch (via `JsonNullable`) (add `x-json-merge-patch: true` to schemas)
 - Override Jackson Include NonNull (via `JsonInclude`) (add `x-jackson-include-non-null: true` to schemas)
 
as well as HTTP clients and controllers for a number of popular frameworks (see [Features](#features)).

More than just bootstrapping, this library can be permanently integrated into your build tool and will ensure contract and code always match, even as APIs evolve in complexity.

### Try Fabrikt Online

Try Fabrikt with your own API spec in the [Fabrikt Playground](https://try.fabrikt.io) and see how it can help you generate code for your API clients and servers.

[![Screenshot of Fabrikt Playground](https://github.com/user-attachments/assets/34ac6aff-f27e-4f48-bc53-cbb2a00e3e78)](https://try.fabrikt.io)

### Coordinates

```xml
<dependency>
  <groupId>com.cjbooms</groupId>
  <artifactId>fabrikt</artifactId>
</dependency>
```

## Features

The library currently has support for generating:

* Models
  * **Jackson** annotated **data classes**
  * **Kotlinx.serialization** annotated **data classes**
* Clients
  * **OkHttp Client (w/ Jackson Models)** - with the option for a resilience4j fault-tolerance wrapper
  * **OpenFeign** annotated client interfaces
  * **Ktor Client (w/ Jackson & Kotlin Serialization models)**
  * **Spring HTTP Interface** annotated client interfaces
* Controllers
  * **Spring MVC** annotated controller interfaces
  * **Micronaut** HTTP annotated controller interfaces
  * **Ktor server** routes and controller interfaces

## Examples

Consult test directory for OpenAPI code generation examples. 

It forms a living documentation full of [code examples](src/test/resources/examples) generated from different OpenAPI 3 permutations.

Furthermore, the [end-to-end tests](/end2end-tests) demonstrate how to integrate the library using Gradle.

## Usage Instructions

The library can be used in a variety of ways, including as a command line tool, a Gradle task, or a Maven plugin.

Please refer to [Configuration Options](#configuration-options) section for a list of available parameters.

### Command Line

Fabrikt is packaged as an executable jar, allowing it to be integrated into any build tool. 

The CLI can be invoked as follows:

```
java -jar fabrikt.jar \
    --output-directory '/tmp' \
    --base-package 'com.example' \
    --api-file '/path-to-api/open-api.yaml' \
    --targets 'client' \
    --targets 'http_models' \
    --http-client-opts resilience4j
```

### Gradle w/ custom task

Here is an example of a Gradle task with code generated to the `build/generated` directory, and execution linked to the compile task. 

```kotlin
val fabrikt: Configuration by configurations.creating

val generationDir = "$buildDir/generated"
val apiFile = "$buildDir/path-to-api/open-api.yaml"

sourceSets {
    main { java.srcDirs("$generationDir/src/main/kotlin") }
    test { java.srcDirs("$generationDir/src/test/kotlin") }
    ...
}

tasks {   
    ...
    val generateCode by creating(JavaExec::class) {
        inputs.files(apiFile)
        outputs.dir(generationDir)
        outputs.cacheIf { true }
        classpath(fabrikt)
        mainClass.set("com.cjbooms.fabrikt.cli.CodeGen")
        args = listOf(
            "--output-directory", generationDir,
            "--base-package", "com.example",
            "--api-file", apiFile,
            "--targets", "http_models",
            "--targets", "client",
            "--http-client-opts", "resilience4j"
        )
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
        dependsOn(generateCode)
    }
}

dependencies {
     fabrikt("com.cjbooms:fabrikt:+") // This should be pinned  
     ...
}
```

### Gradle w/ plugin

The [Fabrikt Gradle plugin](https://github.com/acanda/fabrikt-gradle-plugin) serves as a convenient wrapper for Fabrikt, 
allowing seamless integration of code generation into a Gradle build.

**Note:** Since the plugin is maintained separately from the Fabrikt library, please refer to the
[Configuration](https://github.com/acanda/fabrikt-gradle-plugin?tab=readme-ov-file#configuration) section of the 
plugin's README for the most up-to-date information on how to use it.

Latest version of the plugin: [![Gradle Plugin Portal Version](https://img.shields.io/gradle-plugin-portal/v/ch.acanda.gradle.fabrikt?style=flat)](https://plugins.gradle.org/plugin/ch.acanda.gradle.fabrikt)

```kotlin
plugins {
    // find latest version: https://github.com/acanda/fabrikt-gradle-plugin/releases
    id("ch.acanda.gradle.fabrikt") version "1.26.1"
}

fabrikt {
    generate("dog") {
        apiFile = file("src/main/openapi/dog.yaml")
        basePackage = "com.example.api"
    }
}
```

### Maven

The [exec-maven-plugin](http://www.mojohaus.org/exec-maven-plugin/examples/example-exec-using-plugin-dependencies.html) is capable of downloading the Fabrikt library from Maven Central and executing its main method with defined arguments.

### Docker

Fabrikt is also available as a Docker image, which can be convenient for CI/CD pipelines or environments where you prefer not to install Java directly.

The Docker image can be invoked as follows:

```bash
docker run --rm -v $(pwd):/workspace ghcr.io/fabrikt-io/fabrikt:latest \
  --output-directory '.' \
  --base-package 'com.example' \
  --api-file 'openapi.yaml' \
  --targets 'http_models'
```

The command mounts your current directory to `/workspace` in the container, where Fabrikt will read the OpenAPI specification and write the generated code.

## Getting the Most from Fabrikt

### 1. Prefer components to inline schemas
While inline schemas are perfectly valid they are not supported by Fabrikt in all circumstances.
This is especially true for request bodies and non-trivial parameters. Instead, define your schemas in the
components section of the OpenAPI spec (`components.parameters` & `components.requestBodies`). [#20](https://github.com/cjbooms/fabrikt/issues/20), [#187](https://github.com/cjbooms/fabrikt/issues/187)

### 2. Use `oneOf` with discriminator for polymorphism
`oneOf` along with the flag `SEALED_INTERFACES_FOR_ONE_OF` will generate polymorphic models with sealed interfaces.
The `discriminator` property is used by Fabrikt to determine the subtypes to be generated.

## Configuration Options

This section documents the available CLI parameters for controlling what gets generated. This documentation is generated using: `./gradlew printCodeGenUsage`

| Parameter                     | Description |
| ----------------------------- | ----------------------------- |
|   `--api-file`                | This must be a valid Open API v3 spec. All code generation will be based off this input. |
|   `--api-fragment`            | A partial Open API v3 fragment, to be combined with the primary API for code generation purposes. |
| * `--base-package`            | The base package which all code will be generated under. |
|   `--external-ref-resolution` | Specify to which degree referenced schemas from external files are included in model generation. Default: TARGETED |
|                               | CHOOSE ONE OF: |
|                               |   `TARGETED` - Generate models only for directly referenced schemas in external API files. |
|                               |   `AGGRESSIVE` - Referencing any schema in an external API file triggers generation of every external schema in that file. |
|   `--http-client-opts`        | Select the options for the http client code that you want to be generated. |
|                               | CHOOSE ANY OF: |
|                               |   `RESILIENCE4J` - Generates a fault tolerance service for the client using the following library "io.github.resilience4j:resilience4j-all:+" (only for OkHttp clients) |
|                               |   `SUSPEND_MODIFIER` - This option adds the suspend modifier to the generated client functions (only for OpenFeign clients) |
|                               |   `SPRING_RESPONSE_ENTITY_WRAPPER` - This option adds the Spring-ResponseEntity generic around the response to be able to get response headers and status (only for OpenFeign clients). |
|                               |   `SPRING_CLOUD_OPENFEIGN_STARTER_ANNOTATION` - This option adds the @FeignClient annotation to generated client interface |
|   `--http-client-target`      | Optionally select the target client that you want to be generated. Defaults to OK_HTTP |
|                               | CHOOSE ONE OF: |
|                               |   `OK_HTTP` - Generate OkHttp client. |
|                               |   `OPEN_FEIGN` - Generate OpenFeign client. |
|                               |   `SPRING_HTTP_INTERFACE` - Generate Spring HTTP Interface. |
|                               |   `KTOR` - Generate Ktor client. |
|   `--http-controller-opts`    | Select the options for the controllers that you want to be generated. |
|                               | CHOOSE ANY OF: |
|                               |   `SUSPEND_MODIFIER` - This option adds the suspend modifier to the generated controller functions |
|                               |   `AUTHENTICATION` - This option adds the authentication parameter to the generated controller functions |
|                               |   `COMPLETION_STAGE` - This option makes generated controller functions have Type CompletionStage<T> (works only with Spring Controller generator) |
|   `--http-controller-target`  | Optionally select the target framework for the controllers that you want to be generated. Defaults to Spring Controllers |
|                               | CHOOSE ONE OF: |
|                               |   `SPRING` - Generate for Spring framework. |
|                               |   `MICRONAUT` - Generate for Micronaut framework. |
|                               |   `KTOR` - Generate for Ktor server. |
|   `--http-model-opts`         | Select the options for the http models that you want to be generated. |
|                               | CHOOSE ANY OF: |
|                               |   `X_EXTENSIBLE_ENUMS` - This option treats x-extensible-enums as enums |
|                               |   `JAVA_SERIALIZATION` - This option adds Java Serializable interface to the generated models |
|                               |   `QUARKUS_REFLECTION` - This option adds @RegisterForReflection to the generated models. Requires dependency "'io.quarkus:quarkus-core:+" |
|                               |   `MICRONAUT_INTROSPECTION` - This option adds @Introspected to the generated models. Requires dependency "'io.micronaut:micronaut-core:+" |
|                               |   `MICRONAUT_REFLECTION` - This option adds @ReflectiveAccess to the generated models. Requires dependency "'io.micronaut:micronaut-core:+" |
|                               |   `MICRONAUT_SERDEABLE` - This option adds @Serdeable to the generated models. Requires dependency "'io.micronaut.serde:micronaut-serde-jackson:+" |
|                               |   `INCLUDE_COMPANION_OBJECT` - This option adds a companion object to the generated models. |
|                               |   `SEALED_INTERFACES_FOR_ONE_OF` - This option enables the generation of interfaces for discriminated oneOf types |
|                               |   `NON_NULL_MAP_VALUES` - This option makes map values non-null. The default (since v15) and most spec compliant is make map values nullable |
|   `--http-model-suffix`       | Specify custom suffix for all generated model classes. Defaults to no suffix. |
|   `--instant-library`         | Specify which Instant library to use in generated model classes for kotlinx.serialization. Default: KOTLINX_INSTANT |
|                               | CHOOSE ONE OF: |
|                               |   `KOTLINX_INSTANT` - Use `kotlinx.datetime` Instant in generated classes (default) |
|                               |   `KOTLIN_TIME_INSTANT` - Use `kotlin.time` Instant in generated classes |
|   `--openfeign-client-name`   | Specify openfeign client name for spring-cloud-starter-openfeign. Defaults to 'fabrikt-client'. |
|   `--output-directory`        | Allows the generation dir to be overridden. Defaults to current dir |
|   `--output-opts`             | Select options for the output. |
|                               | CHOOSE ANY OF: |
|                               |   `ADD_FILE_DISCLAIMER` - This option adds a disclaimer to the generated files. |
|   `--resources-path`          | Allows the path for generated resources to be overridden. Defaults to `src/main/resources` |
|   `--serialization-library`   | Specify which serialization library to use for annotations in generated model classes. Default: JACKSON |
|                               | CHOOSE ONE OF: |
|                               |   `JACKSON` - Use Jackson for serialization and deserialization |
|                               |   `KOTLINX_SERIALIZATION` - Use kotlinx.serialization for serialization and deserialization |
|   `--src-path`                | Allows the path for generated source files to be overridden. Defaults to `src/main/kotlin` |
|   `--targets`                 | Targets are the parts of the application that you want to be generated. |
|                               | CHOOSE ANY OF: |
|                               |   `HTTP_MODELS` - Jackson annotated data classes to represent the schema objects defined in the input. |
|                               |   `CONTROLLERS` - Spring / Micronaut / Ktor HTTP controllers for each of the endpoints defined in the input. |
|                               |   `CLIENT` - Simple http rest client. |
|                               |   `QUARKUS_REFLECTION_CONFIG` - This options generates the reflection-config.json file for quarkus integration projects |
|   `--type-overrides`          | Specify non-default kotlin types for certain OAS types. For example, generate `Instant` instead of `OffsetDateTime` |
|                               | CHOOSE ANY OF: |
|                               |   `DATETIME_AS_INSTANT` - Use `Instant` as the datetime type. Defaults to `OffsetDateTime` |
|                               |   `DATETIME_AS_LOCALDATETIME` - Use `LocalDateTime` as the datetime type. Defaults to `OffsetDateTime` |
|                               |   `BYTE_AS_STRING` - Ignore string format `byte` and use `String` as the type |
|                               |   `BINARY_AS_STRING` - Ignore string format `binary` and use `String` as the type |
|                               |   `URI_AS_STRING` - Ignore string format `uri` and use `String` as the type |
|                               |   `UUID_AS_STRING` - Ignore string format `uuid` and use `String` as the type |
|                               |   `DATE_AS_STRING` - Ignore string format `date` and use `String` as the type |
|                               |   `DATETIME_AS_STRING` - Ignore string format `date-time` and use `String` as the type |
|                               |   `BYTEARRAY_AS_INPUTSTREAM` - Use `InputStream` as ByteArray type. Defaults to `ByteArray` |
|   `--validation-library`      | Specify which validation library to use for annotations in generated model classes. Default: JAVAX_VALIDATION |
|                               | CHOOSE ONE OF: |
|                               |   `JAVAX_VALIDATION` - Use `javax.validation` annotations in generated model classes (default) |
|                               |   `JAKARTA_VALIDATION` - Use `jakarta.validation` annotations in generated model classes |
|                               |   `NO_VALIDATION` - Use no validation annotations in generated model classes |

## Original Motivation

The team that built the first version of this tool initially contributed to the Kotlin code generation ability in
[OpenApiTools](https://github.com/OpenAPITools/openapi-generator), but reached the limits of what could be achieved with
template-based generation. This library leverages the rich OpenAPI 3 model provided by
[KaiZen-OpenApi-Parser](https://github.com/RepreZen/KaiZen-OpenApi-Parser) and uses [Kotlin Poet](https://square.github.io/kotlinpoet/) to
programmatically construct Kotlin classes for maximum flexibility.

This project was started by engineers from [Zalando Tech](https://opensource.zalando.com/) and is battle-tested heavily in production there.

## Specific Features

### Polymorphism via `allOf`

The following example shows how `allOf` can be used to generate polymorphic Kotlin data classes. It does the following:
- A `ChildDefinition` schema defines both the discriminator mapping details and the schema for the discriminator property. In this case the discriminator property is an enumeration
- In each child schema, `allOf` is used to merge the `ChildDefinition` with the child's custom schema. This guarantees that each child schema inherits the correct discriminator property.
- In the `Responses` schema a `oneOf` lists only child schemas. This will be detected by Fabrikt and it will generate the list of parent types: `List<ChildDefinition>`

_NOTE: A new feature has been added that allows Polymorphism to be achieved using only a [discriminated oneOf](src/test/resources/examples/discriminatedOneOf). This feature makes use of Kotlin `sealed interface` and must be explicitly enabled via `--http-model-opts, SEALED_INTERFACES_FOR_ONE_OF`_

```yml
openapi: 3.0.0
components:
  schemas:
    ChildDefinition:
      type: object
      discriminator:
        propertyName: some_enum
        mapping:
          obj_one_only: '#/components/schemas/DiscriminatedChild1'
          obj_two_first: '#/components/schemas/DiscriminatedChild2'
          obj_two_second: '#/components/schemas/DiscriminatedChild2'
          obj_three: '#/components/schemas/discriminated_child_3'
      properties:
        discriminating_property:
          $ref: '#/components/schemas/ChildDiscriminator'

    ChildDiscriminator:
      type: string
      enum:
        - obj_one_only
        - obj_two_first
        - obj_two_second
        - obj_three

    DiscriminatedChild1:
      allOf:
        - $ref: '#/components/schemas/ChildDefinition'
        - type: object
          properties:
            some_prop:
              type: string

    DiscriminatedChild2:
      allOf:
        - $ref: '#/components/schemas/ChildDefinition'
        - type: object
          properties:
            some_prop:
              type: string

    discriminated_child_3:
      allOf:
        - $ref: '#/components/schemas/ChildDefinition'

    Responses:
      type: "object"
      properties:
        entries:
          type: "array"
          items:
            oneOf:
              - $ref: "#/components/schemas/DiscriminatedChild2"
              - $ref: "#/components/schemas/DiscriminatedChild2"
              - $ref: "#/components/schemas/discriminated_child_3"
```
```kotlin
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "some_enum",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(
        value = DiscriminatedChild1::class,
        name =
        "obj_one_only"
    ),
    JsonSubTypes.Type(
        value = DiscriminatedChild2::class,
        name =
        "obj_two_first"
    ),
    JsonSubTypes.Type(
        value = DiscriminatedChild2::class,
        name =
        "obj_two_second"
    ),
    JsonSubTypes.Type(value = DiscriminatedChild3::class, name = "obj_three")
)
sealed class ChildDefinition() {
    abstract val someEnum: ChildDiscriminator
}

enum class ChildDiscriminator(
    @JsonValue
    val value: String
) {
    OBJ_ONE_ONLY("obj_one_only"),

    OBJ_TWO_FIRST("obj_two_first"),

    OBJ_TWO_SECOND("obj_two_second"),

    OBJ_THREE("obj_three");

    companion object {
        private val mapping: Map<String, ChildDiscriminator> =
            values().associateBy(ChildDiscriminator::value)

        fun fromValue(value: String): ChildDiscriminator? = mapping[value]
    }
}

data class DiscriminatedChild1(
    @param:JsonProperty("some_prop")
    @get:JsonProperty("some_prop")
    val someProp: String? = null,
    @get:JsonProperty("some_enum")
    @get:NotNull
    override val someEnum: ChildDiscriminator = ChildDiscriminator.OBJ_ONE_ONLY
) : ChildDefinition()

data class DiscriminatedChild2(
    @get:JsonProperty("some_enum")
    @get:NotNull
    override val someEnum: ChildDiscriminator,
    @param:JsonProperty("some_prop")
    @get:JsonProperty("some_prop")
    val someProp: String? = null
) : ChildDefinition()

data class DiscriminatedChild3(
    @get:JsonProperty("some_enum")
    @get:NotNull
    override val someEnum: ChildDiscriminator = ChildDiscriminator.OBJ_THREE
) : ChildDefinition()

data class Responses(
    @param:JsonProperty("entries")
    @get:JsonProperty("entries")
    @get:Valid
    val entries: List<ChildDefinition>? = null
)
```

## Contributing

### Building Locally

Fabrikt is built with Gradle and requires an initialised git repository. The easiest way to build it is to clone the repo locally before executing the build command:
```
git clone git@github.com:cjbooms/fabrikt.git
cd fabrikt/
./gradlew clean build
```

### Adjusting Test Examples

A utility function is available in [GeneratedCodeAsserter.kt](src/test/kotlin/com/cjbooms/fabrikt/util/GeneratedCodeAsserter.kt) to mass change all of the code generation examples in the test resources folder. This is useful when a global change is made to the code generation logic and all of the examples need to be updated.

### Publishing

1. Go to [Release Tab](https://github.com/cjbooms/fabrikt/releases)
2. Select `Draft a new release`.
3. Set tag to a version greater than current using symantic versioning, anticipating whether the changes made could break builds.
4. Click `Generate release notes`. Ensure that the tag and release version match.
5. Click `Publish release` buttom at the bottom.

Github Actions will publish the deployment to [Sonatype Central](https://central.sonatype.com/publishing/deployments). You must then log in to Sonatype and decide to either release or drop that deployment. After 30 minutes or so, Maven Central will have indexed the promoted release.
