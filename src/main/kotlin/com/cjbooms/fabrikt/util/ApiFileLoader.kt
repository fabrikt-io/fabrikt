package com.cjbooms.fabrikt.util

import com.beust.jcommander.ParameterException
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.net.http.HttpTimeoutException
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Paths
import java.time.Duration
import java.util.concurrent.TimeUnit

data class LoadedApi(val content: String, val baseUri: URI)

/**
 * Loads an Open API spec or fragment from either a local file path or an `http(s)` URL.
 */
object ApiFileLoader {

    private val CONNECT_TIMEOUT: Duration = Duration.ofSeconds(10)
    private val REQUEST_TIMEOUT: Duration = Duration.ofSeconds(30)
    private val SHELL_TIMEOUT: Duration = Duration.ofSeconds(10)
    private val ENV_VAR_NAME_REGEX = Regex("^[A-Za-z_][A-Za-z0-9_]*$")
    private val ENV_VAR_PLACEHOLDER_REGEX = Regex("\\$\\{([A-Za-z_][A-Za-z0-9_]*)(?::-([^}]*))?\\}")

    private val httpClient: HttpClient by lazy {
        HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()
    }

    fun isRemote(value: String): Boolean =
        runCatching { URI(value).scheme }.getOrNull()?.lowercase() in setOf("http", "https")

    fun load(value: String, paramName: String, resolvedAuth: List<Pair<String, String>> = emptyList()): LoadedApi =
        if (isRemote(value)) loadRemote(URI(value), resolvedAuth) else loadLocal(value, paramName)

    fun resolveHeaders(headers: List<String>, env: (String) -> String? = System::getenv): List<Pair<String, String>> =
        headers.map { resolveHeader(it, env) }

    private fun resolveHeader(header: String, env: (String) -> String?): Pair<String, String> {
        val colonIndex = header.indexOf(':')
        if (colonIndex == -1) {
            throw ParameterException("Invalid --auth '$header': expected 'Name: value'")
        }
        val name = header.substring(0, colonIndex).trim()
        val rawValue = header.substring(colonIndex + 1)
        return name to resolveHeaderValue(rawValue, env)
    }

    internal fun resolveHeaderValue(value: String, env: (String) -> String?): String {
        val trimmed = value.trim()

        // 1. Shell command: a `!` introduces a command whose trimmed stdout replaces the
        //    `!command` portion. The prefix before `!` is preserved literally. The `!` is
        //    only treated as a command introducer when it appears at the start of the value
        //    or immediately after whitespace, so tokens containing embedded `!` are kept
        //    unchanged.
        val commandMatch = Regex("(^|\\s)!([^\\s].*)").find(trimmed)
        if (commandMatch != null) {
            val prefix = trimmed.substring(0, commandMatch.range.first) + commandMatch.groupValues[1]
            val command = commandMatch.groupValues[2]
            val result = runCommand(command)
            if (result.isBlank()) {
                throw ParameterException("Auth command '$command' produced no output")
            }
            return prefix + result
        }

        // 2. Whole value is an env var name and that var is set
        if (ENV_VAR_NAME_REGEX.matches(trimmed)) {
            env(trimmed)?.takeIf { it.isNotEmpty() }?.let { return it }
        }

        // 3. Substitute ${VAR} / ${VAR:-default} placeholders
        val substituted = ENV_VAR_PLACEHOLDER_REGEX.replace(trimmed) { match ->
            val varName = match.groupValues[1]
            val default = match.groupValues[2]
            env(varName)?.takeIf { it.isNotEmpty() } ?: default
        }
        return substituted
    }

    private fun runCommand(command: String): String {
        val process = try {
            ProcessBuilder("sh", "-c", command).start()
        } catch (e: IOException) {
            throw ParameterException("Failed to run auth command '$command'", e)
        }
        if (!process.waitFor(SHELL_TIMEOUT.seconds, TimeUnit.SECONDS)) {
            process.destroyForcibly()
            throw ParameterException("Auth command '$command' timed out after ${SHELL_TIMEOUT.seconds}s")
        }
        if (process.exitValue() != 0) {
            val stderr = process.errorStream.bufferedReader().use { it.readText() }.trim()
            throw ParameterException(
                "Auth command '$command' failed with exit code ${process.exitValue()}" +
                    if (stderr.isNotEmpty()) ": $stderr" else ""
            )
        }
        return process.inputStream.bufferedReader().use { it.readText() }.trim()
    }

    private fun loadLocal(value: String, paramName: String): LoadedApi {
        val path = try {
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

    private fun loadRemote(uri: URI, resolvedAuth: List<Pair<String, String>>): LoadedApi {
        val requestBuilder = HttpRequest.newBuilder(uri).timeout(REQUEST_TIMEOUT).GET()
        resolvedAuth.forEach { (name, value) -> requestBuilder.header(name, value) }
        val request = requestBuilder.build()
        val response = try {
            httpClient.send(request, BodyHandlers.ofString())
        } catch (e: HttpTimeoutException) {
            throw ParameterException("Timed out fetching api file from '$uri'", e)
        } catch (e: IOException) {
            throw ParameterException("Failed to fetch api file from '$uri'", e)
        }
        if (response.statusCode() !in 200..299) {
            throw ParameterException("Failed to fetch api file from '$uri': received HTTP status ${response.statusCode()}")
        }
        return LoadedApi(response.body(), uri.resolve("."))
    }
}
