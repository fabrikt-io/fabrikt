package com.cjbooms.fabrikt.parser

internal class SourceSchemaPolymorphism(
    private val document: SourceOpenApiDocument,
) {
    fun includeSubtypes(schemaNames: Set<String>): Set<String> {
        val selected = schemaNames.toMutableSet()
        val pending = ArrayDeque(schemaNames)
        while (pending.isNotEmpty()) {
            val name = pending.removeFirst()
            val base = document.componentSchemas[name]?.let(::resolve) as? SourceObjectSchema ?: continue
            if (base.discriminator == null) continue
            document.componentSchemas.forEach { (candidateName, candidate) ->
                val subtype = candidate as? SourceObjectSchema ?: return@forEach
                if (subtype.identity != base.identity &&
                    subtype.allOf.any { includes(it, base.identity, mutableSetOf()) } &&
                    selected.add(candidateName)
                ) {
                    pending.add(candidateName)
                }
            }
        }
        return selected
    }

    private fun includes(
        schema: SourceSchema,
        base: GeneratorSchemaIdentity,
        visited: MutableSet<GeneratorSchemaIdentity>,
    ): Boolean {
        val resolved = resolve(schema)
        if (resolved.identity == base) return true
        if (!visited.add(resolved.identity)) return false
        return (resolved as? SourceObjectSchema)?.allOf.orEmpty().any { includes(it, base, visited) }
    }

    private fun resolve(schema: SourceSchema): SourceSchema {
        var current = schema
        val visited = mutableSetOf<GeneratorSchemaIdentity>()
        while (visited.add(current.identity)) {
            val resolution = document.schemaReferenceResolutions[current.location]
            current = (resolution as? SourceSchemaReferenceResolution.Resolved)?.target ?: return current
        }
        return current
    }
}
