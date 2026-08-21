package com.cjbooms.fabrikt.util

import com.reprezen.jsonoverlay.JsonLoader
import java.net.URL

/**
 * A [JsonLoader] that sends the configured auth headers when fetching remote
 * documents referenced by `$ref`. The parent [JsonLoader] provides its own caching,
 * so this subclass only applies headers and delegates the actual fetch to the
 * shared [HttpFetch] utility.
 */
class AuthJsonLoader(private val resolvedHeaders: List<Pair<String, String>>) : JsonLoader() {

    override fun load(url: URL) = HttpFetch.fetch(url.toURI(), resolvedHeaders).let {
        super.loadString(url, it)
    }
}
