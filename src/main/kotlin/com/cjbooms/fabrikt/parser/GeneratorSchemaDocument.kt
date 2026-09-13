package com.cjbooms.fabrikt.parser

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

    private fun isUninhabitable(schema: GeneratorSchema): Boolean =
        GeneratorSchemaTypeClassifier.classify(resolve(schema), ::resolve) is GeneratorSchemaTypeClassification.Uninhabitable

    fun isUninhabitableAt(location: String): Boolean = sourceSchemaAt(location)?.let(::isUninhabitable) == true

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
