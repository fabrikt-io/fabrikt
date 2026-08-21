package com.cjbooms.fabrikt.util

import com.beust.jcommander.ParameterException
import com.fasterxml.jackson.databind.JsonNode
import com.reprezen.jsonoverlay.JsonLoader
import java.io.IOException
import java.net.URL

/**
 * A [JsonLoader] that sends the configured auth headers when fetching remote
 * documents referenced by `$ref`. No extra cache: the kaizen parser's
 * [com.reprezen.jsonoverlay.ReferenceManager] loads each document at most once per parse.
 */
class AuthJsonLoader(private val resolvedHeaders: List<Pair<String, String>>) : JsonLoader() {

    override fun load(url: URL): JsonNode = try {
        super.loadString(url, HttpFetch.fetch(url.toURI(), resolvedHeaders))
    } catch (e: ParameterException) {
        throw IOException(e.message, e)
    }
}
