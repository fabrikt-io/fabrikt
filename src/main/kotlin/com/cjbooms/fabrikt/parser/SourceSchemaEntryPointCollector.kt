package com.cjbooms.fabrikt.parser

import com.fasterxml.jackson.databind.JsonNode

internal object SourceSchemaEntryPointCollector {
    fun collect(
        root: JsonNode,
        version: OpenApiVersion?,
    ): Map<String, JsonNode> = Collector(version).collect(root)

    private class Collector(
        version: OpenApiVersion?,
    ) {
        private val entryPoints = linkedMapOf<String, JsonNode>()
        private val supportsOpenApi31 = version?.isAtLeast(3, 1) == true
        private val supportsOpenApi32 = version?.isAtLeast(3, 2) == true

        fun collect(root: JsonNode): Map<String, JsonNode> {
            collectComponents(root["components"], "#/components")
            collectPathItemMap(root["paths"], "#/paths", pathsOnly = true)
            if (supportsOpenApi31) collectPathItemMap(root["webhooks"], "#/webhooks")
            return entryPoints
        }

        private fun collectComponents(
            components: JsonNode?,
            location: String,
        ) {
            collectSchemaMap(components?.get("schemas"), "$location/schemas")
            collectObjectMap(components?.get("parameters"), "$location/parameters", ::collectParameter)
            collectObjectMap(components?.get("headers"), "$location/headers", ::collectHeader)
            collectObjectMap(components?.get("requestBodies"), "$location/requestBodies", ::collectRequestBody)
            collectObjectMap(components?.get("responses"), "$location/responses", ::collectResponse)
            collectObjectMap(components?.get("callbacks"), "$location/callbacks", ::collectCallback)
            if (supportsOpenApi31) {
                collectObjectMap(components?.get("pathItems"), "$location/pathItems", ::collectPathItem)
            }
            if (supportsOpenApi32) {
                collectObjectMap(components?.get("mediaTypes"), "$location/mediaTypes", ::collectMediaType)
            }
        }

        private fun collectPathItemMap(
            pathItems: JsonNode?,
            location: String,
            pathsOnly: Boolean = false,
            skipSpecificationExtensions: Boolean = false,
        ) {
            if (pathItems?.isObject != true) return

            pathItems
                .properties()
                .filter { (name, _) -> !pathsOnly || name.startsWith("/") }
                .filterNot { (name, _) -> skipSpecificationExtensions && name.isSpecificationExtension() }
                .forEach { (name, pathItem) -> collectPathItem(pathItem, "$location/${name.toJsonPointerToken()}") }
        }

        private fun collectPathItem(
            pathItem: JsonNode?,
            location: String,
        ) {
            if (pathItem?.isObject != true) return
            collectParameters(pathItem["parameters"], "$location/parameters")
            operationNames.forEach { operationName ->
                collectOperation(pathItem[operationName], "$location/$operationName")
            }
            if (supportsOpenApi32) {
                collectObjectMap(
                    pathItem["additionalOperations"],
                    "$location/additionalOperations",
                    ::collectOperation,
                )
            }
        }

        private fun collectOperation(
            operation: JsonNode?,
            location: String,
        ) {
            if (operation?.isObject != true) return

            collectParameters(operation["parameters"], "$location/parameters")
            collectRequestBody(operation["requestBody"], "$location/requestBody")
            collectResponses(operation["responses"], "$location/responses")
            collectObjectMap(operation["callbacks"], "$location/callbacks", ::collectCallback)
        }

        private fun collectParameters(
            parameters: JsonNode?,
            location: String,
        ) {
            if (parameters?.isArray != true) return
            parameters.forEachIndexed { index, parameter -> collectParameter(parameter, "$location/$index") }
        }

        private fun collectParameter(
            parameter: JsonNode?,
            location: String,
        ) {
            if (!parameter.isInlineObject()) return
            collectSchema(parameter?.get("schema"), "$location/schema")
            collectContent(parameter?.get("content"), "$location/content")
        }

        private fun collectHeader(
            header: JsonNode?,
            location: String,
        ) {
            if (!header.isInlineObject()) return
            collectSchema(header?.get("schema"), "$location/schema")
            collectContent(header?.get("content"), "$location/content")
        }

        private fun collectRequestBody(
            requestBody: JsonNode?,
            location: String,
        ) {
            if (!requestBody.isInlineObject()) return
            collectContent(requestBody?.get("content"), "$location/content")
        }

        private fun collectResponses(
            responses: JsonNode?,
            location: String,
        ) = collectObjectMap(responses, location, ::collectResponse, skipSpecificationExtensions = true)

        private fun collectResponse(
            response: JsonNode?,
            location: String,
        ) {
            if (!response.isInlineObject()) return
            collectObjectMap(response?.get("headers"), "$location/headers", ::collectHeader)
            collectContent(response?.get("content"), "$location/content")
        }

        private fun collectCallback(
            callback: JsonNode?,
            location: String,
        ) {
            if (!callback.isInlineObject()) return
            collectPathItemMap(callback, location, skipSpecificationExtensions = true)
        }

        private fun collectContent(
            content: JsonNode?,
            location: String,
        ) = collectObjectMap(content, location, ::collectMediaType)

        private fun collectMediaType(
            mediaType: JsonNode?,
            location: String,
        ) {
            if (!mediaType.isInlineObject()) return
            collectSchema(mediaType?.get("schema"), "$location/schema")
            if (supportsOpenApi32) collectSchema(mediaType?.get("itemSchema"), "$location/itemSchema")
            collectObjectMap(mediaType?.get("encoding"), "$location/encoding", ::collectEncoding)
            if (supportsOpenApi32) {
                collectEncodingArray(mediaType?.get("prefixEncoding"), "$location/prefixEncoding")
                collectEncoding(mediaType?.get("itemEncoding"), "$location/itemEncoding")
            }
        }

        private fun collectEncoding(
            encoding: JsonNode?,
            location: String,
        ) {
            if (encoding?.isObject != true) return
            collectObjectMap(encoding["headers"], "$location/headers", ::collectHeader)
            if (supportsOpenApi32) {
                collectObjectMap(encoding["encoding"], "$location/encoding", ::collectEncoding)
                collectEncodingArray(encoding["prefixEncoding"], "$location/prefixEncoding")
                collectEncoding(encoding["itemEncoding"], "$location/itemEncoding")
            }
        }

        private fun collectEncodingArray(
            encodings: JsonNode?,
            location: String,
        ) {
            if (encodings?.isArray != true) return
            encodings.forEachIndexed { index, encoding -> collectEncoding(encoding, "$location/$index") }
        }

        private fun collectSchemaMap(
            schemas: JsonNode?,
            location: String,
        ) {
            if (schemas?.isObject != true) return
            schemas.properties().forEach { (name, schema) ->
                collectSchema(schema, "$location/${name.toJsonPointerToken()}")
            }
        }

        private fun collectSchema(
            schema: JsonNode?,
            location: String,
        ) {
            if (schema?.isObject == true || schema?.isBoolean == true) entryPoints[location] = schema
        }

        private fun collectObjectMap(
            objects: JsonNode?,
            location: String,
            collector: (JsonNode?, String) -> Unit,
            skipSpecificationExtensions: Boolean = false,
        ) {
            if (objects?.isObject != true) return
            objects
                .properties()
                .filterNot { (name, _) -> skipSpecificationExtensions && name.isSpecificationExtension() }
                .forEach { (name, value) -> collector(value, "$location/${name.toJsonPointerToken()}") }
        }

        private fun JsonNode?.isInlineObject(): Boolean = this?.isObject == true && this["\$ref"]?.isTextual != true

        private fun String.isSpecificationExtension(): Boolean = startsWith("x-", ignoreCase = true)

        private fun String.toJsonPointerToken(): String = replace("~", "~0").replace("/", "~1")

        private val operationNames: List<String>
            get() = if (supportsOpenApi32) OPERATION_NAMES + "query" else OPERATION_NAMES

        private companion object {
            val OPERATION_NAMES = listOf("get", "put", "post", "delete", "options", "head", "patch", "trace")
        }
    }
}
