package com.cjbooms.fabrikt.parser

import com.cjbooms.fabrikt.util.YamlObjectMapper
import com.fasterxml.jackson.databind.JsonNode

internal data class SourceOpenApiDocument(
    val content: String,
    val root: JsonNode,
    val version: OpenApiVersion?,
    val componentSchemas: Map<String, SourceSchema>,
)

internal object SourceOpenApiDocumentParser {
    fun parse(input: String): SourceOpenApiDocument {
        val root = YamlObjectMapper.instance.readTree(input)
        val version = OpenApiVersion.parse(root["openapi"]?.asText())
        return SourceOpenApiDocument(
            content = input,
            root = root,
            version = version,
            componentSchemas = readComponentSchemas(root, version),
        )
    }

    private fun readComponentSchemas(
        root: JsonNode,
        version: OpenApiVersion?,
    ): Map<String, SourceSchema> {
        val schemas = root.path("components").path("schemas")
        if (!schemas.isObject) return emptyMap()

        return schemas.properties().associate { (name, node) ->
            name to readSchema(node, "#/components/schemas/${name.toJsonPointerToken()}", version)
        }
    }

    private fun readSchema(
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
                types = readTypes(node, version),
                reference = node["\$ref"]?.takeIf(JsonNode::isTextual)?.textValue(),
            )
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
            }.mapNotNull(SourceSchemaType::from)
                .toCollection(linkedSetOf())

        if (version?.major == 3 && version.minor == 0 && declaredTypes.isNotEmpty() && node["nullable"]?.asBoolean() == true) {
            declaredTypes.add(SourceSchemaType.NULL)
        }

        return declaredTypes
    }

    private fun String.toJsonPointerToken(): String = replace("~", "~0").replace("/", "~1")
}
