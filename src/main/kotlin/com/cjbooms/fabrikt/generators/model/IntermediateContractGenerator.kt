package com.cjbooms.fabrikt.generators.model

import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.model.Destinations.modelsPackage
import com.cjbooms.fabrikt.model.KotlinTypeInfo
import com.cjbooms.fabrikt.model.ModelDirection
import com.cjbooms.fabrikt.model.OpenApi3Document
import com.cjbooms.fabrikt.model.OpenApiSchema
import com.cjbooms.fabrikt.model.PropertyInfo
import com.cjbooms.fabrikt.model.SchemaInfo
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.SchemaParserExtensions.allOfAncestors
import com.cjbooms.fabrikt.util.SchemaParserExtensions.discriminatedRoot
import com.cjbooms.fabrikt.util.SchemaParserExtensions.getDiscriminatorForInlinedObjectUnderAllOf
import com.cjbooms.fabrikt.util.SchemaParserExtensions.isHierarchyIntermediate
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.util.logging.Logger

internal class IntermediateContractGenerator(
    private val packages: Packages,
    private val direction: ModelDirection?,
) {
    private data class Contract(
        val schema: OpenApiSchema,
        val root: OpenApiSchema,
        val name: String,
        val members: List<OpenApiSchema>,
    )

    private val propertySettings = PropertyInfo.httpSettings(direction)
    private val contracts = linkedMapOf<String, Contract>()
    val referenceNames = linkedMapOf<String, String>()

    fun prepare(
        api: OpenApi3Document,
        schemas: List<SchemaInfo>,
    ) {
        val candidates =
            api.schemas.values
                .filter { it.isHierarchyIntermediate() }
                .associateBy { it.jsonReference }
        if (candidates.isEmpty()) return
        val referenced = mutableSetOf<String>()
        val visited = mutableSetOf<Pair<String, Boolean>>()

        fun visit(
            schema: OpenApiSchema,
            reference: Boolean = false,
        ) {
            if (reference && schema.jsonReference in candidates) referenced.add(schema.jsonReference)
            if (!visited.add(schema.jsonReference to reference)) return
            schema.properties.values
                .filterNot {
                    (propertySettings.excludeReadOnly && it.isReadOnly) || (propertySettings.excludeWriteOnly && it.isWriteOnly)
                }.forEach { visit(it, true) }
            if (reference || schema.properties.isNotEmpty()) {
                listOf(schema.itemsSchema, schema.additionalPropertiesSchema).filter { it.isPresent }.forEach { visit(it, true) }
            }
            (schema.allOfSchemas + schema.oneOfSchemas + schema.anyOfSchemas).forEach { visit(it) }
        }
        schemas.forEach { visit(it.schema) }
        if (referenced.isEmpty()) return
        schemas.forEach { ModelNameRegistry.getOrRegister(it) }
        val descendants =
            api.schemas.values
                .flatMap { member ->
                    member.allOfAncestors().map { it.jsonReference to member }
                }.groupBy({ it.first }, { it.second })
        referenced.sorted().forEach { reference ->
            val schema = candidates.getValue(reference)
            val type = KotlinTypeInfo.from(schema, direction = direction) as? KotlinTypeInfo.Object ?: return@forEach
            val name = ModelNameRegistry.intermediateContractName(schema, type.simpleClassName)
            contracts[reference] =
                Contract(schema, checkNotNull(schema.discriminatedRoot()), name, listOf(schema) + descendants[reference].orEmpty())
            referenceNames[type.simpleClassName] = name
        }
    }

    fun apply(
        models: MutableSet<TypeSpec>,
        primaryModels: Map<String, TypeSpec>,
    ): MutableSet<TypeSpec> {
        if (contracts.isEmpty()) return models
        val properties = CompositionContractProperties(modelsPackage(packages.base), models)
        val interfaces = mutableMapOf<String, MutableList<ClassName>>()
        val overrides = mutableMapOf<String, MutableSet<String>>()
        val generated =
            contracts.values.mapNotNull { contract ->
                val base = primaryModels[contract.schema.jsonReference] ?: return@mapNotNull null
                val required = properties.effectiveProperties(base)
                val compatible =
                    contract.members.filter { member ->
                        val model = primaryModels[member.jsonReference] ?: return@filter false
                        val actual = properties.effectiveProperties(model)
                        val incompatible =
                            required.values.firstOrNull { expected ->
                                actual[expected.name]?.let { !properties.isCompatible(it.type, expected.type) } ?: true
                            }
                        if (incompatible != null) {
                            logger.warning(
                                "Omitting ${model.name} from ${contract.name}: property '${incompatible.name}' is incompatible with the contract. Align the composed property types to include this subtype.",
                            )
                            false
                        } else {
                            interfaces
                                .getOrPut(
                                    checkNotNull(model.name),
                                    ::mutableListOf,
                                ).add(ClassName(modelsPackage(packages.base), contract.name))
                            overrides.getOrPut(checkNotNull(model.name), ::mutableSetOf).addAll(required.keys)
                            true
                        }
                    }
                val discriminator =
                    contract.root.discriminator.takeIf { it.propertyName != null }
                        ?: checkNotNull(contract.root.getDiscriminatorForInlinedObjectUnderAllOf())
                val mappings =
                    compatible
                        .flatMap { member ->
                            val model = primaryModels.getValue(member.jsonReference)
                            val keys =
                                discriminator.mappings
                                    .filterValues {
                                        it.substringAfterLast('/') == member.name
                                    }.keys
                                    .ifEmpty { setOf(checkNotNull(member.name)) }
                            keys.map { it to ClassName(modelsPackage(packages.base), checkNotNull(model.name)) }
                        }.toMap()
                val enum =
                    (
                        listOf(
                            contract.root,
                        ) + contract.root.allOfAncestors()
                    ).firstNotNullOfOrNull { it.properties[discriminator.propertyName] }?.let {
                        KotlinTypeInfo.from(it, direction = direction)
                    } as? KotlinTypeInfo.Enum
                TypeSpec
                    .interfaceBuilder(contract.name)
                    .addModifiers(KModifier.SEALED)
                    .addAnnotation(JacksonMetadata.basePolymorphicType(checkNotNull(discriminator.propertyName)))
                    .addAnnotation(JacksonMetadata.polymorphicSubTypes(mappings, enum))
                    .addProperties(required.values.map { PropertySpec.builder(it.name, it.type.withoutTypeAnnotations()).build() })
                    .build()
            }
        return models
            .map { model ->
                val implemented = interfaces[model.name].orEmpty()
                if (implemented.isEmpty()) {
                    model
                } else {
                    model
                        .toBuilder()
                        .apply {
                            implemented.forEach { addSuperinterface(it) }
                            propertySpecs.clear()
                            addProperties(
                                model.propertySpecs.map {
                                    if (it.name in
                                        overrides[model.name].orEmpty()
                                    ) {
                                        it.toBuilder().addModifiers(KModifier.OVERRIDE).build()
                                    } else {
                                        it
                                    }
                                },
                            )
                        }.build()
                }
            }.plus(generated)
            .toMutableSet()
    }

    companion object {
        private val logger = Logger.getGlobal()
    }
}
