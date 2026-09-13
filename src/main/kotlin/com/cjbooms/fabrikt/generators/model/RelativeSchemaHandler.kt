package com.cjbooms.fabrikt.generators.model

import com.cjbooms.fabrikt.model.OpenApi3Document
import com.cjbooms.fabrikt.util.NormalisedString.toModelClassName
import com.cjbooms.fabrikt.util.YamlUtils
import com.fasterxml.jackson.databind.JsonNode
import java.net.URL

object RelativeSchemaHandler {
    fun maybeConvertRelativeSchemaFile(
        documentUrl: String,
        input: OpenApi3Document,
    ): OpenApi3Document {
        val url = URL(documentUrl)
        val rootNode = input.parsedJson
        if (rootNode == null || !rootNode.isObject || rootNode.has("openapi") || input.schemas.isNotEmpty()) {
            return input
        }
        val modelName =
            url.file
                .substringAfterLast('/')
                .substringBeforeLast('.')
                .toModelClassName()
        val wrapped =
            YamlUtils.objectMapper.createObjectNode().apply {
                set<JsonNode>("openapi", textNode("3.0.0"))
                set<JsonNode>(
                    "info",
                    objectNode().apply {
                        set<JsonNode>("title", textNode(""))
                        set<JsonNode>("version", textNode(""))
                    },
                )
                set<JsonNode>(
                    "components",
                    objectNode().apply {
                        set<JsonNode>(
                            "schemas",
                            objectNode().apply {
                                set<JsonNode>(modelName, rootNode)
                            },
                        )
                    },
                )
            }
        return OpenApi3Document(YamlUtils.parseOpenApi(YamlUtils.objectMapper.writeValueAsString(wrapped), url.toURI()))
    }
}
