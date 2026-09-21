package com.cjbooms.fabrikt.util

import com.beust.jcommander.ParameterException
import java.net.URI
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Paths

data class LoadedApi(
    val content: String,
    val baseUri: URI,
    val documentUri: URI,
)

/**
 * A `--json-schema-file` value split on its first `#`: the file/URL location, and an optional
 * trailing JSON Pointer fragment (RFC 6901) selecting a JSON Schema nested within that
 * location's content.
 */
data class ApiFileReference(
    val location: String,
    val jsonSchemaPointer: String?,
) {
    companion object {
        /**
         * Splits `value` on its first unescaped `#`. `#` inside a local filesystem path is rare
         * enough, and JSON Pointer fragment addressing common enough in this ecosystem's own
         * `$ref` syntax, that a bare `#` always starts a fragment rather than being literal.
         */
        fun parse(value: String): ApiFileReference {
            val separatorIndex = value.indexOf('#')
            return if (separatorIndex < 0) {
                ApiFileReference(value, null)
            } else {
                ApiFileReference(value.substring(0, separatorIndex), value.substring(separatorIndex + 1))
            }
        }
    }
}

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
        return LoadedApi(path.toFile().readText(), baseUri, path.toUri())
    }

    private fun loadRemote(
        uri: URI,
        resolvedAuth: List<Pair<String, String>>,
    ): LoadedApi = LoadedApi(HttpFetch.fetch(uri, resolvedAuth), uri.resolve("."), uri)
}
