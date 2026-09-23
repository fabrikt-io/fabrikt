package com.cjbooms.fabrikt.cli

import com.cjbooms.fabrikt.VersionCatalogLibraries
import com.cjbooms.fabrikt.generators.JakartaAnnotations
import com.cjbooms.fabrikt.generators.JavaxValidationAnnotations
import com.cjbooms.fabrikt.generators.NoValidationAnnotations
import com.cjbooms.fabrikt.generators.ValidationAnnotations
import com.cjbooms.fabrikt.generators.dependencies.DependencyNotation
import com.cjbooms.fabrikt.generators.dependencies.DependencyNotation.Companion.dependencyOf
import com.cjbooms.fabrikt.generators.dependencies.DependencyNotation.Scope.COMPILE
import com.cjbooms.fabrikt.generators.dependencies.MaybeRequiringDependencies
import com.cjbooms.fabrikt.model.JacksonAnnotations
import com.cjbooms.fabrikt.model.KotlinxSerializationAnnotations
import com.cjbooms.fabrikt.model.SerializationAnnotations

enum class CodeGenerationType(
    val description: String,
) {
    HTTP_MODELS(
        "Jackson annotated data classes to represent the schema objects defined in the input.",
    ),
    CONTROLLERS(
        "Spring / Micronaut / Ktor HTTP controllers for each of the endpoints defined in the input.",
    ),
    CLIENT(
        "Simple http rest client.",
    ),
    QUARKUS_REFLECTION_CONFIG(
        "This options generates the reflection-config.json file for quarkus integration projects",
    ),
    ;

    override fun toString() = "`${super.toString()}` - $description"
}

enum class ClientCodeGenOptionType(
    private val description: String,
) : MaybeRequiringDependencies {
    RESILIENCE4J(
        "Generates a fault tolerance service for the client using the following library \"io.github.resilience4j:resilience4j-all:+\" (only for OkHttp clients)",
    ) {
        override fun requiredDependencies(): List<DependencyNotation> =
            listOf(
                COMPILE.dependencyOf(VersionCatalogLibraries.resilience4j_circuitbreaker),
            )
    },
    SUSPEND_MODIFIER("This option adds the suspend modifier to the generated client functions (only for OpenFeign clients)") {
        override fun requiredDependencies(): List<DependencyNotation> =
            listOf(
                COMPILE.dependencyOf(VersionCatalogLibraries.kotlinx_coroutines_core),
            )
    },
    SPRING_RESPONSE_ENTITY_WRAPPER(
        "This option adds the Spring-ResponseEntity generic around the response to be able to get response headers and status (only for OpenFeign clients).",
    ) {
        override fun requiredDependencies(): List<DependencyNotation> =
            listOf(
                COMPILE.dependencyOf(VersionCatalogLibraries.spring_web),
            )
    },
    SPRING_CLOUD_OPENFEIGN_STARTER_ANNOTATION("This option adds the @FeignClient annotation to generated client interface") {
        override fun requiredDependencies(): List<DependencyNotation> =
            listOf(
                COMPILE.dependencyOf(VersionCatalogLibraries.spring_cloud_starter_openfeign),
            )
    },
    GROUP_BY_TAG("This option groups clients based on the first tag rather than paths"),
    OKHTTP_NON_NULL_RESPONSE_PAYLOADS(
        "This option makes ApiResponse.data non-null. Responses declared with a body must return one: a missing body, or one that deserializes to null, throws ApiException. An operation that declares both a body response and an empty success response (e.g. 200 and 204) throws on the empty success. Binary responses return an empty ByteArray for an empty body (only for OkHttp clients)",
    ),
    DYNAMIC_BASE_URL(
        "This option makes ApiConfiguration.basePath empty, allowing you to set the base URL at runtime (only for Ktor clients)",
    ),
    ;

    override fun toString() = "`${super.toString()}` - $description"

    override fun requiredDependencies(): List<DependencyNotation> = listOf()

    companion object {
        const val DEFAULT_OPEN_FEIGN_CLIENT_NAME = "fabrikt-client"
    }
}

