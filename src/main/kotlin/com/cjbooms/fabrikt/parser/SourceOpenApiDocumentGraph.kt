package com.cjbooms.fabrikt.parser

import java.net.URI
import java.util.ArrayDeque

internal data class SourceSchemaReferenceLocation(
    val documentUri: URI,
    val schemaLocation: String,
)

internal data class SourceDocumentLoadFailure(
    val documentUri: URI,
    val exception: Exception,
)

internal data class SourceOpenApiDocumentGraph(
    val rootDocument: SourceOpenApiDocument,
    val documentsByUri: Map<URI, SourceSchemaDocument>,
    val schemaReferenceResolutions: Map<SourceSchemaReferenceLocation, SourceSchemaReferenceResolution>,
    val loadFailures: Map<URI, SourceDocumentLoadFailure>,
)

internal object SourceOpenApiDocumentGraphParser {
    fun parse(
        input: String,
        documentUri: URI,
        documentLoader: SourceDocumentLoader,
    ): SourceOpenApiDocumentGraph {
        val canonicalDocumentUri = documentUri.withoutFragment().normalize().toAsciiUri()
        val rootDocument = SourceOpenApiDocumentParser.parse(input, canonicalDocumentUri)
        val documents = linkedMapOf(canonicalDocumentUri to rootDocument.asSchemaDocument())
        val loadFailures = linkedMapOf<URI, SourceDocumentLoadFailure>()
        val pendingDocumentUris = ArrayDeque<URI>()
        enqueueExternalDocuments(documents.values.single(), pendingDocumentUris)

        while (pendingDocumentUris.isNotEmpty()) {
            val externalDocumentUri = pendingDocumentUris.removeFirst()
            if (externalDocumentUri in documents || externalDocumentUri in loadFailures) continue
            if (documents.values.any { externalDocumentUri in it.schemaReferenceIndex.resourceUris }) continue

            try {
                val externalDocument =
                    SourceSchemaDocumentParser.parse(
                        documentLoader.load(externalDocumentUri),
                        externalDocumentUri,
                    )
                documents[externalDocumentUri] = externalDocument
                enqueueExternalDocuments(externalDocument, pendingDocumentUris)
            } catch (exception: Exception) {
                loadFailures[externalDocumentUri] = SourceDocumentLoadFailure(externalDocumentUri, exception)
            }
        }

        return SourceOpenApiDocumentGraph(
            rootDocument = rootDocument,
            documentsByUri = documents.toMap(),
            schemaReferenceResolutions = resolveAcrossDocuments(documents.values),
            loadFailures = loadFailures.toMap(),
        )
    }

    private fun enqueueExternalDocuments(
        document: SourceSchemaDocument,
        pendingDocumentUris: ArrayDeque<URI>,
    ) {
        document.schemaReferenceIndex.resolutionsByLocation.values
            .filterIsInstance<SourceSchemaReferenceResolution.External>()
            .map { it.uri.withoutFragment() }
            .forEach(pendingDocumentUris::addLast)
    }

    private fun resolveAcrossDocuments(
        documents: Collection<SourceSchemaDocument>,
    ): Map<SourceSchemaReferenceLocation, SourceSchemaReferenceResolution> {
        val schemasByUri = linkedMapOf<URI, SourceSchema>()
        val resourceUris = linkedSetOf<URI>()
        documents.forEach { document ->
            document.schemaReferenceIndex.schemasByUri.forEach(schemasByUri::putIfAbsent)
            resourceUris.addAll(document.schemaReferenceIndex.resourceUris)
        }

        return buildMap {
            documents.forEach { document ->
                document.schemaReferenceIndex.resolutionsByLocation.forEach { (location, resolution) ->
                    put(
                        SourceSchemaReferenceLocation(document.documentUri, location),
                        resolution.resolveAgainst(schemasByUri, resourceUris),
                    )
                }
            }
        }
    }

    private fun SourceSchemaReferenceResolution.resolveAgainst(
        schemasByUri: Map<URI, SourceSchema>,
        resourceUris: Set<URI>,
    ): SourceSchemaReferenceResolution {
        if (this !is SourceSchemaReferenceResolution.External) return this
        val target = schemasByUri[uri]
        if (target != null) return SourceSchemaReferenceResolution.Resolved(value, uri, target)
        return if (uri.withoutFragment() in resourceUris) {
            SourceSchemaReferenceResolution.Missing(value, uri)
        } else {
            this
        }
    }
}
