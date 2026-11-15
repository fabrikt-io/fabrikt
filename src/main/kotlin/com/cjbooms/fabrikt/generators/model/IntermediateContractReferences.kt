package com.cjbooms.fabrikt.generators.model

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec

internal fun rewriteContractReferences(
    models: Collection<TypeSpec>,
    modelPackage: String,
    names: Map<String, String>,
): MutableSet<TypeSpec> {
    if (names.isEmpty()) return models.toMutableSet()

    fun reference(type: TypeName): TypeName =
        when (type) {
            is ClassName ->
                if (type.packageName == modelPackage && type.simpleNames.size == 1) {
                    names[type.simpleName]?.let {
                        ClassName(
                            modelPackage,
                            it,
                        ).copy(nullable = type.isNullable, annotations = type.annotations)
                    }
                        ?: type
                } else {
                    type
                }
            is ParameterizedTypeName ->
                type.rawType
                    .parameterizedBy(
                        type.typeArguments.map(::reference),
                    ).copy(nullable = type.isNullable, annotations = type.annotations)
            else -> type
        }
    return models
        .map { model ->
            model
                .toBuilder()
                .apply {
                    propertySpecs.clear()
                    addProperties(model.propertySpecs.map { it.toBuilder(type = reference(it.type)).build() })
                    model.primaryConstructor?.let { constructor ->
                        primaryConstructor(
                            constructor
                                .toBuilder()
                                .apply {
                                    parameters.clear()
                                    addParameters(constructor.parameters.map { it.toBuilder(type = reference(it.type)).build() })
                                }.build(),
                        )
                    }
                    funSpecs.clear()
                    addFunctions(
                        model.funSpecs.map { function ->
                            if (function.annotations.none {
                                    (it.typeName as? ClassName)?.canonicalName in
                                        setOf(
                                            "com.fasterxml.jackson.annotation.JsonAnyGetter",
                                            "com.fasterxml.jackson.annotation.JsonAnySetter",
                                        )
                                }
                            ) {
                                function
                            } else {
                                function
                                    .toBuilder()
                                    .apply {
                                        parameters.clear()
                                        addParameters(function.parameters.map { it.toBuilder(type = reference(it.type)).build() })
                                        returns(reference(function.returnType))
                                    }.build()
                            }
                        },
                    )
                }.build()
        }.toMutableSet()
}
