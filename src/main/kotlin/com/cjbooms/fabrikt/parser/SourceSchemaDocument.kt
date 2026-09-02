package com.cjbooms.fabrikt.parser

import com.cjbooms.fabrikt.util.YamlObjectMapper
import com.fasterxml.jackson.databind.JsonNode
import java.net.URI

internal data class SourceSchemaDocument(
    val documentUri: URI,
    val content: String,
    val root: JsonNode,
    val schemaEntryPoints: Map<String, SourceSchema>,
    val schemasByLocation: Map<String, SourceSchema>,
    val schemaReferenceIndex: SourceSchemaReferenceIndex,
)

internal fun SourceOpenApiDocument.asSchemaDocument(): SourceSchemaDocument =
    SourceSchemaDocument(
        documentUri = baseUri.withoutFragment().normalize().toAsciiUri(),
        content = content,
        root = root,
        schemaEntryPoints = schemaEntryPoints,
        schemasByLocation = schemasByLocation,
        schemaReferenceIndex = schemaReferenceIndex,
    )

internal object SourceSchemaDocumentParser {
    fun parse(
        input: String,
        documentUri: URI,
    ): SourceSchemaDocument {
        val canonicalDocumentUri = documentUri.withoutFragment().normalize().toAsciiUri()
        val root = YamlObjectMapper.instance.readTree(input)
        if (OpenApiVersion.parse(root["openapi"]?.asText()) != null && root["info"]?.isObject == true) {
            return SourceOpenApiDocumentParser.parse(input, canonicalDocumentUri).asSchemaDocument()
        }
        require(root.isObject || root.isBoolean) { "External schema document must contain an object or boolean schema: $documentUri" }

        val rootSchema = SourceSchemaParser.parse(root, "#", null)
        val schemasByLocation = indexSchemas(rootSchema)
        return SourceSchemaDocument(
            documentUri = canonicalDocumentUri,
            content = input,
            root = root,
            schemaEntryPoints = mapOf("#" to rootSchema),
            schemasByLocation = schemasByLocation,
            schemaReferenceIndex =
                SourceSchemaReferenceResolver.index(
                    baseUri = canonicalDocumentUri,
                    version = null,
                    schemaEntryPoints = listOf(rootSchema),
                    supportsSchemaResources = true,
                ),
        )
    }

    private fun indexSchemas(rootSchema: SourceSchema): Map<String, SourceSchema> =
        buildMap {
            fun index(schema: SourceSchema) {
                put(schema.location, schema)
                schema.childSchemas().forEach(::index)
            }
            index(rootSchema)
        }
}
