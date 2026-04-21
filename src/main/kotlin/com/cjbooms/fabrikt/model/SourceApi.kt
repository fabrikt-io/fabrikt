package com.cjbooms.fabrikt.model

import com.beust.jcommander.ParameterException
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isEnumDefinition
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isNotDefined
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.YamlUtils
import com.cjbooms.fabrikt.validation.ValidationError
import com.reprezen.jsonoverlay.Overlay
import com.reprezen.kaizen.oasparser.model3.OpenApi3
import com.reprezen.kaizen.oasparser.model3.Schema
import java.nio.file.Path
import java.nio.file.Paths
import java.util.logging.Logger

data class SchemaInfo(val name: String, val schema: Schema) {
    val typeInfo: KotlinTypeInfo = KotlinTypeInfo.from(schema, name)
}

data class SourceApi(
    private val rawApiSpec: String,
    val baseDir: Path = Paths.get("").toAbsolutePath(),
) {
    companion object {
        fun create(
            baseApi: String,
            apiFragments: Collection<String>,
            baseDir: Path = Paths.get("").toAbsolutePath(),
        ): SourceApi {
            val combinedApi =
                apiFragments.fold(YamlUtils.expandYamlAliases(baseApi)) { acc: String, fragment -> YamlUtils.mergeYamlTrees(acc, fragment) }
            return SourceApi(combinedApi, baseDir)
        }
    }

    val openApi3: OpenApi3 = YamlUtils.parseOpenApi(rawApiSpec, baseDir)
    val allSchemas: List<SchemaInfo>

    init {
        validateSchemaObjects(openApi3).let {
            if (it.isNotEmpty()) throw ParameterException("Invalid models or api file:\n${it.joinToString("\n\t")}")
        }

        val inlineEnumParams = openApi3.paths.values.flatMap { path ->
            val allParams = path.parameters + path.operations.values.flatMap { it.parameters }
            allParams.filter { param ->
                Overlay.of(param.schema).pathFromRoot.contains("paths") &&
                    (param.schema?.isEnumDefinition() == true ||
                        (param.schema?.type == "array" && param.schema?.itemsSchema?.isEnumDefinition() == true))
            }.map { param ->
                val schema = if (param.schema?.type == "array") param.schema.itemsSchema else param.schema
                param.name to schema
            }
        }.distinctBy { Overlay.of(it.second).jsonReference }

        inlineEnumParams.forEach { (name, schema) ->
            ModelNameRegistry.preRegisterByReference(schema, name)
        }

        val inlineRequestBodySchemas = openApi3.requestBodies.entries.flatMap { requestBody ->
            requestBody.value.contentMediaTypes.entries
                .filter { content ->
                    val schema = content.value.schema
                    Overlay.of(schema).pathFromRoot.contains("requestBodies") &&
                        schema.oneOfSchemas.isNullOrEmpty() &&
                        schema.anyOfSchemas.isNullOrEmpty()
                }
                .map { content -> requestBody.key to content.value.schema }
        }

        inlineRequestBodySchemas.forEach { (name, schema) ->
            ModelNameRegistry.preRegisterByReference(schema, name)
        }

        val inlineResponseSchemas = openApi3.responses.entries.flatMap { response ->
            response.value.contentMediaTypes.entries
                .filter { content ->
                    val schema = content.value.schema
                    Overlay.of(schema).pathFromRoot.contains("responses") &&
                        schema.oneOfSchemas.isNullOrEmpty() &&
                        schema.anyOfSchemas.isNullOrEmpty()
                }
                .map { content -> response.key to content.value.schema }
        }

        inlineResponseSchemas.forEach { (name, schema) ->
            ModelNameRegistry.preRegisterByReference(schema, name)
        }

        allSchemas = openApi3.schemas.entries.map { it.key to it.value }
            .plus(openApi3.parameters.entries.map { it.key to it.value.schema })
            .plus(inlineResponseSchemas)
            .plus(inlineRequestBodySchemas)
            .plus(inlineEnumParams)
            .map { (key, schema) -> SchemaInfo(key, schema) }
    }

    private fun validateSchemaObjects(api: OpenApi3): List<ValidationError> {
        val schemaErrors = api.schemas.entries.fold(emptyList<ValidationError>()) { errors, entry ->
            val name = entry.key
            val schema = entry.value
            if (schema.type == OasType.Object.type && schema.properties?.isNotEmpty() == true && (
                    schema.oneOfSchemas?.isNotEmpty() == true ||
                        schema.allOfSchemas?.isNotEmpty() == true ||
                        schema.anyOfSchemas?.isNotEmpty() == true
                    )
            ) {
                errors + listOf(
                    ValidationError(
                        "'$name' schema contains an invalid combination of properties and `oneOf | anyOf | allOf`. " +
                            "Do not use properties and a combiner at the same level.",
                    ),
                )
            } else {
                errors
            }
        }

        return api.schemas.map { it.value.properties }.flatMap { it.entries }
            .fold(schemaErrors) { lst, entry ->
                val name = entry.key
                val schema = entry.value
                if (schema.isNotDefined()) {
                    lst + listOf(ValidationError("Property '$name' cannot be parsed to a Schema. Check your input"))
                } else {
                    lst
                }
            }
    }
}
