package com.cjbooms.fabrikt.parser

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode

internal object ComposedOneOfNormalizer {
    fun normalize(root: JsonNode): Boolean {
        val schemas = root.path("components").path("schemas") as? ObjectNode ?: return false
        val references = root.findValues("\$ref").map { it.asText() }.toSet()
        var changed = false
        schemas.properties().asSequence().toList().forEach { (name, schema) ->
            if (schema is ObjectNode && normalizeSchema(name, schema, schemas, root, references)) changed = true
        }
        return changed
    }

    private fun normalizeSchema(
        name: String,
        schema: ObjectNode,
        schemas: ObjectNode,
        root: JsonNode,
        references: Set<String>,
    ): Boolean {
        val allOf = schema.path("allOf").takeIf { it.isArray && !it.isEmpty } ?: return false
        val descendantPrefix = reference(name) + "/"
        if (references.any { it.startsWith(descendantPrefix) }) return false
        val branches = allOf.toList()
        val unions = (listOf(schema) + branches.map { resolve(it, root) }).filter { it.path("oneOf").isArray }
        val union = unions.singleOrNull() as? ObjectNode ?: return false
        val variants = union.path("oneOf").toList()
        if (variants.isEmpty() || variants.any { !isObjectSchema(resolve(it, root), root, emptySet()) }) return false
        if ((schema.has("discriminator") || union.has("discriminator")) && variants.any { !it.has("\$ref") }) return false
        val common = branches.filter { resolve(it, root) !== union }.toMutableList()
        if (common.any { !isObjectSchema(resolve(it, root), root, emptySet()) }) return false
        if (common.isEmpty() && !schema.has("properties") && !schema.has("required")) return false

        val outerConstraints =
            schema.deepCopy().also {
                it.remove(
                    listOf("allOf", "oneOf", "discriminator", "x-jackson-subtype-deduction"),
                )
            }
        if (!outerConstraints.isEmpty) common.add(outerConstraints)
        if (union !== schema) {
            val unionConstraints = union.deepCopy().also { it.remove(listOf("oneOf", "discriminator", "x-jackson-subtype-deduction")) }
            if (!unionConstraints.isEmpty) common.add(unionConstraints)
        }

        val generated =
            variants.mapIndexed { index, variant ->
                val memberName =
                    variant
                        .path("\$ref")
                        .asText()
                        .substringAfterLast('/')
                        .ifEmpty { "Variant${index + 1}" }
                val candidate = name + memberName
                var generatedName = candidate
                var suffix = 1
                while (schemas.has(generatedName)) {
                    generatedName = candidate + "Extra" + if (suffix == 1) "" else suffix.toString()
                    suffix++
                }
                val composed = schemas.objectNode().put("type", "object")
                val required = (schema.path("required").toList() + union.path("required").toList()).distinct()
                if (required.isNotEmpty()) composed.putArray("required").addAll(required)
                composed.putArray("allOf").also { array ->
                    common.forEach { array.add(it.deepCopy<JsonNode>()) }
                    array.add(variant.deepCopy<JsonNode>())
                }
                schemas.set<ObjectNode>(generatedName, composed)
                variant to generatedName
            }

        val discriminator = (schema.get("discriminator") ?: union.get("discriminator"))?.deepCopy<JsonNode>() as? ObjectNode
        discriminator?.let { value ->
            val existing = value.path("mapping")
            val mapping = value.objectNode()
            generated.forEach { (variant, generatedName) ->
                val originalRef = variant.path("\$ref").asText()
                val memberName = originalRef.substringAfterLast('/')
                val aliases =
                    existing
                        .properties()
                        .asSequence()
                        .filter { (_, target) ->
                            target.asText() == originalRef || target.asText() == memberName
                        }.map { it.key }
                        .toList()
                (aliases.ifEmpty { listOf(memberName) }).forEach { alias ->
                    mapping.put(alias, reference(generatedName))
                }
            }
            value.set<ObjectNode>("mapping", mapping)
        }
        val deduction = schema.get("x-jackson-subtype-deduction") ?: union.get("x-jackson-subtype-deduction")
        val metadata =
            schema
                .properties()
                .asSequence()
                .filter { (key, _) ->
                    key in
                        setOf(
                            "description",
                            "title",
                            "deprecated",
                            "nullable",
                            "readOnly",
                            "writeOnly",
                            "default",
                            "example",
                            "externalDocs",
                        ) ||
                        key.startsWith("x-")
                }.associate { it.key to it.value }
        schema.removeAll()
        schema.putArray("oneOf").also { array ->
            generated.forEach { (_, generatedName) -> array.addObject().put("\$ref", reference(generatedName)) }
        }
        discriminator?.let { schema.set<JsonNode>("discriminator", it) }
        deduction?.let { schema.set<JsonNode>("x-jackson-subtype-deduction", it) }
        metadata.forEach { (key, value) -> schema.set<JsonNode>(key, value) }
        return true
    }

    private fun reference(name: String): String = "#/components/schemas/" + name.replace("~", "~0").replace("/", "~1")

    private fun resolve(
        node: JsonNode,
        root: JsonNode,
    ): JsonNode {
        val ref = node.path("\$ref").asText()
        return if (ref.startsWith("#/components/schemas/")) root.at(ref.removePrefix("#")) else node
    }

    private fun isObjectSchema(
        node: JsonNode,
        root: JsonNode,
        seen: Set<JsonNode>,
    ): Boolean {
        if (!node.isObject || node in seen || node.has("oneOf") || node.has("anyOf")) return false
        if (node.path("type").asText() == "object") return true
        val allOf = node.path("allOf")
        return allOf.isArray && !allOf.isEmpty && allOf.all { isObjectSchema(resolve(it, root), root, seen + setOf(node)) }
    }
}
