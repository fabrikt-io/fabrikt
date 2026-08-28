package com.cjbooms.fabrikt.parser

import com.cjbooms.fabrikt.util.OpenApi31Downgrader
import com.fasterxml.jackson.databind.JsonNode
import com.reprezen.jsonoverlay.JsonLoader
import com.reprezen.kaizen.oasparser.model3.OpenApi3
import java.net.URI
import java.nio.file.Paths

internal data class ParsedOpenApiDocument(
    val source: SourceOpenApiDocument,
    val kaizenModel: OpenApi3,
) {
    val version: OpenApiVersion? = source.version
}

internal object OpenApiDocumentParser {
    fun parse(
        input: String,
        baseUri: URI = Paths.get("").toAbsolutePath().toUri(),
        jsonLoader: JsonLoader? = null,
    ): ParsedOpenApiDocument =
        try {
            val source = SourceOpenApiDocumentParser.parse(input)
            val kaizenInput = source.root.deepCopy<JsonNode>()
            OpenApi31Downgrader.downgradeIncompatibleElements(kaizenInput)
            OpenApiInputCleaner.cleanEmptyTypes(kaizenInput)
            val kaizenModel = KaizenParserAdapter.parse(kaizenInput, baseUri.toURL(), jsonLoader)
            ParsedOpenApiDocument(source, kaizenModel)
        } catch (ex: NullPointerException) {
            throw IllegalArgumentException(
                "The Kaizen openapi-parser library threw a NPE exception when parsing this API. " +
                    "This is commonly due to an external schema reference that is unresolvable, " +
                    "possibly due to a lack of internet connection",
                ex,
            )
        }
}
