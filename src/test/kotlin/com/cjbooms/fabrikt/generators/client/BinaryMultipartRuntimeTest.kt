package com.cjbooms.fabrikt.generators.client

import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.SerializationLibrary
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.MutableSettings
import com.cjbooms.fabrikt.model.Clients
import com.cjbooms.fabrikt.model.SimpleFile
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.lang.reflect.InvocationTargetException
import java.net.URLClassLoader
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

class BinaryMultipartRuntimeTest {
    @TempDir
    lateinit var directory: Path

    @Test
    fun `compiled client sends exact binary parts and rejects invalid names before network execution`() {
        MutableSettings.updateSettings(genTypes = setOf(CodeGenerationType.CLIENT), serializationLibrary = SerializationLibrary.JACKSON_2)
        ModelNameRegistry.clear()
        val generator =
            OkHttpSimpleClientGenerator(Packages("runtime.multipart"), SourceApi(readTextResource("/examples/multipartPatterns/api.yaml")))
        val sources = mutableListOf<Path>()
        Clients(generator.generateDynamicClientCode()).files.forEach {
            it.writeTo(directory)
            sources.add(directory.resolve("runtime/multipart/client/${it.name}.kt"))
        }
        generator.generateLibrary(emptySet()).filterIsInstance<SimpleFile>().forEach {
            val file = directory.resolve("runtime/multipart/client/${it.path.fileName}")
            file.parent.createDirectories()
            file.writeText(it.content)
            sources.add(file)
        }
        val caller = directory.resolve("runtime/multipart/client/Caller.kt")
        caller.writeText(
            """
            package runtime.multipart.client
            fun invokeWithoutDynamicParts(client: DynamicClient, template: RequestBodyWithFilename) =
                client.uploadDynamic(template, null, emptyMap(), emptyMap())
            """.trimIndent(),
        )
        sources.add(caller)
        val output = directory.resolve("classes")
        val diagnostics = ByteArrayOutputStream()
        val classpath =
            listOf(
                Unit::class.java,
                ObjectMapper::class.java,
                com.fasterxml.jackson.core.JsonFactory::class.java,
                com.fasterxml.jackson.annotation.JsonProperty::class.java,
                com.fasterxml.jackson.module.kotlin.KotlinModule::class.java,
                OkHttpClient::class.java,
                Buffer::class.java,
            ).map {
                Path
                    .of(
                        it.protectionDomain.codeSource.location
                            .toURI(),
                    ).toString()
            }.distinct()
                .joinToString(java.io.File.pathSeparator)
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
                *sources.map(Path::toString).toTypedArray(),
            )
        assertThat(result).withFailMessage(diagnostics.toString()).isEqualTo(ExitCode.OK)
        URLClassLoader(arrayOf(output.toUri().toURL()), javaClass.classLoader).use { loader ->
            val requests = mutableListOf<Request>()
            val httpClient =
                OkHttpClient
                    .Builder()
                    .addInterceptor { chain ->
                        requests.add(chain.request())
                        Response
                            .Builder()
                            .request(chain.request())
                            .protocol(Protocol.HTTP_1_1)
                            .code(204)
                            .message("Synthetic success")
                            .body(ByteArray(0).toResponseBody())
                            .build()
                    }.build()
            val clientType = loader.loadClass("runtime.multipart.client.DynamicClient")
            val uploadType = loader.loadClass("runtime.multipart.client.RequestBodyWithFilename")
            val client =
                clientType
                    .getConstructor(ObjectMapper::class.java, String::class.java, OkHttpClient::class.java)
                    .newInstance(ObjectMapper(), "http://localhost.invalid", httpClient)

            fun upload(
                bytes: ByteArray,
                filename: String,
                contentType: String,
            ): Any =
                uploadType
                    .getConstructor(RequestBody::class.java, String::class.java)
                    .newInstance(bytes.toRequestBody(contentType.toMediaType()), filename)
            val templateBytes = byteArrayOf(0, 1, 13, 10, -1)
            val fontBytes = byteArrayOf(-128, 0, 34, 13, 10, -1)
            val imageBytes = byteArrayOf(0, 127, -2, -3)
            val template = upload(templateBytes, "template.bin", "application/octet-stream")
            val font = upload(fontBytes, "font.woff2", "font/woff2")
            val image = upload(imageBytes, "image.png", "image/png")
            val method = clientType.getMethod("uploadDynamic", uploadType, uploadType, Map::class.java, Map::class.java, Map::class.java)

            fun invoke(parts: Map<String, Any>) =
                method.invoke(client, template, null, emptyMap<String, String>(), emptyMap<String, String>(), parts)
            loader
                .loadClass("runtime.multipart.client.CallerKt")
                .getMethod("invokeWithoutDynamicParts", clientType, uploadType)
                .invoke(null, client, template)
            invoke(linkedMapOf("resource.font1" to font, "resource.font2" to image))
            invoke(mapOf("font" to font))
            invoke(mapOf("myfontname" to font))
            invoke(mapOf("resource.font1" to image, "resource.font1" to font))
            assertThat(requests).hasSize(5)

            fun checkPart(
                part: MultipartBody.Part,
                name: String,
                filename: String,
                contentType: String,
                bytes: ByteArray,
            ) {
                assertThat(part.headers!!["Content-Disposition"]).isEqualTo("form-data; name=\"$name\"; filename=\"$filename\"")
                assertThat(part.body.contentType().toString()).isEqualTo(contentType)
                val buffer = Buffer()
                part.body.writeTo(buffer)
                assertThat(buffer.readByteArray()).containsExactly(*bytes)
            }
            val emptyBody = requests[0].body as MultipartBody
            assertThat(emptyBody.parts).hasSize(1)
            checkPart(emptyBody.part(0), "template", "template.bin", "application/octet-stream", templateBytes)
            val body = requests[1].body as MultipartBody
            assertThat(body.parts).hasSize(3)
            checkPart(body.part(0), "template", "template.bin", "application/octet-stream", templateBytes)
            checkPart(body.part(1), "resource.font1", "font.woff2", "font/woff2", fontBytes)
            checkPart(body.part(2), "resource.font2", "image.png", "image/png", imageBytes)
            checkPart((requests[4].body as MultipartBody).part(1), "resource.font1", "font.woff2", "font/woff2", fontBytes)
            val wire = Buffer()
            body.writeTo(wire)
            val bytes = wire.readByteArray()
            val text = bytes.toString(Charsets.ISO_8859_1)
            assertThat(body.contentType().parameter("boundary")).isEqualTo(body.boundary)
            assertThat(text).startsWith("--${body.boundary}\r\n").endsWith("\r\n--${body.boundary}--\r\n")
            assertThat(text.split("--${body.boundary}")).hasSize(5)
            assertThat(text).contains("Content-Type: font/woff2\r\n", "Content-Type: image/png\r\n")
            assertThat(body.contentLength()).isEqualTo(bytes.size.toLong())
            listOf(
                "",
                "other",
                "template",
                "resource.fixed",
                "resource.",
                "resource." + "a".repeat(129),
                "resource.font\n",
                "resource.\"font",
                "resource.fönt",
            ).forEach { name ->
                assertThatThrownBy { invoke(mapOf(name to font)) }
                    .isInstanceOf(InvocationTargetException::class.java)
                    .hasCauseInstanceOf(IllegalArgumentException::class.java)
            }
            assertThat(requests).hasSize(5)
        }
    }
}
