package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.SerializationLibrary
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.client.OkHttpSimpleClientGenerator
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.GeneratedCodeAsserter.Companion.assertThatGenerated
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import com.cjbooms.fabrikt.util.TestFileUtils.toSingleFile
import com.cjbooms.fabrikt.util.YamlObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.logging.Handler
import java.util.logging.LogRecord
import java.util.logging.Logger

class BinaryMultipartGeneratorTest {
    private val spec = readTextResource("/examples/multipartPatterns/api.yaml")

    @BeforeEach
    fun init() {
        MutableSettings.updateSettings(genTypes = setOf(CodeGenerationType.CLIENT), serializationLibrary = SerializationLibrary.JACKSON_3)
        ModelNameRegistry.clear()
    }

    @ParameterizedTest
    @ValueSource(strings = ["multipartPatterns"])
    fun `binary pattern multipart client matches golden`(testCaseName: String) {
        assertThatGenerated(generate(readTextResource("/examples/$testCaseName/api.yaml")))
            .isEqualTo("/examples/$testCaseName/client/ApiClient.kt")
    }

    @Test
    fun `fixed only multipart methods have no dynamic argument`() {
        assertThat(
            generate(
                spec.substringBefore("              patternProperties:") +
                    spec.substringAfter("      responses:").let { "      responses:$it" },
            ),
        ).doesNotContain("dynamicParts")
    }

    @Test
    fun `multipart media type parameters use the existing schema selection`() {
        assertThat(generate(spec.replace("multipart/form-data:", "multipart/form-data; charset=utf-8:")))
            .contains("dynamicParts: Map<String, RequestBodyWithFilename>")
    }

    @Test
    fun `dynamic argument avoids a fixed parameter name collision`() {
        assertThat(generate(spec.replace("template", "dynamicParts")))
            .contains("dynamicParts_: Map<String, RequestBodyWithFilename> = emptyMap()")
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "propertyNames: {pattern: '^resource'}",
            "minProperties: 1",
            "maxProperties: 4",
            "dependentRequired: {template: [other]}",
            "allOf: [{type: object}]",
        ],
    )
    fun `unsupported object constraints keep fixed parts and warn`(constraint: String) {
        assertUnsupported(spec.replace("              type: object", "              type: object\n              $constraint"))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "minLength: 2",
            "contentEncoding: base64",
            "enum: [foo]",
            "pattern: '^foo'",
            "type: [string, 'null']",
            "format: byte",
            "allOf: [{type: string}]",
        ],
    )
    fun `constrained or heterogeneous pattern values keep fixed parts and warn`(constraint: String) {
        val constrained =
            when {
                constraint.startsWith(
                    "type:",
                ) -> spec.replace("type: string\n                  format: binary", "$constraint\n                  format: binary")
                constraint.startsWith("format:") -> spec.replace("                  format: binary", "                  $constraint")
                else -> spec.replace("                  format: binary", "                  format: binary\n                  $constraint")
            }
        assertUnsupported(constrained)
    }

    @ParameterizedTest
    @ValueSource(strings = ["[", "(?i)font", "\\\\d+", "resource.*", "[A-Z&&B]", "(font|image)", "[^x]"])
    fun `unsupported patterns keep fixed parts and warn`(pattern: String) {
        assertUnsupported(spec.replace("'^resource\\.[A-Za-z0-9][A-Za-z0-9._-]{0,127}$'", "'$pattern'"))
    }

    @Test
    fun `nonbinary fixed property matching binary pattern is diagnosed`() {
        assertUnsupported(spec.replace("resource.fixed: {type: string, format: binary}", "resource.fixed: {type: string}"))
    }

    @Test
    fun `binary pattern values can be referenced`() {
        val referenced =
            spec.replace(
                "                  type: string\n                  format: binary",
                "                  \$ref: '#/components/schemas/Upload'",
            ) +
                "\ncomponents:\n  schemas:\n    Upload: {type: string, format: binary}\n"
        assertThat(generate(referenced)).contains("dynamicParts: Map<String, RequestBodyWithFilename>")
    }

    @Test
    fun `referenced multipart schema retains patterns and rejects reference siblings`() {
        val mapper = YamlObjectMapper.instance
        val root = mapper.readTree(spec) as ObjectNode
        val media = root.at("/paths/~1dynamic/post/requestBody/content/multipart~1form-data") as ObjectNode
        val schema = media["schema"]
        val schemas = mapper.createObjectNode().set<ObjectNode>("Payload", schema)
        root.set<ObjectNode>("components", mapper.createObjectNode().set<ObjectNode>("schemas", schemas))
        val reference = mapper.createObjectNode().put("\$ref", "#/components/schemas/Payload")
        media.set<ObjectNode>("schema", reference)
        assertThat(generate(mapper.writeValueAsString(root))).contains("dynamicParts: Map<String, RequestBodyWithFilename>")
        reference.set<ObjectNode>("propertyNames", mapper.createObjectNode().put("pattern", "^resource"))
        assertUnsupported(mapper.writeValueAsString(root))
    }

    @Test
    fun `required dynamic names cannot silently become optional`() {
        assertUnsupported(spec.replace("required: [template]", "required: [template, resource.font1]"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["true", "false", "{type: integer}", "null"])
    fun `one unsupported pattern value disables the complete dynamic map`(value: String) {
        val mapper = YamlObjectMapper.instance
        val root = mapper.readTree(spec)
        val patterns = root.at("/paths/~1dynamic/post/requestBody/content/multipart~1form-data/schema/patternProperties") as ObjectNode
        patterns.set<ObjectNode>("font", mapper.readTree(value))
        assertUnsupported(mapper.writeValueAsString(root))
    }

    @Test
    fun `patterns nested in composition are diagnosed`() {
        val mapper = YamlObjectMapper.instance
        val root = mapper.readTree(spec)
        val schema = root.at("/paths/~1dynamic/post/requestBody/content/multipart~1form-data/schema") as ObjectNode
        val patterns = schema.remove("patternProperties")
        val nested = mapper.createObjectNode().put("type", "object").set<ObjectNode>("patternProperties", patterns)
        schema.set<ObjectNode>("allOf", mapper.createArrayNode().add(nested))
        assertUnsupported(mapper.writeValueAsString(root))
    }

    private fun assertUnsupported(input: String) {
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
            assertThat(generate(input)).contains("template: RequestBodyWithFilename").doesNotContain("dynamicParts:")
            assertThat(messages).anySatisfy {
                assertThat(
                    it,
                ).contains("Skipping dynamic multipart uploads", "Fixed parts are retained", "Declare uploads as fixed properties")
            }
        } finally {
            logger.removeHandler(handler)
        }
    }

    private fun generate(input: String): String =
        OkHttpSimpleClientGenerator(Packages("examples.multipartPatterns"), SourceApi(input)).generateDynamicClientCode().toSingleFile()
}
