package com.cjbooms.fabrikt.parser

import java.net.URI

internal sealed interface SourceSchemaReferenceResolution {
    val value: String

    data class Resolved(
        override val value: String,
        val uri: URI,
        val target: SourceSchema,
    ) : SourceSchemaReferenceResolution

    data class Missing(
        override val value: String,
        val uri: URI,
    ) : SourceSchemaReferenceResolution

    data class External(
        override val value: String,
        val uri: URI,
    ) : SourceSchemaReferenceResolution

    data class Invalid(
        override val value: String,
    ) : SourceSchemaReferenceResolution
}

internal data class SourceSchemaReferenceIndex(
    val documentUri: URI,
    val schemasByUri: Map<URI, SourceSchema>,
    val resourceUris: Set<URI>,
    val resolutionsByLocation: Map<String, SourceSchemaReferenceResolution>,
)
