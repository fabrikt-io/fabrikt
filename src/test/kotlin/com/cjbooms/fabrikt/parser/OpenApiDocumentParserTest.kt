package com.cjbooms.fabrikt.parser

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.net.URI

class OpenApiDocumentParserTest {
    @Test
    fun `preserves the source document before applying Kaizen compatibility transformations`() {
        val input =
            """
            openapi: 3.1.0
            info:
              title: Test
              version: "1.0"
            paths: {}
            components:
              schemas:
                Name:
                  type:
                    - string
                    - "null"
            """.trimIndent()

        val parsedDocument = OpenApiDocumentParser.parse(input)
        val source = parsedDocument.source.root

        assertThat(source.at("/components/schemas/Name/type").map { it.textValue() })
            .containsExactly("string", "null")
        assertThat(parsedDocument.kaizenModel.schemas["Name"]!!.type).isEqualTo("string")
        assertThat(parsedDocument.kaizenModel.schemas["Name"]!!.nullable).isTrue()
    }

    @ParameterizedTest
    @CsvSource(
        "3.0.4, 3, 0, 4",
        "3.1.2, 3, 1, 2",
        "3.2.0, 3, 2, 0",
    )
    fun `exposes the source OpenAPI version before compatibility transformations`(
        value: String,
        major: Int,
        minor: Int,
        patch: Int,
    ) {
        val input =
            """
            openapi: $value
            info:
              title: Test
              version: "1.0"
            paths: {}
            """.trimIndent()

        val version = OpenApiDocumentParser.parse(input).version

        assertThat(version)
            .isEqualTo(OpenApiVersion(value, major, minor, patch))
    }

    @Test
    fun `uses the supplied base URI for source reference resolution`() {
        val input =
            """
            openapi: 3.1.0
            info:
              title: Test
              version: "1.0"
            paths: {}
            components:
              schemas:
                Name:
                  type: string
                Alias:
                  ${'$'}ref: '#/components/schemas/Name'
            """.trimIndent()
        val baseUri = URI("https://example.test/specs%20with%20spaces/openapi.yaml")

        val source = OpenApiDocumentParser.parse(input, baseUri).source
        val resolution =
            source.schemaReferenceResolutions.getValue("#/components/schemas/Alias") as
                SourceSchemaReferenceResolution.Resolved

        assertThat(source.baseUri).isEqualTo(baseUri)
        assertThat(resolution.uri)
            .isEqualTo(URI("https://example.test/specs%20with%20spaces/openapi.yaml#/components/schemas/Name"))
        assertThat(resolution.target).isSameAs(source.componentSchemas.getValue("Name"))
    }
}
