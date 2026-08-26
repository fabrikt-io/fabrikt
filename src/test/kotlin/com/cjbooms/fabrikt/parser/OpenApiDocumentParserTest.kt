package com.cjbooms.fabrikt.parser

import com.cjbooms.fabrikt.util.YamlUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

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
        val source = YamlUtils.objectMapper.readTree(parsedDocument.sourceContent)

        assertThat(source.at("/components/schemas/Name/type").map { it.textValue() })
            .containsExactly("string", "null")
        assertThat(parsedDocument.kaizenModel.schemas["Name"]!!.type).isEqualTo("string")
        assertThat(parsedDocument.kaizenModel.schemas["Name"]!!.nullable).isTrue()
    }
}
