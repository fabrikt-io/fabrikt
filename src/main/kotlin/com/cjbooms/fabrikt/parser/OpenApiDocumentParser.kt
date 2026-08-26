package com.cjbooms.fabrikt.parser

import com.cjbooms.fabrikt.util.OpenApi31Downgrader
import com.cjbooms.fabrikt.util.YamlObjectMapper
import com.reprezen.jsonoverlay.JsonLoader
import com.reprezen.kaizen.oasparser.model3.OpenApi3
import java.net.URI
import java.nio.file.Paths

internal class ParsedOpenApiDocument(
    val sourceContent: String,
    val version: OpenApiVersion?,
    val kaizenModel: OpenApi3,
)

internal object OpenApiDocumentParser {
    fun parse(
        input: String,
        baseUri: URI = Paths.get("").toAbsolutePath().toUri(),
        jsonLoader: JsonLoader? = null,
    ): ParsedOpenApiDocument =
        try {
            val kaizenInput = YamlObjectMapper.instance.readTree(input)
            val version = OpenApiVersion.parse(kaizenInput["openapi"]?.asText())
            OpenApi31Downgrader.downgradeIncompatibleElements(kaizenInput)
            OpenApiInputCleaner.cleanEmptyTypes(kaizenInput)
            val kaizenModel = KaizenParserAdapter.parse(kaizenInput, baseUri.toURL(), jsonLoader)
            ParsedOpenApiDocument(input, version, kaizenModel)
        } catch (ex: NullPointerException) {
            throw IllegalArgumentException(
                "The Kaizen openapi-parser library threw a NPE exception when parsing this API. " +
                    "This is commonly due to an external schema reference that is unresolvable, " +
                    "possibly due to a lack of internet connection",
                ex,
            )
        }
}
