package com.cjbooms.fabrikt.generators.model

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec

internal class CompositionContractProperties(
    private val modelPackage: String,
    models: Collection<TypeSpec>,
) {
    private val modelsByName = models.associateBy { it.name }

    fun effectiveProperties(
        model: TypeSpec,
        visited: Set<String> = emptySet(),
    ): Map<String, PropertySpec> {
        val name = model.name ?: return emptyMap()
        if (name in visited) return emptyMap()
        val parent = (model.superclass as? ClassName)?.generatedModel()
        return parent?.let { effectiveProperties(it, visited + name) }.orEmpty() + model.propertySpecs.associateBy { it.name }
    }

    fun isCompatible(
        actual: TypeName,
        expected: TypeName,
        visited: Set<Pair<TypeName, TypeName>> = emptySet(),
    ): Boolean {
        val actualType = actual.withoutTypeAnnotations()
        val expectedType = expected.withoutTypeAnnotations()
        if (actualType.isNullable && !expectedType.isNullable) return false
        val source = actualType.copy(nullable = false)
        val target = expectedType.copy(nullable = false)
        if (source == target || target == ANY) return true
        val pair = source to target
        if (pair in visited) return false
        val next = visited + pair
        if (source is ParameterizedTypeName && target is ParameterizedTypeName && source.rawType == target.rawType) {
            return when (source.rawType.canonicalName) {
                "kotlin.collections.List", "kotlin.collections.Set", "kotlin.collections.Collection",
                "kotlin.collections.Iterable", "kotlin.sequences.Sequence",
                -> isCompatible(source.typeArguments.single(), target.typeArguments.single(), next)
                "kotlin.collections.Map" ->
                    source.typeArguments.first() == target.typeArguments.first() &&
                        isCompatible(source.typeArguments.last(), target.typeArguments.last(), next)
                else -> false
            }
        }
        val model = (source as? ClassName)?.generatedModel() ?: return false
        return (listOf(model.superclass) + model.superinterfaces.keys).any { isCompatible(it, target, next) }
    }

    private fun ClassName.generatedModel(): TypeSpec? =
        if (packageName == modelPackage && simpleNames.size == 1) modelsByName[simpleName] else null
}

internal fun TypeName.withoutTypeAnnotations(): TypeName =
    if (this is ParameterizedTypeName) {
        rawType.parameterizedBy(typeArguments.map { it.withoutTypeAnnotations() }).copy(nullable = isNullable)
    } else {
        copy(annotations = emptyList())
    }
