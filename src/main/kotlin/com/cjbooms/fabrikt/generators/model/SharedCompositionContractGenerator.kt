package com.cjbooms.fabrikt.generators.model

import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.model.Destinations.modelsPackage
import com.cjbooms.fabrikt.model.OasType
import com.cjbooms.fabrikt.model.OpenApiSchema
import com.cjbooms.fabrikt.model.SchemaCompositionRelationships
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.SchemaParserExtensions.isOneOfSuperInterface
import com.cjbooms.fabrikt.util.SchemaParserExtensions.safeType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.util.logging.Logger

internal class SharedCompositionContractGenerator(
    private val packages: Packages,
    componentSchemas: Collection<OpenApiSchema>,
    private val primaryModels: Map<String, TypeSpec>,
    private val mappedContractModels: Map<String, TypeSpec> = emptyMap(),
    private val generatedSchemas: Collection<OpenApiSchema> = emptyList(),
) {
    private val schemas = (componentSchemas + generatedSchemas).distinctBy { it.jsonReference }
    private val relationships = SchemaCompositionRelationships(componentSchemas)
    private val propertyTypes = CompositionContractProperties(modelsPackage(packages.base), primaryModels.values)

    fun apply(models: MutableSet<TypeSpec>): MutableSet<TypeSpec> {
        val contracts = mutableListOf<TypeSpec>()
        val interfacesByModel = mutableMapOf<String, MutableList<ClassName>>()
        val overriddenProperties = mutableMapOf<String, MutableSet<String>>()
        val ancestors = relationships.contractComponents(generatedSchemas)
        ancestors.forEach { ancestor ->
            val baseModel = primaryModels[ancestor.jsonReference] ?: mappedContractModels[ancestor.jsonReference] ?: return@forEach
            if (ancestor.safeType() != OasType.Object.type || !baseModel.isConcreteObject()) return@forEach
            val properties = propertyTypes.effectiveProperties(baseModel)
            val name = ModelNameRegistry.compositionContractName(ancestor, checkNotNull(baseModel.name))
            val contractType = ClassName(modelsPackage(packages.base), name)
            contracts.add(
                TypeSpec
                    .interfaceBuilder(name)
                    .addProperties(properties.values.map { PropertySpec.builder(it.name, it.type.withoutTypeAnnotations()).build() })
                    .build(),
            )
            val compatibleMembers = mutableSetOf<String>()
            val members = (listOf(ancestor) + relationships.descendantsOf(ancestor, generatedSchemas)).distinctBy { it.jsonReference }
            members.forEach member@{ schema ->
                val model = primaryModels[schema.jsonReference] ?: return@member
                if (!model.isConcreteObject()) return@member
                val memberProperties = propertyTypes.effectiveProperties(model)
                val incompatible =
                    properties.values.firstOrNull { property ->
                        val memberProperty = memberProperties[property.name]
                        memberProperty == null || !propertyTypes.isCompatible(memberProperty.type, property.type)
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
        return applyContractInterfaces(models, interfacesByModel, overriddenProperties).plus(contracts).toMutableSet()
    }

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

    companion object {
        private val logger = Logger.getGlobal()
    }
}
