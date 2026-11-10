package com.cjbooms.fabrikt.util

import com.cjbooms.fabrikt.model.OpenApiSchema
import com.cjbooms.fabrikt.util.SchemaParserExtensions.findOneOfSuperInterface

internal class OneOfSuperInterfaceCache(
    private val schemas: List<OpenApiSchema>,
) {
    private data class Discovery(
        val interfaces: List<OpenApiSchema>,
        val inlineSchemas: List<Pair<OpenApiSchema, OpenApiSchema>>,
    )

    private val discoveries = mutableMapOf<String, Discovery>()

    fun matchesCandidates(candidates: List<OpenApiSchema>): Boolean =
        schemas.size == candidates.size && schemas.indices.all { schemas[it] === candidates[it] }

    fun find(schema: OpenApiSchema): List<OpenApiSchema> {
        val discovery =
            discoveries.getOrPut(schema.jsonReference) {
                val inlineSchemas = mutableListOf<Pair<OpenApiSchema, OpenApiSchema>>()
                val interfaces =
                    schema.findOneOfSuperInterface(schemas) { inline, enclosing ->
                        inlineSchemas.add(inline to enclosing)
                        ModelNameRegistry.preRegisterInlineSchema(inline, enclosing)
                    }
                Discovery(interfaces, inlineSchemas)
            }
        discovery.inlineSchemas.forEach { (inline, enclosing) ->
            ModelNameRegistry.preRegisterInlineSchema(inline, enclosing)
        }
        return discovery.interfaces
    }
}
