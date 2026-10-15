package com.cjbooms.fabrikt.parser

import com.cjbooms.fabrikt.model.BinaryMultipartPatterns

internal object BinaryMultipartPatternQuery {
    private val annotations = setOf("title", "description", "examples", "example", "deprecated", "externalDocs", "\$comment")
    private val objectKeywords = setOf("type", "properties", "patternProperties", "required", "additionalProperties") + annotations
    private val binaryKeywords = setOf("type", "format") + annotations

    fun query(
        schema: GeneratorSchema,
        resolve: (GeneratorSchema) -> GeneratorSchema,
    ): BinaryMultipartPatterns? {
        val original = schema as? SourceObjectSchema ?: return null
        val source = resolve(original) as? SourceObjectSchema ?: return null
        val declaration = queryResolved(source, resolve)
        return if (declaration != null &&
            original.reference != null &&
            original.node
                .fieldNames()
                .asSequence()
                .any { it != "\$ref" && it !in annotations }
        ) {
            declaration.copy(unsupportedReason = "multipart schema reference siblings are unsupported")
        } else {
            declaration
        }
    }

    private fun queryResolved(
        source: SourceObjectSchema,
        resolve: (GeneratorSchema) -> GeneratorSchema,
    ): BinaryMultipartPatterns? {
        if (!source.node.has("patternProperties")) {
            val compositions = source.allOf + source.anyOf + source.oneOf
            return if (compositions.any { containsPatterns(it, resolve, mutableSetOf()) }) {
                BinaryMultipartPatterns(emptyList(), "patternProperties in composed multipart schemas are unsupported")
            } else {
                null
            }
        }
        val patterns = source.patternProperties.keys.toList()

        fun unsupported(reason: String) = BinaryMultipartPatterns(patterns, reason)
        if (!source.node["patternProperties"].isObject) return unsupported("patternProperties must be an object")
        if (patterns.isEmpty()) return null
        if (source.types != setOf(SourceSchemaType.OBJECT)) return unsupported("the multipart schema must have type object")
        val unsupportedKeywords =
            source.node
                .fieldNames()
                .asSequence()
                .filterNot { it in objectKeywords || it.startsWith("x-") }
                .toList()
        if (unsupportedKeywords.isNotEmpty()) return unsupported("unsupported object keywords: ${unsupportedKeywords.joinToString()}")
        val required = source.node["required"]
        if (required != null &&
            (!required.isArray || required.any { !it.isTextual })
        ) {
            return unsupported("required must be an array of names")
        }
        if (source.requiredProperties.any { it !in source.properties }) return unsupported("required names must be fixed properties")
        if (source.patternProperties.values.any { !isBinary(it, resolve) }) {
            return unsupported("every pattern value must be an unconstrained, non-null string with format binary")
        }
        return BinaryMultipartPatterns(patterns)
    }

    private fun containsPatterns(
        schema: GeneratorSchema,
        resolve: (GeneratorSchema) -> GeneratorSchema,
        visited: MutableSet<GeneratorSchemaIdentity>,
    ): Boolean {
        val source = resolve(schema) as? SourceObjectSchema ?: return false
        if (!visited.add(source.identity)) return false
        return source.node.has("patternProperties") ||
            (source.allOf + source.anyOf + source.oneOf).any { containsPatterns(it, resolve, visited) }
    }

    private fun isBinary(
        schema: GeneratorSchema,
        resolve: (GeneratorSchema) -> GeneratorSchema,
    ): Boolean {
        val source = schema as? SourceObjectSchema ?: return false
        if (source.reference != null) {
            if (source.node
                    .fieldNames()
                    .asSequence()
                    .any { it != "\$ref" && it !in annotations }
            ) {
                return false
            }
            val target = resolve(source)
            return target !== source && isBinaryWithoutReference(target)
        }
        return isBinaryWithoutReference(source)
    }

    private fun isBinaryWithoutReference(schema: GeneratorSchema): Boolean {
        val source = schema as? SourceObjectSchema ?: return false
        return source.types == setOf(SourceSchemaType.STRING) &&
            source.metadata.format == "binary" &&
            source.node
                .fieldNames()
                .asSequence()
                .all { it in binaryKeywords || it.startsWith("x-") }
    }
}
