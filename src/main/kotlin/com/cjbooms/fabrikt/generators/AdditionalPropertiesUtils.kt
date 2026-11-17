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

internal object AdditionalPropertiesUtils {
    // A closed subtype gets no map, only a setter that rejects extra properties.
    fun AdditionalProperties.addAdditionalPropertiesToClass(
        property: PropertySpec.Builder,
        mapType: TypeName,
        parameterizedType: TypeName,
        classSettings: ClassSettings,
        classBuilder: TypeSpec.Builder,
        constructorBuilder: FunSpec.Builder,
        serializationAnnotations: SerializationAnnotations,
    ) {
        if (!serializationAnnotations.supportsAdditionalProperties) {
            throw UnsupportedOperationException("Additional properties not supported by selected serialization library")
        }
        val valueType = valueType(parameterizedType)
        if (closed) {
            classBuilder.addFunction(disabledParentSetter(valueType, serializationAnnotations))
            return
        }
        property.initializer(name)
        serializationAnnotations.addIgnore(property)
        constructorBuilder.addParameter(ParameterSpec.builder(name, mapType).defaultValue("mutableMapOf()").build())
        if (sharesParentMap(classSettings)) {
            forwardToParent(property, classBuilder)
        } else {
            val modifiers = if (classSettings.polymorphyType == ClassSettings.PolymorphyType.SUPER) listOf(KModifier.OPEN) else emptyList()
            property.addModifiers(modifiers)
            classBuilder.addFunction(getter(valueType, modifiers, serializationAnnotations))
            classBuilder.addFunction(setter(valueType, modifiers, serializationAnnotations))
        }
        classBuilder.addProperty(property.build())
    }

    private fun AdditionalProperties.valueType(parameterizedType: TypeName): TypeName =
        when (val typeInfo = typeInfo) {
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

    private fun AdditionalProperties.sharesParentMap(classSettings: ClassSettings) =
        classSettings.polymorphyType == ClassSettings.PolymorphyType.SUB && isInherited

    private fun AdditionalProperties.forwardToParent(
        property: PropertySpec.Builder,
        classBuilder: TypeSpec.Builder,
    ) {
        property.addModifiers(KModifier.OVERRIDE)
        classBuilder.addSuperclassConstructorParameter("%N = %N", name, name)
    }

    private fun AdditionalProperties.getter(
        valueType: TypeName,
        modifiers: List<KModifier>,
        serializationAnnotations: SerializationAnnotations,
    ): FunSpec =
        FunSpec
            .builder("get")
            .addModifiers(modifiers)
            .returns(Map::class.asTypeName().parameterizedBy(String::class.asTypeName(), valueType))
            .addStatement("return %N", name)
            .also { serializationAnnotations.addGetter(it) }
            .build()

    private fun AdditionalProperties.setter(
        valueType: TypeName,
        modifiers: List<KModifier>,
        serializationAnnotations: SerializationAnnotations,
    ): FunSpec =
        FunSpec
            .builder("set")
            .addModifiers(modifiers)
            .addParameter("name", String::class)
            .addParameter("value", valueType)
            .addStatement("%N[name] = value", name)
            .also { serializationAnnotations.addSetter(it) }
            .build()

    private fun disabledParentSetter(
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
}
