package com.cjbooms.fabrikt.model

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec

sealed interface SerializationAnnotations {
    /**
     * Whether to include backing property for a polymorphic discriminator
     */
    val supportsBackingPropertyForDiscriminator: Boolean

    /**
     * Whether the annotation supports OpenAPI's additional properties
     * https://spec.openapis.org/oas/v3.0.0.html#model-with-map-dictionary-properties
     */
    val supportsAdditionalProperties: Boolean

    fun addIgnore(propertySpecBuilder: PropertySpec.Builder): PropertySpec.Builder
    fun addGetter(funSpecBuilder: FunSpec.Builder): FunSpec.Builder
    fun addSetter(funSpecBuilder: FunSpec.Builder): FunSpec.Builder
    fun addProperty(propertySpecBuilder: PropertySpec.Builder, oasKey: String, kotlinTypeInfo: KotlinTypeInfo): PropertySpec.Builder
    fun addParameter(
        propertySpecBuilder: PropertySpec.Builder,
        oasKey: String,
        isRequired: Boolean,
        typeInfo: KotlinTypeInfo,
    ): PropertySpec.Builder
    fun addClassAnnotation(typeSpecBuilder: TypeSpec.Builder): TypeSpec.Builder
    fun addBasePolymorphicTypeAnnotation(typeSpecBuilder: TypeSpec.Builder, propertyName: String): TypeSpec.Builder
    fun addPolymorphicSubTypesAnnotation(typeSpecBuilder: TypeSpec.Builder, mappings: Map<String, TypeName>): TypeSpec.Builder
    fun addSubtypeMappingAnnotation(typeSpecBuilder: TypeSpec.Builder, mapping: String): TypeSpec.Builder
    fun addEnumPropertyAnnotation(propSpecBuilder: PropertySpec.Builder): PropertySpec.Builder
    fun addEnumConstantAnnotation(enumSpecBuilder: TypeSpec.Builder, enumValue: String): TypeSpec.Builder
    fun addEnumDefaultAnnotation(enumSpecBuilder: TypeSpec.Builder, enumValue: String): TypeSpec.Builder
    fun annotateArrayElementType(elementType: TypeName, elementTypeInfo: KotlinTypeInfo): TypeName
}
