package com.cjbooms.fabrikt.parser

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.URI

class SourceSchemaReferenceResolverTest {
    @ParameterizedTest
    @ValueSource(strings = ["3.0.4", "3.1.2", "3.2.0"])
    fun `resolves local JSON Pointer schema references without following them`(version: String) {
        val document =
            parse(
                version,
                """
                components:
                  schemas:
                    First:
                      ${'$'}ref: '#/components/schemas/Second'
                    Second:
                      ${'$'}ref: '#/components/schemas/First'
                    Target/with~escape and café:
                      type: string
                    Escaped:
                      ${'$'}ref: '#/components/schemas/Target~1with~0escape%20and%20caf%C3%A9'
                """,
            )

        val firstReference = document.resolvedReference("#/components/schemas/First")
        val secondReference = document.resolvedReference("#/components/schemas/Second")
        val escapedReference = document.resolvedReference("#/components/schemas/Escaped")

        assertThat(firstReference.target).isSameAs(document.componentSchemas.getValue("Second"))
        assertThat(secondReference.target).isSameAs(document.componentSchemas.getValue("First"))
        assertThat(escapedReference.value).isEqualTo("#/components/schemas/Target~1with~0escape%20and%20caf%C3%A9")
        assertThat(escapedReference.uri)
            .isEqualTo(URI("https://example.test/specs/openapi.yaml#/components/schemas/Target~1with~0escape%20and%20caf%C3%A9"))
        assertThat(escapedReference.target)
            .isSameAs(document.componentSchemas.getValue("Target/with~escape and café"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["3.1.2", "3.2.0"])
    fun `resolves schema resources anchors and references to boolean schemas`(version: String) {
        val document =
            parse(
                version,
                """
                components:
                  schemas:
                    Root:
                      ${'$'}id: schemas/root.json
                      ${'$'}defs:
                        Anchored:
                          ${'$'}anchor: named
                          type: string
                        Boolean: false
                        Embedded:
                          ${'$'}id: embedded.json
                          ${'$'}anchor: embedded
                          type: object
                      properties:
                        self:
                          ${'$'}ref: ''
                        anchor:
                          ${'$'}ref: '#named'
                          description: Reference sibling
                        boolean:
                          ${'$'}ref: '#/${'$'}defs/Boolean'
                        embeddedResource:
                          ${'$'}ref: 'embedded.json'
                        embeddedAnchor:
                          ${'$'}ref: 'embedded.json#embedded'
                """,
            )
        val root = document.componentSchemas.getValue("Root") as SourceObjectSchema
        val anchored = root.definitions.getValue("Anchored") as SourceObjectSchema
        val boolean = root.definitions.getValue("Boolean") as SourceBooleanSchema
        val embedded = root.definitions.getValue("Embedded")
        val anchorReference = document.resolvedReference("#/components/schemas/Root/properties/anchor")

        assertThat(root.identifier).isEqualTo("schemas/root.json")
        assertThat(anchored.anchor).isEqualTo("named")
        assertThat(document.resolvedReference("#/components/schemas/Root/properties/self").target)
            .isSameAs(root)
        assertThat(anchorReference.uri).isEqualTo(URI("https://example.test/specs/schemas/root.json#named"))
        assertThat(anchorReference.target).isSameAs(anchored)
        assertThat((root.properties.getValue("anchor") as SourceObjectSchema).node["description"].textValue())
            .isEqualTo("Reference sibling")
        assertThat(document.resolvedReference("#/components/schemas/Root/properties/boolean").target)
            .isSameAs(boolean)
        assertThat(document.resolvedReference("#/components/schemas/Root/properties/embeddedResource").target)
            .isSameAs(embedded)
        assertThat(document.resolvedReference("#/components/schemas/Root/properties/embeddedAnchor").target)
            .isSameAs(embedded)
    }

    @ParameterizedTest
    @ValueSource(strings = ["3.1.2", "3.2.0"])
    fun `classifies missing external and invalid schema references`(version: String) {
        val document =
            parse(
                version,
                """
                components:
                  schemas:
                    Root:
                      ${'$'}id: schemas/root.json
                      properties:
                        missingPointer:
                          ${'$'}ref: '#/${'$'}defs/Missing'
                        missingAnchor:
                          ${'$'}ref: '#missing'
                        external:
                          ${'$'}ref: '../external.yaml#/components/schemas/External'
                        invalid:
                          ${'$'}ref: 'http://[invalid'
                """,
            )

        assertThat(document.reference("#/components/schemas/Root/properties/missingPointer"))
            .isEqualTo(
                SourceSchemaReferenceResolution.Missing(
                    value = "#/${'$'}defs/Missing",
                    uri = URI("https://example.test/specs/schemas/root.json#/${'$'}defs/Missing"),
                ),
            )
        assertThat(document.reference("#/components/schemas/Root/properties/missingAnchor"))
            .isInstanceOf(SourceSchemaReferenceResolution.Missing::class.java)
        assertThat(document.reference("#/components/schemas/Root/properties/external"))
            .isEqualTo(
                SourceSchemaReferenceResolution.External(
                    value = "../external.yaml#/components/schemas/External",
                    uri = URI("https://example.test/specs/external.yaml#/components/schemas/External"),
                ),
            )
        assertThat(document.reference("#/components/schemas/Root/properties/invalid"))
            .isEqualTo(SourceSchemaReferenceResolution.Invalid("http://[invalid"))
    }

    private fun parse(
        version: String,
        body: String,
    ): SourceOpenApiDocument {
        val document =
            """
            openapi: $version
            info:
              title: Test
              version: "1.0"
            paths: {}
            """.trimIndent() + "\n" + body.trimIndent()
        return SourceOpenApiDocumentParser.parse(
            document,
            URI("https://example.test/specs/openapi.yaml"),
        )
    }

    private fun SourceOpenApiDocument.reference(location: String): SourceSchemaReferenceResolution =
        schemaReferenceResolutions.getValue(location)

    private fun SourceOpenApiDocument.resolvedReference(location: String): SourceSchemaReferenceResolution.Resolved =
        reference(location) as SourceSchemaReferenceResolution.Resolved
}
