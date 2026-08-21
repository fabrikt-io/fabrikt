package com.cjbooms.fabrikt.util

import com.fasterxml.jackson.databind.JsonNode
import com.reprezen.jsonoverlay.JsonLoader
import java.io.IOException
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.net.http.HttpTimeoutException
import java.time.Duration

/**
 * A [JsonLoader] that sends the configured auth headers when fetching remote
 * documents referenced by `$ref`. The parent [JsonLoader] keeps its own private
 * cache, so this subclass maintains a separate cache to avoid re-fetching (and
 * re-sending credentials) for documents referenced more than once.
 */
class AuthJsonLoader(private val resolvedHeaders: List<Pair<String, String>>) : JsonLoader() {

    private val cache = mutableMapOf<String, JsonNode>()

    override fun load(url: URL): JsonNode {
        cache[url.toString()]?.let { return it }

        val content = fetch(url)
        val node = loadString(url, content)
        cache[url.toString()] = node
        return node
    }

    private fun fetch(url: URL): String {
        val requestBuilder = HttpRequest.newBuilder(url.toURI())
            .timeout(REQUEST_TIMEOUT)
            .GET()
        resolvedHeaders.forEach { (name, value) -> requestBuilder.header(name, value) }
        val request = requestBuilder.build()
        return try {
            HTTP_CLIENT.send(request, BodyHandlers.ofString()).also { response ->
                if (response.statusCode() !in 200..299) {
                    throw IOException("Failed to fetch referenced document from '$url': received HTTP status ${response.statusCode()}")
                }
            }.body()
        } catch (e: HttpTimeoutException) {
            throw IOException("Timed out fetching referenced document from '$url'", e)
        } catch (e: IOException) {
            throw IOException("Failed to fetch referenced document from '$url'", e)
        }
    }

    companion object {
        private val CONNECT_TIMEOUT: Duration = Duration.ofSeconds(10)
        private val REQUEST_TIMEOUT: Duration = Duration.ofSeconds(30)

        private val HTTP_CLIENT: HttpClient by lazy {
            HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build()
        }
    }
}
