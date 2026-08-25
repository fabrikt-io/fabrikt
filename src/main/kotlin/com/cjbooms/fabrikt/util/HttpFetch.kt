package com.cjbooms.fabrikt.util

import com.beust.jcommander.ParameterException
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.net.http.HttpTimeoutException
import java.time.Duration

internal object HttpFetch {
    private val client: HttpClient by lazy {
        HttpClient
            .newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()
    }

    fun fetch(
        uri: URI,
        headers: List<Pair<String, String>>,
    ): String {
        val request =
            HttpRequest
                .newBuilder(uri)
                .timeout(Duration.ofSeconds(30))
                .GET()
                .apply { headers.forEach { (name, value) -> header(name, value) } }
                .build()
        val response =
            try {
                client.send(request, BodyHandlers.ofString())
            } catch (e: HttpTimeoutException) {
                throw ParameterException("Timed out fetching api file from '$uri'", e)
            } catch (e: IOException) {
                throw ParameterException("Failed to fetch api file from '$uri'", e)
            }
        if (response.statusCode() !in 200..299) {
            throw ParameterException("Failed to fetch api file from '$uri': received HTTP status ${response.statusCode()}")
        }
        return response.body()
    }
}
