package com.cjbooms.fabrikt.util

import com.cjbooms.fabrikt.generators.GeneratorUtils.functionName
import com.cjbooms.fabrikt.generators.GeneratorUtils.functionNameFromOperation
import com.cjbooms.fabrikt.generators.GeneratorUtils.mergeParameters
import com.cjbooms.fabrikt.generators.GeneratorUtils.toKCodeName
import com.cjbooms.fabrikt.model.OpenApiMediaType
import com.cjbooms.fabrikt.model.OpenApiOperation
import com.cjbooms.fabrikt.model.OpenApiPath
import com.cjbooms.fabrikt.model.OpenApiRequestBody
import com.cjbooms.fabrikt.model.OpenApiSchema
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.NormalisedString.toKotlinParameterName

internal fun OpenApiSchema.effectiveRequestSchema(): OpenApiSchema {
    var schema = this
    val visited = mutableSetOf<String>()
    while (visited.add(schema.jsonReference) &&
        schema.allOfSchemas.size == 1 &&
        schema.parsedJson
            ?.fieldNames()
            ?.asSequence()
            ?.toSet() == setOf("allOf")
    ) {
        schema = schema.allOfSchemas.single()
    }
    return schema
}

internal fun OpenApiOperation.requestRepresentationGroups(): List<Map<String, OpenApiMediaType>> = requestBody.requestRepresentationGroups()

internal fun OpenApiRequestBody.requestRepresentationGroups(): List<Map<String, OpenApiMediaType>> =
    contentMediaTypes.entries
        .groupBy { entry ->
            val schema = entry.value.schema.effectiveRequestSchema()
            val schemaKey = if (schema.name != null) schema.jsonReference else schema.parsedJson ?: schema.jsonReference
            schemaKey to entry.key.startsWith("multipart/", ignoreCase = true)
        }.values
        .map { entries -> entries.associate { it.key to it.value } }

internal fun SourceApi.requestOperations(path: OpenApiPath): List<Pair<String, OpenApiOperation>> {
    if (path.operations.values.none { it.requestBody.contentMediaTypes.size > 1 }) {
        return path.operations.map { it.key to it.value }
    }
    val reservedNames =
        openApi3.paths.values
            .flatMap { candidate ->
                candidate.operations.flatMap { (verb, operation) -> candidate.operationFunctionNames(verb, operation) }
            }.flatMap { listOf(it, "${it}WithCookieHeader") }
            .toMutableSet()
    return path.operations.flatMap { (verb, operation) ->
        val groups = operation.requestRepresentationGroups()
        if (operation.requestBody.contentMediaTypes.size <= 1) {
            listOf(verb to operation)
        } else {
            groups.mapIndexed { index, content ->
                val suffix =
                    if (index == 0) {
                        ""
                    } else {
                        val baseNames = path.operationFunctionNames(verb, operation)
                        val mediaSuffix =
                            content.keys
                                .first()
                                .replace("*", "Wildcard")
                                .toKCodeName()
                                .capitalized()
                        generateSequence(mediaSuffix) { "${it}Extra" }
                            .first { candidate ->
                                val names = baseNames.flatMap { listOf(it + candidate, it + candidate + "WithCookieHeader") }
                                if (names.none { it in reservedNames }) {
                                    reservedNames.addAll(names)
                                    true
                                } else {
                                    false
                                }
                            }
                    }
                val normalizedContent = content.mapValues { (_, media) -> media.withSchema(media.schema.effectiveRequestSchema()) }
                verb to operation.withRequestContent(normalizedContent, suffix, groups.size > 1)
            }
        }
    }
}

private fun OpenApiPath.operationFunctionNames(
    verb: String,
    operation: OpenApiOperation,
): Set<String> {
    functionNameFromOperation(operation)?.let { return setOf(it) }
    val pathParameters =
        mergeParameters(parameters, operation.parameters)
            .filter { it.`in` == "path" }
            .map { it.name.toKotlinParameterName().capitalized() }
    val ktorName = verb + if (pathParameters.isEmpty()) "" else "By" + pathParameters.joinToString("And")
    return setOf(functionName(operation, pathString, verb), verb, "${verb}ById", ktorName)
}
