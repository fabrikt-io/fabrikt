package com.cjbooms.fabrikt.multipart

import com.example.multipart.client.JsonClient
import com.example.multipart.client.RequestBodyWithFilename
import com.example.multipart.models.JsonNested
import com.example.multipart.models.JsonPayload
import com.fasterxml.jackson.annotation.JsonInclude
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule

class JsonMultipartTest {
    private val payload = JsonPayload(message = "Café 日本語", nested = JsonNested(listOf("first", "é", "last")))
    private val bytes = byteArrayOf(0, 1, 13, 10, 127, -1)
    private val upload = RequestBodyWithFilename(filename = "template.bin", requestBody = bytes.toRequestBody("application/octet-stream".toMediaType()))

    @ParameterizedTest
    @EnumSource(value = JsonInclude.Include::class, names = ["ALWAYS", "NON_NULL"])
    fun `explicit and default JSON parts preserve Jackson 3 mapper settings and binary uploads`(inclusion: JsonInclude.Include) {
        val mapper = JsonMapper.builder()
            .addModule(kotlinModule())
            .enable(SerializationFeature.INDENT_OUTPUT)
            .changeDefaultPropertyInclusion { it.withValueInclusion(inclusion) }
            .build()
        val body = capture(mapper) { it.uploadJson(request = payload, template = upload, details = payload, optionalRequest = payload) }
        val wire = Buffer().also(body::writeTo).readByteArray()
        val text = wire.toString(Charsets.UTF_8)

        for (name in listOf("request", "details", "optionalRequest")) {
            val part = body.parts.single { it.headers?.get("Content-Disposition") == "form-data; name=\"$name\"" }
            assertThat(part.body.contentType().toString()).isEqualTo("application/json; charset=utf-8")
            val json = Buffer().also(part.body::writeTo).readUtf8()
            assertThat(json).isEqualTo(mapper.writeValueAsString(payload)).contains("\n")
            val tree = mapper.readTree(json)
            assertThat(tree["message"].asString()).isEqualTo("Café 日本語")
            assertThat(tree["nested"]["values"][1].asString()).isEqualTo("é")
            assertThat(tree.has("note")).isEqualTo(inclusion == JsonInclude.Include.ALWAYS)
            if (inclusion == JsonInclude.Include.ALWAYS) assertThat(tree["note"].isNull).isTrue()
            assertThat(text).contains("Content-Disposition: form-data; name=\"$name\"\r\nContent-Type: application/json; charset=utf-8\r\n")
        }
        assertBinary(body, wire)
    }

    @Test
    fun `absent optional JSON parts are omitted`() {
        val mapper = JsonMapper.builder().addModule(kotlinModule()).build()
        val body = capture(mapper) { it.uploadJson(request = payload, template = upload, details = null, optionalRequest = null) }
        val wire = Buffer().also(body::writeTo).readByteArray()

        assertThat(body.parts).hasSize(2)
        assertThat(wire.toString(Charsets.UTF_8)).doesNotContain("name=\"details\"", "name=\"optionalRequest\"")
        assertBinary(body, wire)
    }

    private fun assertBinary(body: MultipartBody, wire: ByteArray) {
        val binary = body.parts.single { it.headers?.get("Content-Disposition") == "form-data; name=\"template\"; filename=\"template.bin\"" }
        assertThat(binary.body).isSameAs(upload.requestBody)
        assertThat(Buffer().also(binary.body::writeTo).readByteArray()).isEqualTo(bytes)
        assertThat(wire).containsSubsequence(*bytes)
        assertThat(wire.toString(Charsets.UTF_8)).contains("Content-Type: application/octet-stream\r\n")
    }

    private fun capture(mapper: JsonMapper, call: (JsonClient) -> Unit): MultipartBody {
        var captured: MultipartBody? = null
        val httpClient = OkHttpClient.Builder().addInterceptor { chain ->
            captured = chain.request().body as MultipartBody
            Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(204)
                .message("Synthetic success")
                .body("".toResponseBody())
                .build()
        }.build()
        call(JsonClient(mapper, "http://localhost", httpClient))
        return checkNotNull(captured)
    }
}
