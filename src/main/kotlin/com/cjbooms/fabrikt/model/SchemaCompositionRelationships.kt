package com.cjbooms.fabrikt.model

import com.cjbooms.fabrikt.util.NormalisedString.camelCase
import com.cjbooms.fabrikt.util.SchemaParserExtensions.isOneOfSuperInterface

internal class SchemaCompositionRelationships(
    componentSchemas: Collection<OpenApiSchema>,
) {
    private val components = componentSchemas.associateBy { it.jsonReference }
    private val propertyConflicts by lazy {
        val conflicts = mutableMapOf<String, MutableSet<String>>()
        components.values.forEach { member ->
            val included = listOf(member) + allOfSchemas(member)
            val keys = propertyCollisions(included)
            included.forEach { schema ->
                conflicts.getOrPut(schema.jsonReference, ::linkedSetOf).addAll(keys)
            }
        }
        conflicts
    }

    fun conflictingPropertyKeys(schema: OpenApiSchema): Set<String> =
        propertyConflicts[schema.jsonReference] ?: propertyCollisions(listOf(schema) + allOfSchemas(schema))

    private fun propertyCollisions(schemas: Collection<OpenApiSchema>): Set<String> =
        schemas
            .flatMap { it.properties.keys }
            .distinct()
            .groupBy { it.camelCase() }
            .values
            .filter { it.size > 1 }
            .flatten()
            .toSet()

    fun contractComponents(): List<OpenApiSchema> =
        components.values
            .flatMap { schema ->
                allOfComponents(schema) +
                    if (schema.isOneOfSuperInterface()) {
                        schema.oneOfSchemas.filter { it.jsonReference in components }
                    } else {
                        emptyList()
                    }
            }.distinctBy { it.jsonReference }

    fun allOfComponents(schema: OpenApiSchema): List<OpenApiSchema> = allOfSchemas(schema).mapNotNull { components[it.jsonReference] }

    private fun allOfSchemas(schema: OpenApiSchema): List<OpenApiSchema> {
        val ancestors = mutableListOf<OpenApiSchema>()
        val visited = mutableSetOf(schema.jsonReference)
        val pending = ArrayDeque(schema.allOfSchemas)
        while (pending.isNotEmpty()) {
            val ancestor = pending.removeFirst()
            if (!visited.add(ancestor.jsonReference)) continue
            ancestors.add(ancestor)
            pending.addAll(ancestor.allOfSchemas)
        }
        return ancestors
    }
}
