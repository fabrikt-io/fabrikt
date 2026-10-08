package com.cjbooms.fabrikt.model

internal class SchemaCompositionRelationships(
    componentSchemas: Collection<OpenApiSchema>,
) {
    private val components = componentSchemas.associateBy { it.jsonReference }

    fun allOfComponents(schema: OpenApiSchema): List<OpenApiSchema> {
        val ancestors = mutableListOf<OpenApiSchema>()
        val visited = mutableSetOf(schema.jsonReference)
        val pending = ArrayDeque(schema.allOfSchemas)
        while (pending.isNotEmpty()) {
            val ancestor = pending.removeFirst()
            if (!visited.add(ancestor.jsonReference)) continue
            components[ancestor.jsonReference]?.let(ancestors::add)
            pending.addAll(ancestor.allOfSchemas)
        }
        return ancestors
    }
}
