package com.cjbooms.fabrikt.parser

import com.cjbooms.fabrikt.model.SchemaSemantics

internal class GeneratorSchemaDocument(
    val version: OpenApiVersion?,
    val componentSchemas: Map<String, GeneratorSchema>,
    private val sourceSchemasByLocation: Map<String, SourceSchema>,
    private val referencedSchemas: Map<GeneratorSchemaIdentity, GeneratorSchema>,
) {
    fun resolve(schema: GeneratorSchema): GeneratorSchema {
        val sourceSchema = schema as? SourceSchema ?: sourceSchemaAt(schema.location)
        return sourceSchema?.let { referencedSchemas[it.identity] } ?: schema
    }

    fun schemaSemanticsAt(location: String): SchemaSemantics {
        val schema = sourceSchemaAt(location) ?: return SchemaSemantics()
        val classification = GeneratorSchemaTypeClassifier.classify(resolve(schema), ::resolve)
        val declaredTypes =
            if (version?.isAtLeast(3, 1) == true && schema is SourceObjectSchema) schema.types else emptySet()
        val nonNullTypes = declaredTypes - SourceSchemaType.NULL
        return SchemaSemantics(
            isUninhabitable = classification is GeneratorSchemaTypeClassification.Uninhabitable,
            hasMultipleNonNullTypes =
                classification is GeneratorSchemaTypeClassification.Unsupported &&
                    classification.reason == GeneratorSchemaTypeClassification.Reason.MULTIPLE_NON_NULL_TYPES,
            declaredType =
                nonNullTypes.singleOrNull()?.takeIf { it is SourceSchemaType.Recognised }?.value,
            isNullable = declaredTypes.takeIf { it.isNotEmpty() }?.contains(SourceSchemaType.NULL),
        )
    }

    private fun sourceSchemaAt(location: String): SourceSchema? =
        sourceSchemasByLocation[location]
            ?: sourceSchemasByLocation["#$location"]
            ?: sourceSchemasByLocation[location.removePrefix("#")]
}

internal fun ParsedOpenApiDocument.toGeneratorSchemaDocument(): GeneratorSchemaDocument {
    val adapter = LegacyGeneratorSchemaAdapter()
    return GeneratorSchemaDocument(
        version = version,
        componentSchemas = kaizenModel.schemas.mapValues { (_, schema) -> adapter.adapt(schema) },
        sourceSchemasByLocation = source.schemasByLocation,
        referencedSchemas =
            source.schemaReferenceResolutions
                .mapNotNull { (location, resolution) ->
                    val sourceSchema = source.schemasByLocation[location] ?: return@mapNotNull null
                    val target = (resolution as? SourceSchemaReferenceResolution.Resolved)?.target ?: return@mapNotNull null
                    sourceSchema.identity to target
                }.toMap(),
    )
}