enum class ClientCodeGenTargetType(
    val description: String,
) : MaybeRequiringDependencies {
    OK_HTTP("Generate OkHttp client.") {
        override fun requiredDependencies(): List<DependencyNotation> =
            listOf(
                COMPILE.dependencyOf(VersionCatalogLibraries.okhttp),
            )
    },
    OPEN_FEIGN("Generate OpenFeign client.") {
        override fun requiredDependencies(): List<DependencyNotation> =
            listOf(
                COMPILE.dependencyOf(VersionCatalogLibraries.feign_core),
            )
    },
    SPRING_HTTP_INTERFACE("Generate Spring HTTP Interface.") {
        override fun requiredDependencies(): List<DependencyNotation> =
            listOf(
                COMPILE.dependencyOf(VersionCatalogLibraries.spring_web),
            )
    },
    KTOR("Generate Ktor client.") {
        override fun requiredDependencies(): List<DependencyNotation> =
            listOf(
                COMPILE.dependencyOf(VersionCatalogLibraries.ktor_client_core),
                COMPILE.dependencyOf(VersionCatalogLibraries.ktor_client_content_negotiation),
            )
    },
    ;

    override fun toString() = "`${super.toString()}` - $description"

    override fun requiredDependencies(): List<DependencyNotation> = listOf()

    companion object {
        val default = OK_HTTP
    }
}

enum class ModelCodeGenOptionType(
    val description: String,
) : MaybeRequiringDependencies {
    X_EXTENSIBLE_ENUMS("This option treats x-extensible-enums as enums"),
    JAVA_SERIALIZATION("This option adds Java Serializable interface to the generated models"),
    QUARKUS_REFLECTION(
        "This option adds @RegisterForReflection to the generated models. Requires dependency \"'io.quarkus:quarkus-core:+\"",
    ) {
        override fun requiredDependencies(): List<DependencyNotation> =
            listOf(
                COMPILE.dependencyOf(VersionCatalogLibraries.quarkus_core),
            )
    },
    MICRONAUT_INTROSPECTION(
        "This option adds @Introspected to the generated models. Requires dependency \"'io.micronaut:micronaut-core:+\"",
    ) {
        override fun requiredDependencies(): List<DependencyNotation> =
            listOf(
                COMPILE.dependencyOf(VersionCatalogLibraries.micronaut_core),
            )
    },
    MICRONAUT_REFLECTION(
        "This option adds @ReflectiveAccess to the generated models. Requires dependency \"'io.micronaut:micronaut-core:+\"",
    ) {
        override fun requiredDependencies(): List<DependencyNotation> =
            listOf(
                COMPILE.dependencyOf(VersionCatalogLibraries.micronaut_core),
            )
    },
    MICRONAUT_SERDEABLE(
        "This option adds @Serdeable to the generated models. Requires dependency \"'io.micronaut.serde:micronaut-serde-jackson:+\"",
    ) {
        override fun requiredDependencies(): List<DependencyNotation> =
            listOf(
                COMPILE.dependencyOf(VersionCatalogLibraries.micronaut_serde_jackson),
            )
    },
    INCLUDE_COMPANION_OBJECT("This option adds a companion object to the generated models."),

    @Deprecated("Sealed interfaces are enabled by default in v26+. Use DISABLE_SEALED_INTERFACES_FOR_ONE_OF to disable.")
    SEALED_INTERFACES_FOR_ONE_OF(
        "This option is deprecated. Sealed interfaces are enabled by default in v26+. Use DISABLE_SEALED_INTERFACES_FOR_ONE_OF to disable.",
    ),
    DISABLE_SEALED_INTERFACES_FOR_ONE_OF("This option disables the default sealed interfaces for oneOf behavior in v26+"),
    NON_NULL_MAP_VALUES(
        "This option makes map values non-null. The default (since v15) and most spec compliant is make map values nullable",
    ),
    FAULT_TOLERANT_ENUMS(
        "This option adds an UNRECOGNIZED enum entry as a fallback for unmapped values, preventing deserialization exceptions. If jackson is used, the deserialization option **READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE** will need to be enabled as well",
    ),
    FAULT_TOLERANT_OPEN_ENUMS(
        "This option converts the \"open enum\" pattern (an `anyOf` combining a string enum with an open `type: string`) into a fault-tolerant enum, i.e. an enum carrying the declared values plus an UNRECOGNIZED fallback, instead of collapsing the type to a plain `String`. Behaves like FAULT_TOLERANT_ENUMS for the affected enums",
    ),
    ;

    override fun toString() = "`${super.toString()}` - $description"

    override fun requiredDependencies(): List<DependencyNotation> = listOf()
}

