package com.cjbooms.fabrikt.parser

import com.cjbooms.fabrikt.util.OpenApi31Downgrader
import com.cjbooms.fabrikt.util.YamlObjectMapper
import com.reprezen.jsonoverlay.JsonLoader
import com.reprezen.kaizen.oasparser.OpenApi3Parser
import com.reprezen.kaizen.oasparser.model3.OpenApi3
import java.net.URI
import java.nio.file.Paths

internal class ParsedOpenApiDocument(
    val sourceContent: String,
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
            OpenApi31Downgrader.downgradeIncompatibleElements(kaizenInput)
            OpenApiInputCleaner.cleanEmptyTypes(kaizenInput)
            val kaizenModel = OpenApi3Parser().parse(kaizenInput, baseUri.toURL(), false, jsonLoader) as OpenApi3
            ParsedOpenApiDocument(input, kaizenModel)
        } catch (ex: NullPointerException) {
            throw IllegalArgumentException(
                "The Kaizen openapi-parser library threw a NPE exception when parsing this API. " +
                    "This is commonly due to an external schema reference that is unresolvable, " +
                    "possibly due to a lack of internet connection",
                ex,
            )
        }
}
