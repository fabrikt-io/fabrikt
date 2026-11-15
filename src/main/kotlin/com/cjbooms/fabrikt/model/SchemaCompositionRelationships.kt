package com.cjbooms.fabrikt.model

import com.cjbooms.fabrikt.util.NormalisedString.camelCase
import com.cjbooms.fabrikt.util.SchemaParserExtensions.allOfAncestors
import com.cjbooms.fabrikt.util.SchemaParserExtensions.isOneOfSuperInterface

internal class SchemaCompositionRelationships(
    componentSchemas: Collection<OpenApiSchema>,
) {
    private val components = componentSchemas.associateBy { it.jsonReference }
    private val descendants by lazy {
        components.values
            .flatMap { member ->
                allOfComponents(member).map { it.jsonReference to member }
            }.groupBy({ it.first }, { it.second })
    }
    val parentReferences: Set<String> get() = descendants.keys

    fun descendantsOf(
        ancestor: OpenApiSchema,
        additionalMembers: Collection<OpenApiSchema> = emptyList(),
    ): List<OpenApiSchema> =
        (
            descendants[ancestor.jsonReference].orEmpty() +
                additionalMembers.filter { member ->
                    member.jsonReference !in components && allOfComponents(member).any { it.jsonReference == ancestor.jsonReference }
                }
        ).distinctBy { it.jsonReference }

    private val propertyConflicts by lazy {
        val conflicts = mutableMapOf<String, MutableSet<String>>()
        components.values.forEach { member ->
            val included = listOf(member) + member.allOfAncestors()
            val keys = propertyCollisions(included)
            included.forEach { schema ->
                conflicts.getOrPut(schema.jsonReference, ::linkedSetOf).addAll(keys)
            }
        }
        conflicts
    }

    fun conflictingPropertyKeys(schema: OpenApiSchema): Set<String> =
        propertyConflicts[schema.jsonReference] ?: propertyCollisions(listOf(schema) + schema.allOfAncestors())

    private fun propertyCollisions(schemas: Collection<OpenApiSchema>): Set<String> =
        schemas
            .flatMap { it.properties.keys }
            .distinct()
            .groupBy { it.camelCase() }
            .values
            .filter { it.size > 1 }
            .flatten()
            .toSet()

    fun contractComponents(additionalMembers: Collection<OpenApiSchema> = emptyList()): List<OpenApiSchema> =
        (components.values + additionalMembers)
            .flatMap { schema ->
                allOfComponents(schema) +
                    if (schema.isOneOfSuperInterface()) {
                        schema.oneOfSchemas.filter { it.jsonReference in components }
                    } else {
                        emptyList()
                    }
            }.distinctBy { it.jsonReference }

    fun allOfComponents(schema: OpenApiSchema): List<OpenApiSchema> = schema.allOfAncestors().mapNotNull { components[it.jsonReference] }
}