enum class ControllerCodeGenOptionType(
    val description: String,
) : MaybeRequiringDependencies {
    SUSPEND_MODIFIER("This option adds the suspend modifier to the generated controller functions") {
        override fun requiredDependencies(): List<DependencyNotation> =
            listOf(
                COMPILE.dependencyOf(VersionCatalogLibraries.kotlinx_coroutines_core),
            )
    },
    AUTHENTICATION("This option adds the authentication parameter to the generated controller functions"),
    GROUP_BY_TAG("This option groups controllers based on the first tag rather than paths"),
    COMPLETION_STAGE(
        "This option makes generated controller functions have Type CompletionStage<T> (works only with Spring Controller generator). Can be overridden per operation using the OpenAPI extension `x-async-support: true|false`",
    ),
    SSE_EMITTER("This option makes generated controller functions have Type SseEmitter (works only with Spring Controller generator)"), ;

    override fun toString() = "`${super.toString()}` - $description"

    override fun requiredDependencies(): List<DependencyNotation> = emptyList()
}

enum class ControllerCodeGenTargetType(
    val description: String,
) : MaybeRequiringDependencies {
    SPRING("Generate for Spring framework.") {
        override fun requiredDependencies(): List<DependencyNotation> =
            listOf(
                COMPILE.dependencyOf(VersionCatalogLibraries.spring_webmvc),
            )
    },
    MICRONAUT("Generate for Micronaut framework.") {
        override fun requiredDependencies(): List<DependencyNotation> =
            listOf(
                COMPILE.dependencyOf(VersionCatalogLibraries.micronaut_http),
                COMPILE.dependencyOf(VersionCatalogLibraries.micronaut_security),
            )
    },
    KTOR("Generate for Ktor server.") {
        override fun requiredDependencies(): List<DependencyNotation> =
            listOf(
                COMPILE.dependencyOf(VersionCatalogLibraries.ktor_server_core),
            )
    },
    ;

    override fun toString() = "`${super.toString()}` - $description"

    override fun requiredDependencies(): List<DependencyNotation> = emptyList()

    companion object {
        val default = SPRING
    }
}

enum class CodeGenTypeOverride(
    val description: String,
) : MaybeRequiringDependencies {
    DATETIME_AS_INSTANT("Use `Instant` as the datetime type. Defaults to `OffsetDateTime`"),
    DATETIME_AS_LOCALDATETIME("Use `LocalDateTime` as the datetime type. Defaults to `OffsetDateTime`"),
    BYTE_AS_STRING("Ignore string format `byte` and use `String` as the type"),
    BINARY_AS_STRING("Ignore string format `binary` and use `String` as the type"),
    URI_AS_STRING("Ignore string format `uri` and use `String` as the type"),
    UUID_AS_STRING("Ignore string format `uuid` and use `String` as the type"),
    DATE_AS_STRING("Ignore string format `date` and use `String` as the type"),
    DATETIME_AS_STRING("Ignore string format `date-time` and use `String` as the type"),
    BYTEARRAY_AS_INPUTSTREAM("Use `InputStream` as ByteArray type. Defaults to `ByteArray`"),
    ANY_AS_JSONELEMENT(
        "Use `kotlinx.serialization.json.JsonElement` for untyped (any) schemas and `JsonObject` for untyped objects. Requires the KOTLINX_SERIALIZATION serialization library. Defaults to `Any`",
    ),
    ;

    override fun toString() = "`${super.toString()}` - $description"

    override fun requiredDependencies(): List<DependencyNotation> = emptyList()
}

enum class OutputOptionType(
    val description: String,
) {
    ADD_FILE_DISCLAIMER("This option adds a disclaimer to the generated files."),
    ADD_GENERATED_ANNOTATION("Annotate generated types and top-level functions with javax.annotation.processing.Generated."),
    ;

    override fun toString() = "`${super.toString()}` - $description"
}

