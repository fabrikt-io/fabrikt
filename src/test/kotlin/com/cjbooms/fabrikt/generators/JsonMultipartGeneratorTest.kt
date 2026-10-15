package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.SerializationLibrary
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.client.OkHttpSimpleClientGenerator
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import com.cjbooms.fabrikt.util.TestFileUtils.toSingleFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.util.logging.Handler
import java.util.logging.LogRecord
import java.util.logging.Logger

class JsonMultipartGeneratorTest {
    private val spec = readTextResource("/examples/multipartJson/api.yaml")

    @BeforeEach
    fun init() {
        MutableSettings.updateSettings(genTypes = setOf(CodeGenerationType.CLIENT))
        ModelNameRegistry.clear()
    }

    @ParameterizedTest
    @EnumSource(value = SerializationLibrary::class, names = ["JACKSON", "JACKSON_2", "JACKSON_3"])
    fun `JSON request bodies use the selected Jackson mapper`(library: SerializationLibrary) {
        MutableSettings.updateSettings(serializationLibrary = library)
        val client = generate(spec)

        assertThat(client.replace(Regex("\\s+"), ""))
            .contains(
                "addFormDataPart(\"request\",null,objectMapper.writeValueAsString(request).toRequestBody(\"application/json\".toMediaType())",
            ).contains(
                "addFormDataPart(\"details\",null,objectMapper.writeValueAsString(details).toRequestBody(\"application/json\".toMediaType())",
            ).contains("addFormDataPart(\"template\",template.filename,template.requestBody)")
        if (library == SerializationLibrary.JACKSON_3) {
            assertThat(client).contains("import tools.jackson.databind.json.JsonMapper")
        } else {
            assertThat(client).contains("import com.fasterxml.jackson.databind.ObjectMapper")
        }
    }

    @Test
    fun `encoding content types are exposed by the facade and retained when replacing a schema`() {
        val api = SourceApi(spec)
        val mediaType =
            api.openApi3.paths["/json"]!!
                .operations["post"]!!
                .requestBody.contentMediaTypes
                .getValue("multipart/form-data")

        assertThat(mediaType.encodingContentType("request")).isEqualTo("application/json")
        assertThat(mediaType.encodingContentType("details")).isNull()
        assertThat(mediaType.withSchema(mediaType.schema).encodingContentType("request")).isEqualTo("application/json")
    }

    @Test
    fun `unsupported XML encoding warns and keeps JSON bytes labelled as JSON`() {
        val messages = mutableListOf<String>()
        val handler =
            object : Handler() {
                override fun publish(record: LogRecord) {
                    messages.add(record.message)
                }

                override fun flush() = Unit

                override fun close() = Unit
            }
        val logger = Logger.getGlobal()
        logger.addHandler(handler)
        try {
            val client = generate(spec.replace("contentType: application/json", "contentType: application/xml"))

            assertThat(client).contains("\"application/json\".toMediaType()").doesNotContain("application/xml")
            assertThat(messages).anySatisfy {
                assertThat(
                    it,
                ).contains(
                    "part 'request'",
                    "application/xml",
                    "using application/json",
                    "Set encoding.request.contentType to application/json",
                )
            }
        } finally {
            logger.removeHandler(handler)
        }
    }

    private fun generate(spec: String): String =
        OkHttpSimpleClientGenerator(Packages("examples.multipartJson"), SourceApi(spec)).generateDynamicClientCode().toSingleFile()
}
