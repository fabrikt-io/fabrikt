package com.cjbooms.fabrikt.model

import com.beust.jcommander.ParameterException
import com.cjbooms.fabrikt.generators.GeneratorUtils.toKCodeName
import com.cjbooms.fabrikt.parser.OpenApiDocumentParser
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.SchemaParserExtensions.isEnumDefinition
import com.cjbooms.fabrikt.util.SchemaParserExtensions.isOneOfSuperInterface
import com.cjbooms.fabrikt.util.SchemaParserExtensions.isSchemaAbsent
import com.cjbooms.fabrikt.util.SchemaParserExtensions.safeName
import com.cjbooms.fabrikt.util.YamlUtils
import com.cjbooms.fabrikt.util.capitalized
import com.cjbooms.fabrikt.util.effectiveRequestSchema
import com.cjbooms.fabrikt.util.requestRepresentationGroups
import com.cjbooms.fabrikt.validation.ValidationError
import com.reprezen.jsonoverlay.JsonLoader
import java.net.URI
import java.nio.file.Paths

data class SchemaInfo(
    val name: String,
    val schema: OpenApiSchema,
    val direction: ModelDirection? = null,
) {
    val typeInfo: KotlinTypeInfo = KotlinTypeInfo.from(schema, name, direction = direction)
}

