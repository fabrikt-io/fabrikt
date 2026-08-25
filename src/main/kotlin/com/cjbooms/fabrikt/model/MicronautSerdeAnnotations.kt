package com.cjbooms.fabrikt.model

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

/**
 * Serialization annotations for Micronaut Serialization.
 * Delegates to JacksonAnnotations for most behavior due to compatibility with Jackson annotations.
 */
object MicronautSerdeAnnotations : SerializationAnnotations by JacksonAnnotations {
    private val SERDEABLE = ClassName("io.micronaut.serde.annotation", "Serdeable")

    override fun addClassAnnotation(typeSpecBuilder: TypeSpec.Builder) = typeSpecBuilder.addAnnotation(SERDEABLE)

    /**
     * Micronaut Serde has built-in enum serialization and does not require @JsonValue annotation.
     * Returning the builder as-is (no-op).
     */
    override fun addEnumPropertyAnnotation(propSpecBuilder: PropertySpec.Builder) = propSpecBuilder
}
