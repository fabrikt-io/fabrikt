package com.cjbooms.fabrikt.util

import com.beust.jcommander.ParameterException
import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.HttpServer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.net.InetSocketAddress
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

class ApiFileLoaderTest {
    private lateinit var server: HttpServer
    private val recordedHeaders = mutableListOf<Headers>()

    @BeforeEach
    fun startServer() {
        server = HttpServer.create(InetSocketAddress("localhost", 0), 0)
        server.start()
        recordedHeaders.clear()
    }

    @AfterEach
    fun stopServer() {
        server.stop(0)
    }

    private fun baseUrl() = "http://localhost:${server.address.port}"

    private fun HttpServer.stub(
        path: String,
        status: Int,
        body: String? = null,
    ) {
        createContext(path) { exchange ->
            recordedHeaders.add(exchange.requestHeaders)
            val bytes = body?.toByteArray(StandardCharsets.UTF_8)
            exchange.sendResponseHeaders(status, bytes?.size?.toLong() ?: -1)
            bytes?.let { exchange.responseBody.write(it) }
            exchange.responseBody.close()
        }
    }

    @Test
    fun `isRemote is true only for http and https urls`() {
        assertThat(ApiFileLoader.isRemote("http://example.com/api.yaml")).isTrue()
        assertThat(ApiFileLoader.isRemote("https://example.com/api.yaml")).isTrue()
        assertThat(ApiFileLoader.isRemote("HTTPS://example.com/api.yaml")).isTrue()
        assertThat(ApiFileLoader.isRemote("./api.yaml")).isFalse()
        assertThat(ApiFileLoader.isRemote("api.yaml")).isFalse()
        assertThat(ApiFileLoader.isRemote("/tmp/api.yaml")).isFalse()
        assertThat(ApiFileLoader.isRemote("ftp://example.com/api.yaml")).isFalse()
    }

    @Test
    fun `loads spec content and base uri from a remote http url`() {
        server.stub("/specs/api.yaml", 200, "openapi: 3.0.0")

        val loaded = ApiFileLoader.load("${baseUrl()}/specs/api.yaml", "--api-file")

        assertThat(loaded.content).isEqualTo("openapi: 3.0.0")
        assertThat(loaded.baseUri.toString()).isEqualTo("${baseUrl()}/specs/")
    }

    @Test
    fun `throws a clear parameter exception on a non-2xx response`() {
        server.stub("/missing.yaml", 404)

        assertThatThrownBy { ApiFileLoader.load("${baseUrl()}/missing.yaml", "--api-file") }
            .isInstanceOf(ParameterException::class.java)
            .hasMessageContaining("404")
    }

    @Test
    fun `loads spec content and base uri from a local file`(
        @TempDir tempDir: Path,
    ) {
        val file = tempDir.resolve("api.yaml")
        Files.writeString(file, "openapi: 3.0.0")

        val loaded = ApiFileLoader.load(file.toString(), "--api-file")

        assertThat(loaded.content).isEqualTo("openapi: 3.0.0")
        assertThat(loaded.baseUri).isEqualTo(tempDir.toUri())
    }

    @Test
    fun `throws a clear parameter exception when the local file does not exist`(
        @TempDir tempDir: Path,
    ) {
        val missing = tempDir.resolve("missing.yaml")

        assertThatThrownBy { ApiFileLoader.load(missing.toString(), "--api-file") }
            .isInstanceOf(ParameterException::class.java)
            .hasMessageContaining("Could not find api file")
            .hasMessageContaining("--api-file")
    }

    @Test
    fun `throws a clear parameter exception when the local path is syntactically invalid`() {
        val invalidPath = "invalid" + Character.MIN_VALUE + "path.yaml"

        assertThatThrownBy { ApiFileLoader.load(invalidPath, "--api-file") }
            .isInstanceOf(ParameterException::class.java)
            .hasMessageContaining("not a valid path")
            .hasMessageContaining("--api-file")
    }

    @Test
    fun `sends multiple auth headers on remote fetch`() {
        server.stub("/secure/api.yaml", 200, "openapi: 3.0.0")

        ApiFileLoader.load(
            "${baseUrl()}/secure/api.yaml",
            "--api-file",
            listOf("Authorization" to "Bearer smoke", "X-Custom" to "value"),
        )

        assertThat(recordedHeaders).hasSize(1)
        assertThat(recordedHeaders.single()["Authorization"]).containsExactly("Bearer smoke")
        assertThat(recordedHeaders.single()["X-Custom"]).containsExactly("value")
    }

    @Test
    fun `sends auth header containing embedded exclamation mark as a literal value`() {
        server.stub("/secure/api.yaml", 200, "openapi: 3.0.0")

        ApiFileLoader.load(
            "${baseUrl()}/secure/api.yaml",
            "--api-file",
            listOf("Authorization" to "Bearer abc!def"),
        )

        assertThat(recordedHeaders).hasSize(1)
        assertThat(recordedHeaders.single()["Authorization"]).containsExactly("Bearer abc!def")
    }

    @Test
    fun `local file load ignores auth headers without error`() {
        val file = Files.createTempFile("api", ".yaml")
        Files.writeString(file, "openapi: 3.0.0")

        val loaded = ApiFileLoader.load(file.toString(), "--api-file", listOf("Authorization" to "Bearer smoke"))

        assertThat(loaded.content).isEqualTo("openapi: 3.0.0")
    }

    @Test
    fun `AuthJsonLoader sends auth headers when fetching remote refs`() {
        server.stub("/common.yaml", 200, "openapi: 3.0.0\ncomponents:\n  schemas:\n    X:\n      type: object")

        AuthJsonLoader(listOf("Authorization" to "Bearer ref-token")).load(URL("${baseUrl()}/common.yaml"))

        assertThat(recordedHeaders).hasSize(1)
        assertThat(recordedHeaders.single()["Authorization"]).containsExactly("Bearer ref-token")
    }
}
