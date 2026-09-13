package com.cjbooms.fabrikt.model

import com.cjbooms.fabrikt.parser.OpenApiDocumentParser
import com.cjbooms.fabrikt.util.YamlUtils
import com.reprezen.jsonoverlay.Overlay
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OpenApiModelTest {
    @Test
    fun `OpenApiSchema exposes document-aware native semantics through references`() {
        val document = OpenApiDocumentParser.parse(booleanSchemaSpec).asOpenApi3Document()
        val forbidden = document.schemas.getValue("Forbidden")
        val forbiddenReference =
            document.schemas
                .getValue("Subject")
                .properties
                .getValue("forbidden")

        assertThat(forbidden.isUninhabitable).isTrue()
        assertThat(forbiddenReference.isUninhabitable).isTrue()
    }

    @Test
    fun `OpenApiSchema positional accessors match underlying Kaizen Overlay values`() {
        val spec =
            """
            |openapi: 3.0.0
            |info:
            |  title: test
            |  version: 1.0.0
            |paths: {}
            |components:
            |  schemas:
            |    Pet:
            |      type: object
            |      properties:
            |        name:
            |          type: string
            """.trimMargin()
        val document = OpenApi3Document(YamlUtils.parseOpenApi(spec))
        val schema = document.schemas["Pet"]!!
        val overlay = Overlay.of(schema.kaizen)

        assertThat(schema.jsonPathFromRoot).isEqualTo(overlay.pathFromRoot)
        assertThat(schema.jsonPathInParent).isEqualTo(overlay.pathInParent)
        assertThat(schema.jsonReference).isEqualTo(overlay.jsonReference)
        assertThat(schema.isPresent).isEqualTo(overlay.isPresent)
        assertThat(schema.documentUrl).isEqualTo(overlay.positionInfo?.orElse(null)?.documentUrl)
        assertThat(schema.parsedJson).isSameAs(overlay.parsedJson)
    }

    private val booleanSchemaSpec =
        """
        openapi: 3.1.0
        info:
          title: Boolean schemas
          version: 1.0.0
        paths: {}
        components:
          schemas:
            Forbidden: false
            Subject:
              type: object
              properties:
                forbidden:
                  ${'$'}ref: '#/components/schemas/Forbidden'
        """.trimIndent()
}
