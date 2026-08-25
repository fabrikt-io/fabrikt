package com.cjbooms.fabrikt.util

import com.beust.jcommander.ParameterException
import java.net.URI
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Paths

data class LoadedApi(
    val content: String,
    val baseUri: URI,
)

/**
 * Loads an Open API spec or fragment from either a local file path or an `http(s)` URL.
 */
object ApiFileLoader {
    fun isRemote(value: String): Boolean = runCatching { URI(value).scheme }.getOrNull()?.lowercase() in setOf("http", "https")

    fun load(
        value: String,
        paramName: String,
        resolvedAuth: List<Pair<String, String>> = emptyList(),
    ): LoadedApi = if (isRemote(value)) loadRemote(URI(value), resolvedAuth) else loadLocal(value, paramName)

    private fun loadLocal(
        value: String,
        paramName: String,
    ): LoadedApi {
        val path =
            try {
                Paths.get(value).toAbsolutePath()
            } catch (e: InvalidPathException) {
                throw ParameterException("'$value' is not a valid path for the $paramName option.", e)
            }
        if (Files.notExists(path)) {
            throw ParameterException(
                "Could not find api file '$value', Specify its location with the $paramName option. " +
                    "Use --help for further information.",
            )
        }
        val baseUri = (path.parent ?: path).toUri()
        return LoadedApi(path.toFile().readText(), baseUri)
    }

    private fun loadRemote(
        uri: URI,
        resolvedAuth: List<Pair<String, String>>,
    ): LoadedApi = LoadedApi(HttpFetch.fetch(uri, resolvedAuth), uri.resolve("."))
}
