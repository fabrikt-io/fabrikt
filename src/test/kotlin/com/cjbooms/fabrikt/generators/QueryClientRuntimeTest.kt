package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.SerializationLibrary
import com.cjbooms.fabrikt.cli.ValidationLibrary
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.client.OkHttpClientGenerator
import com.cjbooms.fabrikt.generators.model.ModelGenerator
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sun.net.httpserver.HttpServer
import okhttp3.OkHttpClient
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.net.InetSocketAddress
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.io.path.extension

class QueryClientRuntimeTest {
    @TempDir
    lateinit var directory: Path

    @ParameterizedTest
    @ValueSource(strings = ["JSON", "NONE", "MULTIPART"])
    fun `compiled QUERY client sends its body and decodes its response while preserving GET`(bodyType: String) {
        MutableSettings.updateSettings(
            serializationLibrary = SerializationLibrary.JACKSON_2,
            validationLibrary = ValidationLibrary.NO_VALIDATION,
        )
        ModelNameRegistry.clear()
        val spec = readTextResource("/examples/queryOperation/api.yaml")
        val body = "      requestBody:" + spec.substringAfter("      requestBody:").substringBefore("      responses:")
        val api =
            SourceApi(
                when (bodyType) {
                    "NONE" -> spec.replace(body, "")
                    "MULTIPART" ->
                        spec.replace(
                            body,
                            """
                            requestBody:
                              required: true
                              content:
                                multipart/form-data:
                                  schema:
                                    type: object
                                    required: [name]
                                    properties:
                                      name:
                                        type: string
                            """.trimIndent().prependIndent("      ") + "\n",
                        )
                    else -> spec
                },
            )
        val packages = Packages("audit")
        val source = directory.resolve("source")
        val generator = OkHttpClientGenerator(packages, api, Path.of(""))
        ModelGenerator(packages, api).generate().files.forEach { it.writeTo(source) }
        generator.generate(emptySet()).files.forEach { it.writeTo(source) }
        generator.generateLibrary(emptySet()).forEach { it.writeFileTo(source.toFile()) }
        val output = directory.resolve("classes")
        val diagnostics = ByteArrayOutputStream()
        val classpath =
            listOf(
                Unit::class.java,
                com.fasterxml.jackson.annotation.JsonProperty::class.java,
                com.fasterxml.jackson.databind.ObjectMapper::class.java,
                com.fasterxml.jackson.core.JsonFactory::class.java,
                com.fasterxml.jackson.module.kotlin.KotlinModule::class.java,
                OkHttpClient::class.java,
                okio.Buffer::class.java,
            ).map {
                Path
                    .of(
                        it.protectionDomain.codeSource.location
                            .toURI(),
                    ).toString()
            }.distinct()
                .joinToString(File.pathSeparator)
        val result =
            K2JVMCompiler().exec(
                PrintStream(diagnostics),
                "-no-stdlib",
                "-no-reflect",
                "-jvm-target",
                "17",
                "-classpath",
                classpath,
                "-d",
                output.toString(),
                *Files.walk(source).use { files ->
                    files
                        .filter { it.extension == "kt" }
                        .map { it.toString() }
                        .toList()
                        .toTypedArray()
                },
            )
        assertThat(result).withFailMessage(diagnostics.toString()).isEqualTo(ExitCode.OK)
        val requests = CopyOnWriteArrayList<Triple<String, String, String?>>()
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/search") { exchange ->
            val body = exchange.requestBody.use { it.readAllBytes().toString(Charsets.UTF_8) }
            requests.add(Triple(exchange.requestMethod, body, exchange.requestHeaders.getFirst("Content-Type")))
            val response = """{"name":"Alice"}""".toByteArray()
            exchange.responseHeaders.set("Content-Type", "application/json")
            exchange.sendResponseHeaders(200, response.size.toLong())
            exchange.responseBody.use { it.write(response) }
            exchange.close()
        }
        server.start()
        val http = OkHttpClient()
        try {
            URLClassLoader(arrayOf(output.toUri().toURL()), javaClass.classLoader).use { loader ->
                val clientType = loader.loadClass("audit.client.SearchClient")
                val requestType = loader.loadClass("audit.models.SearchRequest")
                val client =
                    clientType
                        .getConstructor(
                            com.fasterxml.jackson.databind.ObjectMapper::class.java,
                            String::class.java,
                            OkHttpClient::class.java,
                        ).newInstance(jacksonObjectMapper(), "http://127.0.0.1:${server.address.port}", http)
                val maps = arrayOf(emptyMap<String, String>(), emptyMap<String, String>())
                val response =
                    when (bodyType) {
                        "NONE" -> clientType.getMethod("search", Map::class.java, Map::class.java).invoke(client, *maps)
                        "MULTIPART" ->
                            clientType
                                .getMethod("search", String::class.java, Map::class.java, Map::class.java)
                                .invoke(client, "Alice", *maps)
                        else ->
                            clientType
                                .getMethod("search", requestType, Map::class.java, Map::class.java)
                                .invoke(client, requestType.getConstructor(String::class.java).newInstance("Alice"), *maps)
                    }
                val data = response.javaClass.getMethod("getData").invoke(response)
                assertThat(data.javaClass.getMethod("getName").invoke(data)).isEqualTo("Alice")
                clientType
                    .getMethod(
                        "getSearch",
                        Map::class.java,
                        Map::class.java,
                    ).invoke(client, emptyMap<String, String>(), emptyMap<String, String>())
                assertThat(requests.map { it.first }).containsExactly("QUERY", "GET")
                assertThat(requests.last().second).isEmpty()
                val query = requests.first()
                when (bodyType) {
                    "NONE" -> assertThat(query.second).isEmpty()
                    "MULTIPART" -> {
                        assertThat(query.second).contains("name=\"name\"", "Alice")
                        assertThat(query.third).startsWith("multipart/form-data;")
                    }
                    else -> {
                        assertThat(query.second).isEqualTo("""{"name":"Alice"}""")
                        assertThat(query.third).startsWith("application/json")
                    }
                }
            }
        } finally {
            server.stop(0)
            http.connectionPool.evictAll()
            http.dispatcher.executorService.shutdown()
        }
    }
}
