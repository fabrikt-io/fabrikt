package com.cjbooms.fabrikt.parser

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GeneratorSchemaDocumentTest {
    @Test
    fun `uses Kaizen adapters as the default generator schemas`() {
        val document = OpenApiDocumentParser.parse(openApi).toGeneratorSchemaDocument()

        assertThat(document.version?.value).isEqualTo("3.1.2")
        assertThat(document.componentSchemas).containsOnlyKeys("Subject")
        assertThat(document.componentSchemas.values).allMatch { it is GeneratorObjectSchema && it !is SourceSchema }
    }

    @Test
    fun `exposes native boolean schemas behind the same document`() {
        val document = OpenApiDocumentParser.parse(openApi).toGeneratorSchemaDocument()
        val subject = document.componentSchemas.getValue("Subject") as GeneratorObjectSchema
        val forbidden = subject.properties.getValue("forbidden")

        assertThat(forbidden).isNotInstanceOf(SourceSchema::class.java)
        assertThat(document.isUninhabitableAt(forbidden.location)).isTrue()
    }

    @Test
    fun `classifies a false schema reached only through a cross-operation parameter ref as uninhabitable`() {
        val parsed = OpenApiDocumentParser.parse(crossOperationParameterRefApi)
        val location = "#/paths/~1widgets~1{widgetId}/patch/parameters/0/schema"

        val resolvedSchema = parsed.source.schemasByLocation.getValue(location) as SourceBooleanSchema
        assertThat(resolvedSchema.allowsAnyValue).isFalse()

        assertThat(parsed.toGeneratorSchemaDocument().isUninhabitableAt(location)).isTrue()
    }

    private val openApi =
        """
        openapi: 3.1.2
        info:
          title: Test
          version: "1.0"
        paths: {}
        components:
          schemas:
            Subject:
              type: object
              properties:
                forbidden: false
        """.trimIndent()

    private val crossOperationParameterRefApi =
        """
        openapi: 3.1.2
        info:
          title: Test
          version: "1.0"
        paths:
          /widgets/{widgetId}:
            get:
              parameters:
                - name: widgetId
                  in: path
                  required: true
                  schema: false
              responses:
                '200':
                  description: OK
            patch:
              parameters:
                - ${'$'}ref: '#/paths/~1widgets~1%7BwidgetId%7D/get/parameters/0'
              responses:
                '200':
                  description: OK
        """.trimIndent()
}
