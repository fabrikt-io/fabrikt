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

data class LoadedApi(val content: String, val baseUri: URI)

/**
 * Loads an Open API spec or fragment from either a local file path or an `http(s)` URL.
 */
object ApiFileLoader {

    private val CONNECT_TIMEOUT: Duration = Duration.ofSeconds(10)
    private val REQUEST_TIMEOUT: Duration = Duration.ofSeconds(30)

    private val httpClient: HttpClient by lazy {
        HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()
    }

    fun isRemote(value: String): Boolean =
        runCatching { URI(value).scheme }.getOrNull()?.lowercase() in setOf("http", "https")

    fun load(value: String, paramName: String): LoadedApi =
        if (isRemote(value)) loadRemote(URI(value)) else loadLocal(value, paramName)

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

    private fun loadRemote(uri: URI): LoadedApi {
        val request = HttpRequest.newBuilder(uri).timeout(REQUEST_TIMEOUT).GET().build()
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
