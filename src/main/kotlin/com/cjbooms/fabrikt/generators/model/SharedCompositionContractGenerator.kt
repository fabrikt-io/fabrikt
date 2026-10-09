package com.cjbooms.fabrikt.generators.model

import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.model.Destinations.modelsPackage
import com.cjbooms.fabrikt.model.OpenApi3Document
import com.cjbooms.fabrikt.model.OpenApiSchema
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.SchemaParserExtensions.isOneOfSuperInterface
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import java.util.logging.Logger

internal class SharedCompositionContractGenerator(
    private val packages: Packages,
    private val document: OpenApi3Document,
    private val primaryModels: Map<String, TypeSpec>,
) {
    private val modelsByName = primaryModels.values.associateBy { it.name }

    fun apply(models: MutableSet<TypeSpec>): MutableSet<TypeSpec> {
        val contracts = mutableListOf<TypeSpec>()
        val interfacesByModel = mutableMapOf<String, MutableList<ClassName>>()
        val overriddenProperties = mutableMapOf<String, MutableSet<String>>()
        val schemas = document.schemas.values.distinctBy { it.jsonReference }
        val ancestors =
            schemas
                .flatMap { schema ->
                    document.allOfComponentSchemas(schema) +
                        if (schema.isOneOfSuperInterface()) {
                            schema.oneOfSchemas.filter { alternative -> schemas.any { it.jsonReference == alternative.jsonReference } }
                        } else {
                            emptyList()
                        }
                }.distinctBy { it.jsonReference }
        ancestors.forEach { ancestor ->
            val baseModel = primaryModels[ancestor.jsonReference] ?: return@forEach
            if (baseModel.kind != TypeSpec.Kind.CLASS) return@forEach
            val properties = effectiveProperties(baseModel)
            if (properties.isEmpty()) return@forEach
            val name = ModelNameRegistry.compositionContractName(ancestor, checkNotNull(baseModel.name))
            val contractType = ClassName(modelsPackage(packages.base), name)
            contracts.add(
                TypeSpec
                    .interfaceBuilder(name)
                    .addProperties(properties.values.map { PropertySpec.builder(it.name, it.type.withoutTypeAnnotations()).build() })
                    .build(),
            )
            val compatibleMembers = mutableSetOf<String>()
            schemas.filter { includes(it, ancestor) }.forEach member@{ schema ->
                val model = primaryModels[schema.jsonReference] ?: return@member
                if (model.kind != TypeSpec.Kind.CLASS) return@member
                val memberProperties = effectiveProperties(model)
                val incompatible =
                    properties.values.firstOrNull { property ->
                        val memberProperty = memberProperties[property.name]
                        memberProperty == null || !isCompatible(memberProperty.type, property.type)
                    }
                if (incompatible != null) {
                    logger.warning(
                        "Omitting $name from ${model.name}: property '${incompatible.name}' cannot implement its Kotlin type. " +
                            "Align the composed property types or disable SHARED_COMPOSITION_CONTRACTS.",
                    )
                    return@member
                }
                interfacesByModel.getOrPut(checkNotNull(model.name), ::mutableListOf).add(contractType)
                overriddenProperties.getOrPut(checkNotNull(model.name), ::mutableSetOf).addAll(properties.keys)
                compatibleMembers.add(schema.jsonReference)
            }
            schemas.filter { it.isOneOfSuperInterface() && exposesContract(it, compatibleMembers) }.forEach union@{ union ->
                val model = primaryModels[union.jsonReference] ?: return@union
                interfacesByModel.getOrPut(checkNotNull(model.name), ::mutableListOf).add(contractType)
            }
        }
        return models
            .map { model ->
                val interfaces = interfacesByModel[model.name].orEmpty()
                if (interfaces.isEmpty()) {
                    model
                } else {
                    model
                        .toBuilder()
                        .apply {
                            interfaces.forEach { addSuperinterface(it) }
                            propertySpecs.clear()
                            addProperties(
                                model.propertySpecs.map { property ->
                                    if (property.name in overriddenProperties[model.name].orEmpty()) {
                                        property.toBuilder().addModifiers(KModifier.OVERRIDE).build()
                                    } else {
                                        property
                                    }
                                },
                            )
                        }.build()
                }
            }.plus(contracts)
            .toMutableSet()
    }

    private fun includes(
        schema: OpenApiSchema,
        ancestor: OpenApiSchema,
    ): Boolean =
        schema.jsonReference == ancestor.jsonReference ||
            document.allOfComponentSchemas(schema).any { it.jsonReference == ancestor.jsonReference }

    private fun exposesContract(
        schema: OpenApiSchema,
        compatibleMembers: Set<String>,
        visited: Set<String> = emptySet(),
    ): Boolean {
        if (schema.jsonReference in compatibleMembers) return true
        if (schema.jsonReference in visited || !schema.isOneOfSuperInterface()) return false
        if (primaryModels[schema.jsonReference]?.kind != TypeSpec.Kind.INTERFACE) return false
        return schema.oneOfSchemas.all { exposesContract(it, compatibleMembers, visited + schema.jsonReference) }
    }

    private fun effectiveProperties(
        model: TypeSpec,
        visited: Set<String> = emptySet(),
    ): Map<String, PropertySpec> {
        val name = model.name ?: return emptyMap()
        if (name in visited) return emptyMap()
        val parent = (model.superclass as? ClassName)?.simpleName?.let(modelsByName::get)
        return parent?.let { effectiveProperties(it, visited + name) }.orEmpty() + model.propertySpecs.associateBy { it.name }
    }

    private fun isCompatible(
        actual: TypeName,
        expected: TypeName,
    ): Boolean {
        val actualType = actual.withoutTypeAnnotations()
        val expectedType = expected.withoutTypeAnnotations()
        return (!actualType.isNullable || expectedType.isNullable) &&
            actualType.copy(nullable = false) == expectedType.copy(nullable = false)
    }

    private fun TypeName.withoutTypeAnnotations(): TypeName =
        if (this is ParameterizedTypeName) {
            rawType.parameterizedBy(typeArguments.map { it.withoutTypeAnnotations() }).copy(nullable = isNullable)
        } else {
            copy(annotations = emptyList())
        }

    companion object {
        private val logger = Logger.getGlobal()
    }
}
