package com.cjbooms.fabrikt.util

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode

/**
 * Handles downgrading OpenAPI 3.1 schemas to OpenAPI 3.0 compatibility.
 * This is needed because some OpenAPI 3.1 features are not supported by the kaizen parser.
 */
object OpenApi31Downgrader {
    private val NULL_TYPE: JsonNode = YamlUtils.objectMapper.valueToTree("null")
    private const val TARGET_MAJOR = 3
    private const val TARGET_MINOR = 0

    /**
     * Main entry point for converting an OpenAPI schema to 3.0 compatibility.
     */
    fun downgradeIncompatibleElements(root: JsonNode) {
        if (shouldDowngradeSpecForCompatibility(root)) {
            downgradeNullableSyntax(root)
            fillMissingArrayItems(root)
        }
    }

    private fun shouldDowngradeSpecForCompatibility(root: JsonNode): Boolean {
        val versionString = root["openapi"]?.asText() ?: ""
        if (versionString.isBlank()) return false

        val parts = versionString.split(".")
        val major = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0

        return major > TARGET_MAJOR || (major == TARGET_MAJOR && minor > TARGET_MINOR)
    }

    /**
     * Fill missing `items` schemas for array types introduced in OpenAPI 3.1 where `items` is optional.
     * When an array has no items schema, we treat it as List<Any?>.
     */
    private fun fillMissingArrayItems(node: JsonNode) {
        when {
            node.isObject -> {
                val objectNode = node as ObjectNode
                if (objectNode.get("type")?.asText() == "array" && !objectNode.has("items")) {
                    objectNode.set<JsonNode>("items", YamlUtils.objectMapper.createObjectNode().apply {
                        put("nullable", true)
                    })
                }
                objectNode.fields().forEach { (_, value) -> fillMissingArrayItems(value) }
            }

            node.isArray -> node.forEach { fillMissingArrayItems(it) }
        }
    }

    /**
     * Converts OpenAPI 3.1 nullable syntax to OpenAPI 3.0 compatible format.
     * Handles:
     * - Arrays of types including null (`type: ["string", "null"]`)
     * - Union types with null (`oneOf`/`anyOf` containing a null type)
     */
    private fun downgradeNullableSyntax(
        node: JsonNode,
        propertyName: String? = null,
        parentSchemaObject: ObjectNode? = null
    ) {
        // Track the schema object through recursion. Top level attributes, e.g. 'required', may be modified.
        var schemaObject = parentSchemaObject
        if (node.has("properties")) {
            schemaObject = node as ObjectNode
        }
        when {
            node.isObject -> {
                val objectNode = node as ObjectNode
                val fieldsToProcess = objectNode.fields().asSequence().toList()
                var requiresNullable = false

                for ((key, value) in fieldsToProcess) {
                    // Handle `type: ["object", "null"]`
                    if (key == "type" && value.isArray && value.contains(NULL_TYPE)) {
                        val nonNullType = value.first { it != NULL_TYPE }
                        objectNode.replace("type", nonNullType)
                        requiresNullable = true
                    }

                    // Handle `anyOf` or `oneOf` with a `type: null` entry
                    if ((key == "oneOf" || key == "anyOf")
                        && value.isArray
                        && value.size() == 2
                        && value.any { it.isObject && it.has("type") && it.get("type") == NULL_TYPE }
                    ) {
                        val nonNullOption = value.first { !it.has("type") || it.get("type") != NULL_TYPE }

                        if (nonNullOption.isObject) {
                            // Replace the *entire object* with the non-null option, and add nullable
                            objectNode.removeAll()
                            objectNode.setAll<ObjectNode>(nonNullOption.deepCopy<ObjectNode>())
                            objectNode.put("nullable", true)

                            // In OAS 3.0, the 'nullable' sibling property will be ignored for '$ref' types, but the
                            // generated Kotlin type still needs to be recognized as nullable. Without modifying the
                            // referenced schema object or building a new nullable version of that object, we can remove
                            // the field from 'required', which has the same effect on the generated code.
                            if (objectNode.has("\$ref")) {
                                val requiredProperties = schemaObject?.get("required") as? ArrayNode
                                val newRequiredProperties =
                                    requiredProperties?.filter { it.textValue() != propertyName }
                                if (newRequiredProperties.isNullOrEmpty()) {
                                    schemaObject?.remove("required")
                                } else {
                                    schemaObject?.replace(
                                        "required",
                                        YamlUtils.objectMapper.valueToTree(newRequiredProperties)
                                    )
                                }
                            }
                            return
                        }
                    }

                    downgradeNullableSyntax(value, key, schemaObject)
                }

                if (requiresNullable) {
                    objectNode.put("nullable", true)
                }
            }

            node.isArray -> {
                node.forEach { downgradeNullableSyntax(it, propertyName, schemaObject) }
            }
        }
    }
}
