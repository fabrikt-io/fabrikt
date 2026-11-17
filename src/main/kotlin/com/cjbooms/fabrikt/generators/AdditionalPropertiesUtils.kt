package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.generators.TypeFactory.maybeMakeMapValueNullable
import com.cjbooms.fabrikt.model.KotlinTypeInfo
import com.cjbooms.fabrikt.model.PropertyInfo.AdditionalProperties
import com.cjbooms.fabrikt.model.SerializationAnnotations
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName

/** Generates the `additionalProperties` map of a model with its any-getter and any-setter. */
internal object AdditionalPropertiesUtils {
    fun valueType(
        additionalProperties: AdditionalProperties,
        parameterizedType: TypeName,
    ): TypeName =
        when (val typeInfo = additionalProperties.typeInfo) {
            is KotlinTypeInfo.MapTypeAdditionalProperties ->
                Map::class
                    .asTypeName()
                    .parameterizedBy(String::class.asTypeName(), parameterizedType.maybeMakeMapValueNullable())
            is KotlinTypeInfo.SimpleTypedAdditionalProperties ->
                typeInfo.parameterizedType.modelKClass
                    .asTypeName()
                    .maybeMakeMapValueNullable()
            else -> parameterizedType
        }.maybeMakeMapValueNullable()

    fun add(
        additionalProperties: AdditionalProperties,
        property: PropertySpec.Builder,
        mapType: TypeName,
        valueType: TypeName,
        polymorphyType: ClassSettings.PolymorphyType,
        classBuilder: TypeSpec.Builder,
        constructorBuilder: FunSpec.Builder,
        serializationAnnotations: SerializationAnnotations,
    ) {
        property.initializer(additionalProperties.name)
        serializationAnnotations.addIgnore(property)
        constructorBuilder.addParameter(
            ParameterSpec.builder(additionalProperties.name, mapType).defaultValue("mutableMapOf()").build(),
        )
        if (additionalProperties.sharesParentMap(polymorphyType)) {
            forwardToParent(additionalProperties, property, classBuilder)
        } else {
            val modifiers = if (polymorphyType == ClassSettings.PolymorphyType.SUPER) listOf(KModifier.OPEN) else emptyList()
            property.addModifiers(modifiers)
            classBuilder.addFunction(getter(additionalProperties, valueType, modifiers, serializationAnnotations))
            classBuilder.addFunction(setter(additionalProperties, valueType, modifiers, serializationAnnotations))
        }
    }

    /** Overrides the parent's setter so that unknown properties are rejected instead of captured. */
    fun disabledParentSetter(
        valueType: TypeName,
        serializationAnnotations: SerializationAnnotations,
    ): FunSpec =
        FunSpec
            .builder("set")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("name", String::class)
            .addParameter("value", valueType)
            .addStatement("super.set(name, value)")
            .also { serializationAnnotations.addDisabledSetter(it) }
            .build()

    // The parent's open accessors already use this map, so a subtype only passes it up.
    private fun forwardToParent(
        additionalProperties: AdditionalProperties,
        property: PropertySpec.Builder,
        classBuilder: TypeSpec.Builder,
    ) {
        property.addModifiers(KModifier.OVERRIDE)
        classBuilder.addSuperclassConstructorParameter("%N = %N", additionalProperties.name, additionalProperties.name)
    }

    private fun AdditionalProperties.sharesParentMap(polymorphyType: ClassSettings.PolymorphyType) =
        polymorphyType == ClassSettings.PolymorphyType.SUB && isInherited

    private fun getter(
        additionalProperties: AdditionalProperties,
        valueType: TypeName,
        modifiers: List<KModifier>,
        serializationAnnotations: SerializationAnnotations,
    ): FunSpec =
        FunSpec
            .builder("get")
            .addModifiers(modifiers)
            .returns(Map::class.asTypeName().parameterizedBy(String::class.asTypeName(), valueType))
            .addStatement("return %N", additionalProperties.name)
            .also { serializationAnnotations.addGetter(it) }
            .build()

    private fun setter(
        additionalProperties: AdditionalProperties,
        valueType: TypeName,
        modifiers: List<KModifier>,
        serializationAnnotations: SerializationAnnotations,
    ): FunSpec =
        FunSpec
            .builder("set")
            .addModifiers(modifiers)
            .addParameter("name", String::class)
            .addParameter("value", valueType)
            .addStatement("%N[name] = value", additionalProperties.name)
            .also { serializationAnnotations.addSetter(it) }
            .build()
}
