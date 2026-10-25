package com.cjbooms.fabrikt.model

import com.cjbooms.fabrikt.util.NormalisedString.camelCase
import com.cjbooms.fabrikt.util.SchemaParserExtensions.isOneOfSuperInterface

internal class SchemaCompositionRelationships(
    componentSchemas: Collection<OpenApiSchema>,
) {
    private val components = componentSchemas.associateBy { it.jsonReference }
    private val propertyConflicts = mutableMapOf<String, Set<String>>()
    private val allOfNeighbours by lazy {
        val neighbours = mutableMapOf<String, MutableSet<OpenApiSchema>>()
        val visited = mutableSetOf<String>()

        fun connect(schema: OpenApiSchema) {
            if (!visited.add(schema.jsonReference)) return
            schema.allOfSchemas.forEach { child ->
                neighbours.getOrPut(schema.jsonReference, ::linkedSetOf).add(child)
                neighbours.getOrPut(child.jsonReference, ::linkedSetOf).add(schema)
                connect(child)
            }
        }
        components.values.forEach(::connect)
        neighbours
    }

    fun conflictingPropertyKeys(schema: OpenApiSchema): Set<String> =
        propertyConflicts.getOrPut(schema.jsonReference) {
            val family = linkedMapOf<String, OpenApiSchema>()
            val pending = ArrayDeque(listOf(schema))
            while (pending.isNotEmpty()) {
                val member = pending.removeFirst()
                if (family.putIfAbsent(member.jsonReference, member) != null) continue
                pending.addAll(allOfNeighbours[member.jsonReference].orEmpty())
            }
            val conflicts =
                family.values
                    .flatMap { member ->
                        (listOf(member) + allOfSchemas(member))
                            .flatMap { it.properties.keys }
                            .distinct()
                            .groupBy { it.camelCase() }
                            .values
                            .filter { it.size > 1 }
                            .flatten()
                    }.toSet()
            family.keys.forEach { propertyConflicts[it] = conflicts }
            conflicts
        }

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
