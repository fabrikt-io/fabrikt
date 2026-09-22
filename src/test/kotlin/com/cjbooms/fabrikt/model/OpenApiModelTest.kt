package com.cjbooms.fabrikt.model

import com.cjbooms.fabrikt.parser.OpenApiDocumentParser
import com.cjbooms.fabrikt.util.YamlUtils
import com.reprezen.jsonoverlay.Overlay
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class OpenApiModelTest {
    @ParameterizedTest
    @ValueSource(strings = ["3.1.2", "3.2.0"])
    fun `facade uses source declarations for schema type and nullability`(version: String) {
        val spec =
            """
            openapi: $version
            info:
              title: Type declarations
              version: 1.0.0
            paths: {}
            components:
              schemas:
                Subject:
                  type: object
                  properties:
                    nullableText:
                      type: [string, 'null']
                    mixedValue:
                      type: [string, integer]
                    mixedObject:
                      type: [object, string]
                      properties:
                        code:
                          type: string
            """.trimIndent()
        val subject =
            OpenApiDocumentParser
                .parse(spec)
                .asOpenApi3Document()
                .schemas
                .getValue("Subject")

        assertThat(subject.properties.getValue("nullableText").type).isEqualTo("string")
        assertThat(subject.properties.getValue("nullableText").isNullable).isTrue()
        assertThat(subject.properties.getValue("mixedValue").type).isNull()
        assertThat(subject.properties.getValue("mixedValue").hasMultipleNonNullTypes).isTrue()
        assertThat(subject.properties.getValue("mixedObject").type).isNull()
    }

    @ParameterizedTest
    @ValueSource(strings = ["3.0.4", "3.1.2", "3.2.0"])
    fun `facades expose deprecated schemas properties and operations across OpenAPI versions`(version: String) {
        val document = OpenApiDocumentParser.parse(deprecatedSpec.replace("VERSION", version)).asOpenApi3Document()
        val schema = document.schemas.getValue("LegacySubject")

        assertThat(schema.isDeprecated).isTrue()
        assertThat(schema.properties.getValue("legacyId").isDeprecated).isTrue()
        val operation =
            document.paths
                .getValue("/subjects")
                .operations
                .getValue("get")
        assertThat(operation.isDeprecated).isTrue()
        assertThat(operation.parameters.single().isDeprecated).isTrue()
    }

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

    private val deprecatedSpec =
        """
        openapi: VERSION
        info:
          title: Deprecated elements
          version: 1.0.0
        paths:
          /subjects:
            get:
              operationId: findSubjects
              deprecated: true
              parameters:
                - name: legacyFilter
                  in: query
                  deprecated: true
                  schema:
                    type: string
              responses:
                '204':
                  description: No content
        components:
          schemas:
            LegacySubject:
              type: object
              deprecated: true
              properties:
                legacyId:
                  type: string
                  deprecated: true
        """.trimIndent()
}
