package com.cjbooms.fabrikt.parser

import com.fasterxml.jackson.databind.JsonNode
import com.reprezen.jsonoverlay.JsonLoader
import com.reprezen.kaizen.oasparser.OpenApi3Parser
import com.reprezen.kaizen.oasparser.model3.OpenApi3
import java.net.URL

internal object KaizenParserAdapter {
    fun parse(
        input: JsonNode,
        baseUrl: URL,
        jsonLoader: JsonLoader?,
    ): OpenApi3 = OpenApi3Parser().parse(input, baseUrl, false, jsonLoader) as OpenApi3

    fun parse(url: URL): OpenApi3 = OpenApi3Parser().parse(url)
}
