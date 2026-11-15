package com.cjbooms.fabrikt.generators.model

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

internal fun inheritAdditionalProperties(
    models: Collection<TypeSpec>,
    modelPackage: String,
): MutableSet<TypeSpec> {
    val byName = models.associateBy { it.name }
    val resolved = mutableMapOf<String, TypeSpec>()
    val parents = models.mapNotNull { (it.superclass as? ClassName)?.takeIf { it.packageName == modelPackage }?.simpleName }.toSet()
    val getterAnnotation = ClassName("com.fasterxml.jackson.annotation", "JsonAnyGetter")
    val setterAnnotation = ClassName("com.fasterxml.jackson.annotation", "JsonAnySetter")

    fun TypeSpec.parent(): TypeSpec? = (superclass as? ClassName)?.takeIf { it.packageName == modelPackage }?.simpleName?.let(byName::get)

    fun FunSpec.annotated(annotation: ClassName): Boolean = annotations.any { it.typeName == annotation }

    fun getter(model: TypeSpec?): FunSpec? =
        model?.funSpecs?.firstOrNull { it.annotated(getterAnnotation) } ?: model?.parent()?.let(::getter)

    fun setter(model: TypeSpec?): FunSpec? =
        model?.funSpecs?.firstOrNull { function ->
            function.annotated(setterAnnotation) && function.annotations.none { it.members.toString().contains("enabled = false") }
        } ?: model?.parent()?.let(::setter)

    fun mapProperty(model: TypeSpec?): PropertySpec? =
        model?.propertySpecs?.firstOrNull { property ->
            (property.type as? ParameterizedTypeName)?.rawType?.canonicalName == "kotlin.collections.MutableMap" &&
                property.annotations.any { (it.typeName as? ClassName)?.canonicalName == "com.fasterxml.jackson.annotation.JsonIgnore" }
        } ?: model?.parent()?.let(::mapProperty)

    fun resolve(model: TypeSpec): TypeSpec {
        val name = model.name ?: return model
        resolved[name]?.let { return it }
        val parent = model.parent()?.let(::resolve)
        val parentGetter = getter(parent)
        val parentSetter = setter(parent)
        val ownGetter = model.funSpecs.firstOrNull { it.annotated(getterAnnotation) }
        val ownSetter = model.funSpecs.firstOrNull { it.annotated(setterAnnotation) }
        val map = mapProperty(model)?.takeIf { it in model.propertySpecs }
        val parentMap = mapProperty(parent)
        val sharedMap = map != null && parentMap != null && map.type == parentMap.type && map.name == parentMap.name
        val builder = model.toBuilder()
        if (sharedMap) {
            builder.propertySpecs.remove(map)
            builder.addProperty(map.toBuilder().addModifiers(KModifier.OVERRIDE).build())
            if (parent?.primaryConstructor?.parameters?.any { it.name == parentMap.name } == true) {
                builder.addSuperclassConstructorParameter("%N = %N", parentMap.name, map.name)
            }
        } else if (parentGetter != null && ownGetter != null && map != null) {
            val inheritedNames =
                generateSequence(parent) { it.parent() }
                    .flatMap {
                        it.propertySpecs.asSequence().map { property ->
                            property.name
                        }
                    }.toSet()
            val mapName =
                generateSequence(map.name) { "${it}Extra" }.first { candidate ->
                    candidate !in inheritedNames && (candidate == map.name || model.propertySpecs.none { it.name == candidate })
                }
            if (mapName != map.name) {
                builder.propertySpecs.remove(map)
                builder.addProperty(map.toBuilder(name = mapName).initializer(mapName).build())
                model.primaryConstructor?.let { constructor ->
                    builder.primaryConstructor(
                        constructor
                            .toBuilder()
                            .apply {
                                parameters.clear()
                                addParameters(
                                    constructor.parameters.map {
                                        if (it.name ==
                                            map.name
                                        ) {
                                            it.toBuilder(name = mapName).build()
                                        } else {
                                            it
                                        }
                                    },
                                )
                            }.build(),
                    )
                }
                builder.funSpecs.remove(ownGetter)
                builder.addFunction(
                    ownGetter
                        .toBuilder()
                        .clearBody()
                        .addStatement("return %N", mapName)
                        .build(),
                )
                ownSetter?.let {
                    builder.funSpecs.remove(it)
                    builder.addFunction(
                        it
                            .toBuilder()
                            .clearBody()
                            .addStatement("%N[name] = value", mapName)
                            .build(),
                    )
                }
            }
        }
        if (parentSetter != null &&
            (ownSetter == null || ownSetter.parameters.last().type != parentSetter.parameters.last().type)
        ) {
            builder.addFunction(
                parentSetter
                    .toBuilder()
                    .apply {
                        modifiers.clear()
                        addModifiers(KModifier.OVERRIDE)
                        annotations.clear()
                        addAnnotation(AnnotationSpec.builder(setterAnnotation).addMember("enabled = false").build())
                        clearBody()
                        addStatement("super.%N(name, value)", parentSetter.name)
                    }.build(),
            )
        }
        val functions = builder.funSpecs.toList()
        builder.funSpecs.clear()
        builder.addFunctions(
            functions.map { function ->
                if (function.annotated(getterAnnotation) || function.annotated(setterAnnotation)) {
                    function
                        .toBuilder()
                        .apply {
                            if (parentGetter != null && function.annotated(getterAnnotation)) addModifiers(KModifier.OVERRIDE)
                            if (parentSetter != null &&
                                function.annotated(setterAnnotation) &&
                                function.parameters.last().type == parentSetter.parameters.last().type
                            ) {
                                addModifiers(KModifier.OVERRIDE)
                            }
                            if (name in parents && KModifier.OVERRIDE !in modifiers) addModifiers(KModifier.OPEN)
                        }.build()
                } else {
                    function
                }
            },
        )
        if (name in parents && map != null && !sharedMap) {
            val ownMap = builder.propertySpecs.first { it.type == map.type && it.annotations == map.annotations }
            builder.propertySpecs.remove(ownMap)
            builder.addProperty(ownMap.toBuilder().addModifiers(KModifier.OPEN).build())
        }
        return builder.build().also { resolved[name] = it }
    }
    return models.map(::resolve).toMutableSet()
}
