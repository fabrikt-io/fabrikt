package com.cjbooms.fabrikt.parser

import com.cjbooms.fabrikt.util.YamlObjectMapper
import com.fasterxml.jackson.databind.JsonNode

internal data class SourceOpenApiDocument(
    val content: String,
    val root: JsonNode,
    val version: OpenApiVersion?,
    val componentSchemas: Map<String, SourceSchema>,
    val schemaEntryPoints: Map<String, SourceSchema>,
    val schemasByLocation: Map<String, SourceSchema>,
)

internal object SourceOpenApiDocumentParser {
    fun parse(input: String): SourceOpenApiDocument {
        val root = YamlObjectMapper.instance.readTree(input)
        val version = OpenApiVersion.parse(root["openapi"]?.asText())
        val schemaEntryPoints =
            SourceSchemaEntryPointCollector
                .collect(root, version)
                .mapValues { (location, node) -> readSchema(node, location, version) }
        return SourceOpenApiDocument(
            content = input,
            root = root,
            version = version,
            componentSchemas = readComponentSchemas(root, schemaEntryPoints),
            schemaEntryPoints = schemaEntryPoints,
            schemasByLocation = indexSchemas(schemaEntryPoints.values),
        )
    }

    private fun readComponentSchemas(
        root: JsonNode,
        schemaEntryPoints: Map<String, SourceSchema>,
    ): Map<String, SourceSchema> {
        val schemas = root.path("components").path("schemas")
        if (!schemas.isObject) return emptyMap()

        return schemas.properties().associate { (name, _) ->
            name to schemaEntryPoints.getValue("#/components/schemas/${name.toJsonPointerToken()}")
        }
    }

    private fun indexSchemas(entryPoints: Collection<SourceSchema>): Map<String, SourceSchema> =
        buildMap {
            entryPoints.forEach { entryPoint ->
                entryPoint.visitRecursively { schema ->
                    check(schema.location !in this) { "Duplicate source schema location: ${schema.location}" }
                    put(schema.location, schema)
                }
            }
        }

    private fun SourceSchema.visitRecursively(visitor: (SourceSchema) -> Unit) {
        visitor(this)
        if (this !is SourceObjectSchema) return

        properties.values.forEach { it.visitRecursively(visitor) }
        items?.visitRecursively(visitor)
        allOf.forEach { it.visitRecursively(visitor) }
        anyOf.forEach { it.visitRecursively(visitor) }
        oneOf.forEach { it.visitRecursively(visitor) }
        not?.visitRecursively(visitor)
        additionalProperties?.visitRecursively(visitor)
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
                properties = readNamedSchemas(node["properties"], "$location/properties", version),
                items = readOptionalSchema(node["items"], "$location/items", version),
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
            )
        }

    private fun readNamedSchemas(
        node: JsonNode?,
        location: String,
        version: OpenApiVersion?,
    ): Map<String, SourceSchema> {
        if (node?.isObject != true) return emptyMap()

        return node.properties().associate { (name, schema) ->
            name to readSchema(schema, "$location/${name.toJsonPointerToken()}", version)
        }
    }

    private fun readOptionalSchema(
        node: JsonNode?,
        location: String,
        version: OpenApiVersion?,
    ): SourceSchema? = node?.takeIf { it.isObject || it.isBoolean }?.let { readSchema(it, location, version) }

    private fun readSchemaList(
        node: JsonNode?,
        location: String,
        version: OpenApiVersion?,
    ): List<SourceSchema> {
        if (node?.isArray != true) return emptyList()

        return node.mapIndexedNotNull { index, schema ->
            schema.takeIf { it.isObject || it.isBoolean }?.let { readSchema(it, "$location/$index", version) }
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
