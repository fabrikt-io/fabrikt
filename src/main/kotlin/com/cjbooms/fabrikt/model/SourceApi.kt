package com.cjbooms.fabrikt.model

import com.beust.jcommander.ParameterException
import com.cjbooms.fabrikt.parser.OpenApiDocumentParser
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.SchemaParserExtensions.isEnumDefinition
import com.cjbooms.fabrikt.util.SchemaParserExtensions.isSchemaAbsent
import com.cjbooms.fabrikt.util.YamlUtils
import com.cjbooms.fabrikt.validation.ValidationError
import com.reprezen.jsonoverlay.JsonLoader
import java.net.URI
import java.nio.file.Paths

data class SchemaInfo(
    val name: String,
    val schema: OpenApiSchema,
) {
    val typeInfo: KotlinTypeInfo = KotlinTypeInfo.from(schema, name)
}

class SourceApi private constructor(
    private val rawApiSpec: String,
    val baseUri: URI = Paths.get("").toAbsolutePath().toUri(),
    private val jsonLoader: JsonLoader?,
) {
    constructor(
        rawApiSpec: String,
        baseUri: URI = Paths.get("").toAbsolutePath().toUri(),
    ) : this(rawApiSpec, baseUri, null)

    companion object {
        fun create(
            baseApi: String,
            apiFragments: Collection<String>,
            baseUri: URI = Paths.get("").toAbsolutePath().toUri(),
            jsonLoader: JsonLoader? = null,
        ): SourceApi {
            val combinedApi =
                apiFragments.fold(YamlUtils.expandYamlAliases(baseApi)) { acc: String, fragment -> YamlUtils.mergeYamlTrees(acc, fragment) }
            return SourceApi(combinedApi, baseUri, jsonLoader)
        }

        private const val MAX_NESTED_ARRAY_DEPTH = 10
    }

    val openApi3: OpenApi3Document = OpenApiDocumentParser.parse(rawApiSpec, baseUri, jsonLoader).asOpenApi3Document()
    val allSchemas: List<SchemaInfo>

    init {
        validateSchemaObjects(openApi3).let {
            if (it.isNotEmpty()) throw ParameterException("Invalid models or api file:\n${it.joinToString("\n\t")}")
        }

        val inlineEnumParams =
            openApi3.paths.values
                .flatMap { path ->
                    val allParams = path.parameters + path.operations.values.flatMap { it.parameters }
                    allParams.mapNotNull { param ->
                        innermostInlineEnum(param.schema)?.let { param.name to it }
                    }
                }.distinctBy { it.second.jsonReference }

        inlineEnumParams.forEach { (name, schema) ->
            ModelNameRegistry.preRegisterByReference(schema, name)
        }

        val inlineRequestBodySchemas =
            openApi3.requestBodies.entries.flatMap { requestBody ->
                requestBody.value.contentMediaTypes.entries
                    .filter { content ->
                        val schema = content.value.schema
                        schema.jsonPathFromRoot.contains("requestBodies") &&
                            schema.oneOfSchemas.isEmpty() &&
                            schema.anyOfSchemas.isEmpty()
                    }.map { content -> requestBody.key to content.value.schema }
            }

        inlineRequestBodySchemas.forEach { (name, schema) ->
            ModelNameRegistry.preRegisterByReference(schema, name)
        }

        val inlineResponseSchemas =
            openApi3.responses.entries.flatMap { response ->
                response.value.contentMediaTypes.entries
                    .filter { content ->
                        val schema = content.value.schema
                        schema.jsonPathFromRoot.contains("responses") &&
                            schema.oneOfSchemas.isEmpty() &&
                            schema.anyOfSchemas.isEmpty()
                    }.map { content -> response.key to content.value.schema }
            }

        inlineResponseSchemas.forEach { (name, schema) ->
            ModelNameRegistry.preRegisterByReference(schema, name)
        }

        allSchemas =
            openApi3.schemas.entries
                .map { it.key to it.value }
                .plus(openApi3.parameters.entries.map { it.key to it.value.schema })
                .plus(inlineResponseSchemas)
                .plus(inlineRequestBodySchemas)
                .plus(inlineEnumParams)
                .map { (key, schema) -> SchemaInfo(key, schema) }
    }

    private fun isInlineEnum(schema: OpenApiSchema?): Boolean =
        schema?.jsonPathFromRoot?.contains("paths") == true &&
            schema?.isEnumDefinition() == true

    private fun innermostInlineEnum(schema: OpenApiSchema?): OpenApiSchema? {
        var current = schema ?: return null
        var depth = 0
        while (current.type == OasType.Array.type && depth < MAX_NESTED_ARRAY_DEPTH) {
            val items = current.itemsSchema
            if (items.isSchemaAbsent()) return null
            current = items
            depth++
        }
        return current.takeIf { isInlineEnum(it) }
    }

    private fun validateSchemaObjects(api: OpenApi3Document): List<ValidationError> {
        val schemaErrors =
            api.schemas.entries.fold(emptyList<ValidationError>()) { errors, entry ->
                val name = entry.key
                val schema = entry.value
                if (schema.type == OasType.Object.type &&
                    schema.properties.isNotEmpty() &&
                    (
                        schema.oneOfSchemas.isNotEmpty() ||
                            schema.allOfSchemas.isNotEmpty() ||
                            schema.anyOfSchemas.isNotEmpty()
                    )
                ) {
                    errors +
                        listOf(
                            ValidationError(
                                "'$name' schema contains an invalid combination of properties and `oneOf | anyOf | allOf`. " +
                                    "Do not use properties and a combiner at the same level.",
                            ),
                        )
                } else {
                    errors
                }
            }

        return api.schemas
            .map { it.value.properties }
            .flatMap { it.entries }
            .fold(schemaErrors) { lst, entry ->
                val name = entry.key
                val schema = entry.value
                if (schema.isSchemaAbsent() && !schema.isUninhabitable) {
                    lst + listOf(ValidationError("Property '$name' cannot be parsed to a Schema. Check your input"))
                } else {
                    lst
                }
            }
    }
}