class SourceApi private constructor(
    private val rawApiSpec: String,
    val baseUri: URI = Paths.get("").toAbsolutePath().toUri(),
    private val jsonLoader: JsonLoader?,
    private val schemaConversion: SchemaConversionOptions? = null,
) {
    constructor(
        rawApiSpec: String,
        baseUri: URI = Paths.get("").toAbsolutePath().toUri(),
        schemaConversion: SchemaConversionOptions? = null,
    ) : this(rawApiSpec, baseUri, null, schemaConversion)

    companion object {
        fun create(
            baseApi: String,
            apiFragments: Collection<String>,
            baseUri: URI = Paths.get("").toAbsolutePath().toUri(),
            jsonLoader: JsonLoader? = null,
            schemaConversion: SchemaConversionOptions? = null,
        ): SourceApi {
            val combinedApi =
                apiFragments.fold(YamlUtils.expandYamlAliases(baseApi)) { acc: String, fragment -> YamlUtils.mergeYamlTrees(acc, fragment) }
            return SourceApi(combinedApi, baseUri, jsonLoader, schemaConversion)
        }

        private const val MAX_NESTED_ARRAY_DEPTH = 10
    }

    val openApi3: OpenApi3Document = OpenApiDocumentParser.parse(rawApiSpec, baseUri, jsonLoader, schemaConversion).asOpenApi3Document()
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

        val inlineObjectParams =
            openApi3.paths.values
                .flatMap { path ->
                    val allParams = path.parameters + path.operations.values.flatMap { it.parameters }
                    allParams.mapNotNull { param ->
                        param.schema.takeIf { it.isOperationLevelObjectOrArray() }?.let { schema ->
                            "${param.name}${if (schema.type == OasType.Array.type) "Item" else ""}" to schema
                        }
                    }
                }.distinctBy { it.second.jsonReference }

        inlineObjectParams.forEach { (name, schema) ->
            schema.preRegisterOperationModel(name)
        }

        val inlineRequestBodySchemas =
            openApi3.requestBodies.entries.flatMap { requestBody ->
                val groups = requestBody.value.requestRepresentationGroups()
                groups.mapNotNull { content ->
                    val rawSchema = content.values.first().schema
                    val schema = if (requestBody.value.contentMediaTypes.size > 1) rawSchema.effectiveRequestSchema() else rawSchema
                    if (schema.jsonPathFromRoot.contains("requestBodies") &&
                        (
                            (schema.oneOfSchemas.isEmpty() && schema.anyOfSchemas.isEmpty()) ||
                                schema.isOneOfSuperInterface()
                        )
                    ) {
                        val name =
                            when {
                                schema.isOneOfSuperInterface() -> schema.safeName()
                                groups.size ==
                                    1 -> requestBody.key
                                else ->
                                    requestBody.key +
                                        content.keys
                                            .first()
                                            .replace("*", "Wildcard")
                                            .toKCodeName()
                                            .capitalized()
                            }
                        val registeredName = ModelNameRegistry.preRegisterByReference(schema, name)
                        content.values.drop(1).forEach { media ->
                            ModelNameRegistry.preRegisterReferenceAlias(media.schema.effectiveRequestSchema(), registeredName)
                        }
                        registeredName to schema
                    } else {
                        null
                    }
                }
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
                            (
                                (schema.oneOfSchemas.isEmpty() && schema.anyOfSchemas.isEmpty()) ||
                                    schema.isOneOfSuperInterface()
                            )
                    }.map { content -> response.key to content.value.schema }
            }

        val inlineOperationResponseSchemas =
            openApi3.paths.entries.flatMap { (pathTemplate, path) ->
                path.operations.entries.mapNotNull { (method, operation) ->
                    val responseSchemas =
                        operation.responses.entries
                            .filter { (status, _) ->
                                status.replace('X', '0').toIntOrNull()?.let { it in 200..399 } == true
                            }.flatMap { (_, response) -> response.contentMediaTypes.values.map { it.schema } }
                            .distinctBy { it.jsonReference }

                    responseSchemas
                        .singleOrNull()
                        ?.takeIf { it.isOperationLevelObjectOrArray() || it.isOneOfSuperInterface() }
                        ?.let { schema ->
                            if (schema.isOneOfSuperInterface()) {
                                return@let schema.safeName() to schema
                            }
                            val name =
                                schema.title?.takeIf { it.isNotBlank() }
                                    ?: operation.operationId?.takeIf { it.isNotBlank() }?.let {
                                        "$it${if (schema.type == OasType.Array.type) "ResponseItem" else "Response"}"
                                    }
                                    ?: "${method}_${pathTemplate}_${if (schema.type == OasType.Array.type) "response_item" else "response"}"
                            name to schema
                        }
                }
            }

        val inlineOperationErrorResponseSchemas =
            openApi3.paths.entries.flatMap { (pathTemplate, path) ->
                path.operations.entries.flatMap { (method, operation) ->
                    operation.responses.entries
                        .filter { (status, _) ->
                            status.equals("default", ignoreCase = true) ||
                                status.replace('X', '0').toIntOrNull()?.let { it in 400..599 } == true
                        }.flatMap { (status, response) ->
                            response.contentMediaTypes.values
                                .map { it.schema }
                                .distinctBy { it.jsonReference }
                                .mapNotNull { schema ->
                                    schema.takeIf { it.isOperationLevelObjectOrArray() || it.isOneOfSuperInterface() }?.let {
                                        val suffix = if (schema.type == OasType.Array.type) "Item" else ""
                                        val name =
                                            if (schema.isOneOfSuperInterface()) {
                                                schema.safeName()
                                            } else {
                                                schema.title?.takeIf { it.isNotBlank() }
                                                    ?: operation.operationId?.takeIf { it.isNotBlank() }?.let {
                                                        "${it}Response$status$suffix"
                                                    }
                                                    ?: "${method}_${pathTemplate}_response_${status}$suffix"
                                            }
                                        name to schema
                                    }
                                }
                        }
                }
            }

        inlineOperationResponseSchemas.forEach { (name, schema) ->
            schema.preRegisterOperationModel(name)
        }

        inlineOperationErrorResponseSchemas.forEach { (name, schema) ->
            schema.preRegisterOperationModel(name)
        }

        val inlineOperationRequestBodySchemas =
            openApi3.paths.entries.flatMap { (pathTemplate, path) ->
                path.operations.entries.flatMap { (method, operation) ->
                    val groups = operation.requestRepresentationGroups()
                    groups.mapNotNull { content ->
                        val rawSchema = content.values.first().schema
                        val schema = if (operation.requestBody.contentMediaTypes.size > 1) rawSchema.effectiveRequestSchema() else rawSchema
                        val isOperationBody = schema.isOperationLevelObjectOrArray() || schema.isOneOfSuperInterface()
                        if (isOperationBody && content.keys.none { it.equals("multipart/form-data", true) }) {
                            val baseName =
                                schema.title?.takeIf { it.isNotBlank() }
                                    ?: operation.operationId?.takeIf { it.isNotBlank() }?.let {
                                        "$it${if (schema.type == OasType.Array.type) "RequestItem" else "Request"}"
                                    }
                                    ?: "${method}_${pathTemplate}_${if (schema.type == OasType.Array.type) "request_item" else "request"}"
                            val name =
                                if (schema.isOneOfSuperInterface()) {
                                    schema.safeName()
                                } else if (groups.size ==
                                    1
                                ) {
                                    baseName
                                } else {
                                    baseName +
                                        content.keys
                                            .first()
                                            .replace("*", "Wildcard")
                                            .toKCodeName()
                                            .capitalized()
                                }
                            val registeredName = ModelNameRegistry.preRegisterByReference(schema, name)
                            content.values.drop(1).forEach { media ->
                                val alias = media.schema.effectiveRequestSchema()
                                ModelNameRegistry.preRegisterReferenceAlias(alias, registeredName)
                                if (alias.type == OasType.Array.type) {
                                    ModelNameRegistry.preRegisterReferenceAlias(alias.itemsSchema, registeredName)
                                }
                            }
                            registeredName to schema
                        } else {
                            null
                        }
                    }
                }
            }

        inlineOperationRequestBodySchemas.forEach { (name, schema) ->
            schema.preRegisterOperationModel(name)
        }

        inlineResponseSchemas.forEach { (name, schema) ->
            ModelNameRegistry.preRegisterByReference(schema, name)
        }

        allSchemas =
            openApi3.schemas.entries
                .map { it.key to it.value }
                .plus(openApi3.parameters.entries.map { it.key to it.value.schema })
                .plus(inlineResponseSchemas)
                .plus(inlineOperationResponseSchemas)
                .plus(inlineOperationErrorResponseSchemas)
                .plus(inlineOperationRequestBodySchemas)
                .plus(inlineRequestBodySchemas)
                .plus(inlineEnumParams)
                .plus(inlineObjectParams)
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

    private fun OpenApiSchema.isDirectObject(): Boolean =
        properties.isNotEmpty() && oneOfSchemas.isEmpty() && anyOfSchemas.isEmpty() && allOfSchemas.isEmpty()

    private fun OpenApiSchema.isArrayOfOperationObjects(): Boolean =
        type == OasType.Array.type &&
            itemsSchema.jsonPathFromRoot.contains("paths") &&
            (itemsSchema.isDirectObject() || itemsSchema.isAllOfObject())

    private fun OpenApiSchema.isOperationLevelObjectOrArray(): Boolean =
        jsonPathFromRoot.contains("paths") &&
            (isDirectObject() || isAllOfObject() || isArrayOfOperationObjects())

    private fun OpenApiSchema.isAllOfObject(): Boolean = allOfSchemas.isNotEmpty() && oneOfSchemas.isEmpty() && anyOfSchemas.isEmpty()

    private fun OpenApiSchema.preRegisterOperationModel(name: String) {
        val registeredName = ModelNameRegistry.preRegisterByReference(this, name)
        if (type == OasType.Array.type) {
            ModelNameRegistry.preRegisterReferenceAlias(itemsSchema, registeredName)
        }
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
