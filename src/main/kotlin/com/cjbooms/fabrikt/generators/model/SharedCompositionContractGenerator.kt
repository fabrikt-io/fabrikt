package com.cjbooms.fabrikt.generators.model

import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.model.Destinations.modelsPackage
import com.cjbooms.fabrikt.model.OasType
import com.cjbooms.fabrikt.model.OpenApiSchema
import com.cjbooms.fabrikt.model.SchemaCompositionRelationships
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.SchemaParserExtensions.isOneOfSuperInterface
import com.cjbooms.fabrikt.util.SchemaParserExtensions.safeType
import com.squareup.kotlinpoet.ANY
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
    componentSchemas: Collection<OpenApiSchema>,
    private val primaryModels: Map<String, TypeSpec>,
) {
    private val schemas = componentSchemas.distinctBy { it.jsonReference }
    private val relationships = SchemaCompositionRelationships(schemas)
    private val modelsByName = primaryModels.values.associateBy { it.name }
    private val modelPackage = modelsPackage(packages.base)

    fun apply(models: MutableSet<TypeSpec>): MutableSet<TypeSpec> {
        val contracts = mutableListOf<TypeSpec>()
        val interfacesByModel = mutableMapOf<String, MutableList<ClassName>>()
        val overriddenProperties = mutableMapOf<String, MutableSet<String>>()
        val ancestors =
            schemas
                .flatMap { schema ->
                    relationships.allOfComponents(schema) +
                        if (schema.isOneOfSuperInterface()) {
                            schema.oneOfSchemas.filter { alternative -> schemas.any { it.jsonReference == alternative.jsonReference } }
                        } else {
                            emptyList()
                        }
                }.distinctBy { it.jsonReference }
        ancestors.forEach { ancestor ->
            val baseModel = primaryModels[ancestor.jsonReference] ?: return@forEach
            if (ancestor.safeType() != OasType.Object.type || !baseModel.isConcreteObject()) return@forEach
            val properties = effectiveProperties(baseModel)
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
                if (!model.isConcreteObject()) return@member
                val memberProperties = effectiveProperties(model)
                val incompatible =
                    properties.values.firstOrNull { property ->
                        val memberProperty = memberProperties[property.name]
                        memberProperty == null || !isCompatible(memberProperty.type, property.type)
                    }
                if (incompatible != null) {
                    logger.warning(
                        "Omitting $name from ${model.name}: property '${incompatible.name}' has type " +
                            "${memberProperties[incompatible.name]?.type ?: "<missing>"}, " +
                            "but the contract requires ${incompatible.type}. " +
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
            relationships.allOfComponents(schema).any { it.jsonReference == ancestor.jsonReference }

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
        val parent = (model.superclass as? ClassName)?.generatedModel()
        return parent?.let { effectiveProperties(it, visited + name) }.orEmpty() + model.propertySpecs.associateBy { it.name }
    }

    private fun isCompatible(
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

    private fun TypeSpec.isConcreteObject(): Boolean = kind == TypeSpec.Kind.CLASS || kind == TypeSpec.Kind.OBJECT

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
