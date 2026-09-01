package com.cjbooms.fabrikt.parser

import com.fasterxml.jackson.databind.JsonNode

internal object SourceSchemaParser {
    fun parse(
        node: JsonNode,
        location: String,
        version: OpenApiVersion?,
    ): SourceSchema =
        if (node.isBoolean) {
            SourceBooleanSchema(location, node, node.booleanValue())
        } else {
            SourceObjectSchema(
                location = location,
                node = node,
                identifier = node["\$id"]?.takeIf(JsonNode::isTextual)?.textValue(),
                anchor = node["\$anchor"]?.takeIf(JsonNode::isTextual)?.textValue(),
                types = readTypes(node, version),
                reference = node["\$ref"]?.takeIf(JsonNode::isTextual)?.textValue(),
                definitions = readNamedSchemas(node["\$defs"], "$location/\$defs", version),
                properties = readNamedSchemas(node["properties"], "$location/properties", version),
                patternProperties =
                    readNamedSchemas(
                        node["patternProperties"],
                        "$location/patternProperties",
                        version,
                    ),
                dependentSchemas =
                    readNamedSchemas(
                        node["dependentSchemas"],
                        "$location/dependentSchemas",
                        version,
                    ),
                prefixItems = readSchemaList(node["prefixItems"], "$location/prefixItems", version),
                items = readOptionalSchema(node["items"], "$location/items", version),
                contains = readOptionalSchema(node["contains"], "$location/contains", version),
                propertyNames = readOptionalSchema(node["propertyNames"], "$location/propertyNames", version),
                ifSchema = readOptionalSchema(node["if"], "$location/if", version),
                thenSchema = readOptionalSchema(node["then"], "$location/then", version),
                elseSchema = readOptionalSchema(node["else"], "$location/else", version),
                allOf = readSchemaList(node["allOf"], "$location/allOf", version),
                anyOf = readSchemaList(node["anyOf"], "$location/anyOf", version),
                oneOf = readSchemaList(node["oneOf"], "$location/oneOf", version),
                not = readOptionalSchema(node["not"], "$location/not", version),
                additionalProperties =
                    readOptionalSchema(
                        node["additionalProperties"],
                        "$location/additionalProperties",
                        version,
                    ),
                unevaluatedItems =
                    readOptionalSchema(
                        node["unevaluatedItems"],
                        "$location/unevaluatedItems",
                        version,
                    ),
                unevaluatedProperties =
                    readOptionalSchema(
                        node["unevaluatedProperties"],
                        "$location/unevaluatedProperties",
                        version,
                    ),
                contentSchema = readOptionalSchema(node["contentSchema"], "$location/contentSchema", version),
            )
        }

    private fun readNamedSchemas(
        node: JsonNode?,
        location: String,
        version: OpenApiVersion?,
    ): Map<String, SourceSchema> {
        if (node?.isObject != true) return emptyMap()

        return node.properties().associate { (name, schema) ->
            name to parse(schema, "$location/${name.toJsonPointerToken()}", version)
        }
    }

    private fun readOptionalSchema(
        node: JsonNode?,
        location: String,
        version: OpenApiVersion?,
    ): SourceSchema? = node?.takeIf { it.isObject || it.isBoolean }?.let { parse(it, location, version) }

    private fun readSchemaList(
        node: JsonNode?,
        location: String,
        version: OpenApiVersion?,
    ): List<SourceSchema> {
        if (node?.isArray != true) return emptyList()

        return node.mapIndexedNotNull { index, schema ->
            schema.takeIf { it.isObject || it.isBoolean }?.let { parse(it, "$location/$index", version) }
        }
    }

    private fun readTypes(
        node: JsonNode,
        version: OpenApiVersion?,
    ): Set<SourceSchemaType> {
        val typeNode = node["type"]
        val declaredTypes =
            when {
                typeNode?.isTextual == true -> sequenceOf(typeNode.textValue())
                typeNode?.isArray == true -> typeNode.asSequence().filter(JsonNode::isTextual).map(JsonNode::textValue)
                else -> emptySequence()
            }.map(SourceSchemaType::from)
                .toCollection(linkedSetOf())

        if (version?.major == 3 && version.minor == 0 && declaredTypes.isNotEmpty() && node["nullable"]?.asBoolean() == true) {
            declaredTypes.add(SourceSchemaType.NULL)
        }

        return declaredTypes
    }

    private fun String.toJsonPointerToken(): String = replace("~", "~0").replace("/", "~1")
}
