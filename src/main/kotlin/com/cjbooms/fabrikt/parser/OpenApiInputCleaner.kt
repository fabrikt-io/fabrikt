package com.cjbooms.fabrikt.parser

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

internal object OpenApiInputCleaner {
    fun cleanEmptyTypes(node: JsonNode) {
        when {
            node.isObject -> {
                val objectNode = node as ObjectNode
                val fieldsToProcess = objectNode.fields().asSequence().toList()

                for ((key, value) in fieldsToProcess) {
                    if (key == "type" && (value.isNull || (value.isTextual && value.asText().isBlank()))) {
                        objectNode.remove("type")
                    } else {
                        cleanEmptyTypes(value)
                    }
                }
            }

            node.isArray -> node.forEach { cleanEmptyTypes(it) }
        }
    }

    /**
     * Inlines a parameter `$ref` that points outside `#/components/parameters/…` — e.g. into
     * another operation's `#/paths/…/parameters/N` — with a deep copy of the target object.
     * Both Kaizen and `SourceSchemaEntryPointCollector` only materialise inline or
     * components-scoped parameters: left unresolved, Kaizen leaves `Parameter.in` unset (NPE
     * in the client generator), and the collector never visits the schema behind the ref
     * (silently skipping its uninhabitable-schema classification).
     */
    fun resolveIntraDocumentParameterRefs(root: JsonNode) {
        walkParameterLists(root, root)
    }

    private fun walkParameterLists(
        root: JsonNode,
        node: JsonNode,
    ) {
        when {
            node.isObject -> {
                val objectNode = node as ObjectNode
                objectNode.fields().asSequence().toList().forEach { (key, value) ->
                    if (key == "parameters" && value.isArray) {
                        inlineParameterRefs(root, value as ArrayNode)
                    } else {
                        walkParameterLists(root, value)
                    }
                }
            }
            node.isArray -> node.forEach { walkParameterLists(root, it) }
        }
    }

    private fun inlineParameterRefs(
        root: JsonNode,
        parameters: ArrayNode,
    ) {
        for (i in 0 until parameters.size()) {
            val param = parameters[i]
            if (!param.isObject) continue
            val ref = param.get("\$ref")?.takeIf { it.isTextual }?.asText() ?: continue
            if (!ref.startsWith("#/paths/")) continue
            val resolved = resolveJsonPointer(root, ref) ?: continue
            parameters.set(i, resolved.deepCopy())
        }
    }

    private fun resolveJsonPointer(
        root: JsonNode,
        ref: String,
    ): JsonNode? {
        // Strip the leading '#' to get the JSON Pointer, then URL-decode percent-encoded characters
        // (e.g. %7B -> '{', %7D -> '}') that appear in path template segments.
        // Jackson's JsonNode.at() handles the remaining JSON Pointer escapes (~0, ~1) natively.
        val pointer = URLDecoder.decode(ref.removePrefix("#"), StandardCharsets.UTF_8.name())
        val resolved = root.at(pointer)
        return if (resolved.isMissingNode) null else resolved
    }
}
