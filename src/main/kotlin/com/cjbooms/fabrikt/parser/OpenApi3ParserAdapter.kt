package com.cjbooms.fabrikt.parser

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.reprezen.jsonoverlay.JsonLoader
import com.reprezen.jsonoverlay.MapOverlay
import com.reprezen.jsonoverlay.Overlay
import com.reprezen.kaizen.oasparser.OpenApi3Parser
import com.reprezen.kaizen.oasparser.model3.OpenApi3
import com.reprezen.kaizen.oasparser.model3.Operation
import com.reprezen.kaizen.oasparser.model3.Path
import com.reprezen.kaizen.oasparser.ovl3.OperationImpl
import java.net.URL
import java.util.logging.Logger

internal object OpenApi3ParserAdapter {
    fun parse(
        input: JsonNode,
        baseUrl: URL,
        jsonLoader: JsonLoader?,
    ): OpenApi3 = OpenApi3Parser().parse(input, baseUrl, false, jsonLoader) as OpenApi3

    fun parseQueryOperations(path: Path): Map<String, Operation> {
        val pathOverlay = Overlay.of(path)
        val query = pathOverlay.parsedJson?.get("query")
        return when {
            query == null -> emptyMap()
            !query.isObject || !query.path("responses").isObject || query.path("responses").isEmpty -> {
                Logger.getGlobal().warning(
                    "Ignoring malformed QUERY ${path.pathString}; provide an Operation Object with non-empty responses.",
                )
                emptyMap()
            }
            else -> {
                // Parse QUERY as a document fragment so inline schemas retain their original JSON pointers.
                val manager =
                    pathOverlay.root
                        ._getCreatingRef()
                        .manager
                        .getManagerFor(URL(pathOverlay.jsonReference.substringBefore('#')))
                val queryPath = JsonNodeFactory.instance.objectNode().set<JsonNode>("query", query)
                val operations =
                    MapOverlay
                        .getFactory(OperationImpl.factory, "query")
                        .create(queryPath, null, manager)
                operations._setCreatingRef(
                    manager.getReference("${pathOverlay.jsonReference.substringBefore('#')}#${pathOverlay.pathFromRoot}"),
                )
                Overlay.get(operations)
            }
        }
    }
}
