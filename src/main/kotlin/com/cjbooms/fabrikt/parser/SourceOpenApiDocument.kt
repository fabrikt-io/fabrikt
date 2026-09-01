package com.cjbooms.fabrikt.parser

import com.cjbooms.fabrikt.util.YamlObjectMapper
import com.fasterxml.jackson.databind.JsonNode
import java.net.URI
import java.nio.file.Paths

internal data class SourceOpenApiDocument(
    val content: String,
    val root: JsonNode,
    val baseUri: URI,
    val version: OpenApiVersion?,
    val componentSchemas: Map<String, SourceSchema>,
    val schemaEntryPoints: Map<String, SourceSchema>,
    val schemasByLocation: Map<String, SourceSchema>,
    val schemaReferenceIndex: SourceSchemaReferenceIndex,
) {
    val schemaReferenceResolutions: Map<String, SourceSchemaReferenceResolution>
        get() = schemaReferenceIndex.resolutionsByLocation
}

internal object SourceOpenApiDocumentParser {
    fun parse(
        input: String,
        baseUri: URI = Paths.get("").toAbsolutePath().toUri(),
    ): SourceOpenApiDocument {
        val root = YamlObjectMapper.instance.readTree(input)
        val version = OpenApiVersion.parse(root["openapi"]?.asText())
        val schemaEntryPoints =
            SourceSchemaEntryPointCollector
                .collect(root, version)
                .mapValues { (location, node) -> SourceSchemaParser.parse(node, location, version) }
        val schemasByLocation = indexSchemas(schemaEntryPoints.values)
        return SourceOpenApiDocument(
            content = input,
            root = root,
            baseUri = baseUri,
            version = version,
            componentSchemas = readComponentSchemas(root, schemaEntryPoints),
            schemaEntryPoints = schemaEntryPoints,
            schemasByLocation = schemasByLocation,
            schemaReferenceIndex =
                SourceSchemaReferenceResolver.index(
                    baseUri = baseUri,
                    version = version,
                    schemaEntryPoints = schemaEntryPoints.values,
                ),
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
        childSchemas().forEach { it.visitRecursively(visitor) }
    }

    private fun String.toJsonPointerToken(): String = replace("~", "~0").replace("/", "~1")
}
