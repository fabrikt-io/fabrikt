package com.cjbooms.fabrikt.util

import com.beust.jcommander.ParameterException
import com.reprezen.jsonoverlay.JsonLoader
import java.io.IOException
import java.net.URL

/**
 * A [JsonLoader] that sends the configured auth headers when fetching remote
 * documents referenced by `$ref`. Fetches via the shared [HttpFetch] utility;
 * the kaizen parser's [com.reprezen.jsonoverlay.ReferenceManager] ensures each
 * document is loaded at most once per parse, so no additional cache is needed.
 */
class AuthJsonLoader(private val resolvedHeaders: List<Pair<String, String>>) : JsonLoader() {

    override fun load(url: URL) = try {
        super.loadString(url, HttpFetch.fetch(url.toURI(), resolvedHeaders))
    } catch (e: ParameterException) {
        throw IOException(e.message, e)
    }
}
