package com.cjbooms.fabrikt.parser

import java.net.URI

internal object SourceSchemaReferenceResolver {
    fun index(
        baseUri: URI,
        version: OpenApiVersion?,
        schemaEntryPoints: Collection<SourceSchema>,
        supportsSchemaResources: Boolean = version?.isAtLeast(3, 1) == true,
    ): SourceSchemaReferenceIndex {
        val index = ReferenceIndex(baseUri.withoutFragment().toAsciiUri(), supportsSchemaResources)
        schemaEntryPoints.forEach(index::index)
        return index.build()
    }

    private class ReferenceIndex(
        private val documentUri: URI,
        private val supportsSchemaResources: Boolean,
    ) {
        private val schemasByUri = linkedMapOf<URI, SourceSchema>()
        private val baseUrisByLocation = linkedMapOf<String, URI>()
        private val schemasByLocation = linkedMapOf<String, SourceSchema>()
        private val resourceUris = linkedSetOf(documentUri)

        fun index(schema: SourceSchema) {
            index(schema, Scope(documentUri, documentUri, "#"))
        }

        fun build(): SourceSchemaReferenceIndex =
            SourceSchemaReferenceIndex(
                documentUri = documentUri,
                schemasByUri = schemasByUri.toMap(),
                resourceUris = resourceUris.toSet(),
                resolutionsByLocation =
                    schemasByLocation.values
                        .asSequence()
                        .filterIsInstance<SourceObjectSchema>()
                        .mapNotNull { schema ->
                            schema.reference?.let { value -> schema.location to resolve(schema, value) }
                        }.toMap(linkedMapOf()),
            )

        private fun index(
            schema: SourceSchema,
            inheritedScope: Scope,
        ) {
            val scope = schema.scope(inheritedScope)
            schemasByLocation.putIfAbsent(schema.location, schema)
            baseUrisByLocation.putIfAbsent(schema.location, scope.baseUri)
            schemasByUri.putIfAbsent(documentUri.withFragment(schema.location.removePrefix("#")), schema)
            schemasByUri.putIfAbsent(scope.uriFor(schema.location), schema)

            if (schema is SourceObjectSchema && supportsSchemaResources) {
                schema.anchor
                    ?.takeIf(ANCHOR_PATTERN::matches)
                    ?.let { anchor -> schemasByUri.putIfAbsent(scope.resourceUri.withFragment(anchor), schema) }
            }

            schema.childSchemas().forEach { child -> index(child, scope) }
        }

        private fun SourceSchema.scope(inherited: Scope): Scope {
            if (this !is SourceObjectSchema || !supportsSchemaResources) return inherited
            val resolvedIdentifier = identifier?.resolveAgainst(inherited.baseUri) ?: return inherited
            if (!resolvedIdentifier.hasEmptyFragment()) return inherited

            val resourceUri = resolvedIdentifier.withoutFragment()
            resourceUris.add(resourceUri)
            return Scope(resourceUri, resourceUri, location)
        }

        private fun resolve(
            schema: SourceObjectSchema,
            value: String,
        ): SourceSchemaReferenceResolution {
            val resolvedUri =
                value.resolveAgainst(baseUrisByLocation.getValue(schema.location))
                    ?: return SourceSchemaReferenceResolution.Invalid(value)
            val canonicalUri = resolvedUri.withoutEmptyFragment()
            val target = schemasByUri[canonicalUri]
            if (target != null) return SourceSchemaReferenceResolution.Resolved(value, canonicalUri, target)

            return if (canonicalUri.withoutFragment() in resourceUris) {
                SourceSchemaReferenceResolution.Missing(value, canonicalUri)
            } else {
                SourceSchemaReferenceResolution.External(value, canonicalUri)
            }
        }
    }

    private data class Scope(
        val baseUri: URI,
        val resourceUri: URI,
        val resourceRootLocation: String,
    ) {
        fun uriFor(location: String): URI {
            if (location == resourceRootLocation) return resourceUri
            val pointer =
                if (resourceRootLocation == "#") {
                    location.removePrefix("#")
                } else {
                    location.removePrefix(resourceRootLocation)
                }
            return resourceUri.withFragment(pointer)
        }
    }

    private fun URI.hasEmptyFragment(): Boolean = rawFragment.isNullOrEmpty()

    private fun URI.withoutEmptyFragment(): URI = if (rawFragment == "") withoutFragment() else this

    private val ANCHOR_PATTERN = Regex("^[A-Za-z_][-A-Za-z0-9._]*$")
}
