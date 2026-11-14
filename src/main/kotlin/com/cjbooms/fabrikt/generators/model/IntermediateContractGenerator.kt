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
        val members: List<OpenApiSchema>,
    )

    private val propertySettings = PropertyInfo.httpSettings(direction)
    private val contracts = linkedMapOf<String, Contract>()
    private val referenced = mutableSetOf<String>()
    val referenceNames = linkedMapOf<String, String>()
    private val namesByReference = linkedMapOf<String, String>()

    fun prepare(
        api: OpenApi3Document,
        schemas: List<SchemaInfo>,
    ) {
        val candidates =
            api.schemas.values
                .filter { it.isHierarchyIntermediate() }
                .associateBy { it.jsonReference }
        val visited = mutableSetOf<Pair<String, Boolean>>()

        fun visit(
            schema: OpenApiSchema,
            reference: Boolean = false,
        ) {
            if (reference) referenced.add(schema.jsonReference)
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
        api.paths.values.forEach { path ->
            path.parameters.forEach { visit(it.schema, true) }
            path.operations.values.forEach { operation ->
                operation.parameters.forEach { visit(it.schema, true) }
                (operation.requestBody.contentMediaTypes.values + operation.responses.values.flatMap { it.contentMediaTypes.values })
                    .forEach {
                        visit(it.schema, true)
                    }
            }
        }
        val components =
            api.schemas.values
                .map { it.jsonReference }
                .toSet()
        val additional = schemas.map { it.schema }.filter { it.jsonReference !in components }
        candidates.values.forEach { schema ->
            contracts[schema.jsonReference] =
                Contract(schema, checkNotNull(schema.discriminatedRoot()), listOf(schema) + api.allOfDescendants(schema, additional))
        }
    }

    fun allocateNames(primaryModels: Map<String, TypeSpec>) {
        contracts.values.filter { it.schema.jsonReference in referenced }.sortedBy { it.schema.jsonReference }.forEach { contract ->
            val model = primaryModels[contract.schema.jsonReference] ?: return@forEach
            if (!model.isConcreteObject()) return@forEach
            val modelName = checkNotNull(model.name)
            val name = ModelNameRegistry.intermediateContractName(contract.schema, modelName)
            namesByReference[contract.schema.jsonReference] = name
            referenceNames[modelName] = name
            ModelNameRegistry.intermediateReferences[modelName] = name
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
                val name = namesByReference[contract.schema.jsonReference] ?: return@mapNotNull null
                val base = primaryModels[contract.schema.jsonReference] ?: return@mapNotNull null
                val required = properties.effectiveProperties(base)
                val compatible =
                    contract.members.filter { member ->
                        val model = primaryModels[member.jsonReference] ?: return@filter false
                        if (!model.isConcreteObject()) return@filter false
                        val actual = properties.effectiveProperties(model)
                        val incompatible =
                            required.values.firstOrNull { expected ->
                                actual[expected.name]?.let { !properties.isCompatible(it.type, expected.type) } ?: true
                            }
                        if (incompatible != null) {
                            logger.warning(
                                "Omitting ${model.name} from $name: property '${incompatible.name}' is incompatible with the contract. Align the composed property types to include this subtype.",
                            )
                            false
                        } else {
                            interfaces
                                .getOrPut(
                                    checkNotNull(model.name),
                                    ::mutableListOf,
                                ).add(ClassName(modelsPackage(packages.base), name))
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
                            JacksonMetadata
                                .discriminatorMappings(
                                    discriminator,
                                    member,
                                    ClassName(modelsPackage(packages.base), checkNotNull(model.name)),
                                    fallbackName = member.name,
                                ).entries
                                .map { it.key to it.value }
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
                    .interfaceBuilder(name)
                    .addModifiers(KModifier.SEALED)
                    .addAnnotation(JacksonMetadata.basePolymorphicType(checkNotNull(discriminator.propertyName)))
                    .addAnnotation(JacksonMetadata.polymorphicSubTypes(mappings, enum))
                    .addProperties(required.values.map { PropertySpec.builder(it.name, it.type.withoutTypeAnnotations()).build() })
                    .build()
            }
        return applyContractInterfaces(models, interfaces, overrides).plus(generated).toMutableSet()
    }

    companion object {
        private val logger = Logger.getGlobal()
    }
}