enum class ValidationLibrary(
    val description: String,
    val annotations: ValidationAnnotations,
) : MaybeRequiringDependencies {
    JAVAX_VALIDATION(
        "Use `javax.validation` annotations in generated model classes",
        JavaxValidationAnnotations,
    ) {
        override fun requiredDependencies(): List<DependencyNotation> =
            listOf(
                COMPILE.dependencyOf(VersionCatalogLibraries.validation_api),
            )
    },
    JAKARTA_VALIDATION(
        "Use `jakarta.validation` annotations in generated model classes (default)",
        JakartaAnnotations,
    ) {
        override fun requiredDependencies(): List<DependencyNotation> =
            listOf(
                COMPILE.dependencyOf(VersionCatalogLibraries.jakarta_validation_api),
            )
    },
    NO_VALIDATION("Use no validation annotations in generated model classes", NoValidationAnnotations),
    ;

    override fun toString() = "`${super.toString()}` - $description"

    override fun requiredDependencies(): List<DependencyNotation> = emptyList()

    companion object {
        val default = JAKARTA_VALIDATION
    }
}

enum class InstantLibrary(
    val description: String,
) : MaybeRequiringDependencies {
    KOTLINX_INSTANT("Use `kotlinx.datetime` Instant in generated classes (default)") {
        override fun requiredDependencies(): List<DependencyNotation> =
            listOf(
                COMPILE.dependencyOf(VersionCatalogLibraries.kotlinx_datetime),
            )
    },
    KOTLIN_TIME_INSTANT("Use `kotlin.time` Instant in generated classes"),
    ;

    override fun toString() = "`${super.toString()}` - $description"

    override fun requiredDependencies(): List<DependencyNotation> = emptyList()

    companion object {
        val default = KOTLINX_INSTANT
    }
}

enum class ExternalReferencesResolutionMode(
    val description: String,
) {
    TARGETED("Generate models only for directly referenced schemas in external API files."),
    AGGRESSIVE("Referencing any schema in an external API file triggers generation of every external schema in that file."),
    ;

    override fun toString() = "`${super.toString()}` - $description"

    companion object {
        val default = TARGETED
    }
}

enum class SerializationLibrary(
    val description: String,
    val serializationAnnotations: SerializationAnnotations,
) : MaybeRequiringDependencies {
    JACKSON("Use Jackson 2 for serialization and deserialization", JacksonAnnotations) {
        override fun requiredDependencies(): List<DependencyNotation> =
            listOf(
                COMPILE.dependencyOf(VersionCatalogLibraries.jackson_core),
                COMPILE.dependencyOf(VersionCatalogLibraries.jackson_databind),
                COMPILE.dependencyOf(VersionCatalogLibraries.jackson_module_kotlin),
            )
    },
    JACKSON_3("Use Jackson 3 for serialization and deserialization", JacksonAnnotations) {
        override fun requiredDependencies(): List<DependencyNotation> =
            listOf(
                COMPILE.dependencyOf(VersionCatalogLibraries.jackson3_databind),
                COMPILE.dependencyOf(VersionCatalogLibraries.jackson3_module_kotlin),
            )
    },
    KOTLINX_SERIALIZATION(
        "Use kotlinx.serialization for serialization and deserialization",
        KotlinxSerializationAnnotations,
    ) {
        override fun requiredDependencies(): List<DependencyNotation> =
            listOf(
                COMPILE.dependencyOf(VersionCatalogLibraries.kotlinx_serialization_json),
            )
    },
    ;

    val isJackson: Boolean
        get() = this == JACKSON || this == JACKSON_3

    override fun toString() = "`${super.toString()}` - $description"

    override fun requiredDependencies(): List<DependencyNotation> = emptyList()

    companion object {
        val default = JACKSON
    }
}

enum class JacksonNullabilityMode(
    val description: String,
) {
    NONE("Default Jackson behaviour"),
    ENFORCE_OPTIONAL_NON_NULL("Omit null values for optional non-null fields"),
    ENFORCE_REQUIRED_NULLABLE("Include null values for required nullable fields"),
    STRICT("Combines `ENFORCE_OPTIONAL_NON_NULL` and `ENFORCE_REQUIRED_NULLABLE` for strictest contract enforcement"),
    ;

    override fun toString() = "`${super.toString()}` - $description"

    companion object {
        val default = NONE
    }
}

enum class DependenciesGenerationMode(
    val description: String,
) {
    NONE("No extra (dependencies) file is generated."),
    GRADLE_NOTATION(
        "Dependencies file is generated with gradle notation (expected to be placed inside dependencies {} block in build.gradle(.kts)",
    ),
    MAVEN_NOTATION(
        "Dependencies file is generated with maven notation (expected to be placed inside <dependencies></dependencies> tags in pom.xml)",
    ),
    ;

    override fun toString() = "`${super.toString()}` - $description"
}
