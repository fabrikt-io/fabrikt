package com.cjbooms.fabrikt.parser

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path

class SourceOpenApiDocumentGraphParserTest {
    @Test
    fun `loads YAML and JSON schema documents recursively from files`(
        @TempDir tempDir: Path,
    ) {
        val rootUri = tempDir.resolve("openapi.yaml").toUri()
        val schemasUri = tempDir.resolve("schemas.yaml").toUri()
        val addressUri = tempDir.resolve("address.json").toUri()
        Files.writeString(
            schemasUri.toPath(),
            """
            ${'$'}schema: https://json-schema.org/draft/2020-12/schema
            ${'$'}defs:
              User:
                type: object
                properties:
                  address:
                    ${'$'}ref: address.json
            """.trimIndent(),
        )
        Files.writeString(
            addressUri.toPath(),
            """
            {
              "${'$'}schema": "https://json-schema.org/draft/2020-12/schema",
              "type": "object",
              "properties": {
                "street": { "type": "string" }
              }
            }
            """.trimIndent(),
        )

        val graph =
            SourceOpenApiDocumentGraphParser.parse(
                openApiWithSchemas(
                    """
                    User:
                      ${'$'}ref: 'schemas.yaml#/${'$'}defs/User'
                    """,
                ),
                rootUri,
                DefaultSourceDocumentLoader(),
            )
        val userTarget =
            graph.documentsByUri
                .getValue(schemasUri)
                .schemasByLocation
                .getValue("#/${'$'}defs/User")
        val addressTarget =
            graph.documentsByUri
                .getValue(addressUri)
                .schemasByLocation
                .getValue("#")

        assertThat(graph.documentsByUri).containsOnlyKeys(rootUri, schemasUri, addressUri)
        assertThat(graph.loadFailures).isEmpty()
        assertThat(graph.resolvedReference(rootUri, "#/components/schemas/User").target)
            .isSameAs(userTarget)
        assertThat(graph.resolvedReference(schemasUri, "#/${'$'}defs/User/properties/address").target)
            .isSameAs(addressTarget)
    }

    @Test
    fun `caches documents and resolves cycles across files`() {
        val rootUri = URI("https://example.test/root.yaml")
        val firstUri = URI("https://example.test/first.yaml")
        val secondUri = URI("https://example.test/second.yaml")
        val documents =
            mapOf(
                firstUri to
                    """
                    ${'$'}schema: https://json-schema.org/draft/2020-12/schema
                    ${'$'}ref: second.yaml
                    """.trimIndent(),
                secondUri to
                    """
                    ${'$'}schema: https://json-schema.org/draft/2020-12/schema
                    ${'$'}ref: first.yaml
                    """.trimIndent(),
            )
        val loadCounts = mutableMapOf<URI, Int>()
        val loader =
            SourceDocumentLoader { uri ->
                loadCounts.merge(uri, 1, Int::plus)
                documents.getValue(uri)
            }

        val graph =
            SourceOpenApiDocumentGraphParser.parse(
                openApiWithSchemas(
                    """
                    FirstUse:
                      ${'$'}ref: first.yaml
                    SecondUse:
                      ${'$'}ref: first.yaml
                    """,
                ),
                rootUri,
                loader,
            )
        val firstTarget =
            graph.documentsByUri
                .getValue(firstUri)
                .schemasByLocation
                .getValue("#")
        val secondTarget =
            graph.documentsByUri
                .getValue(secondUri)
                .schemasByLocation
                .getValue("#")

        assertThat(loadCounts).containsExactlyInAnyOrderEntriesOf(mapOf(firstUri to 1, secondUri to 1))
        assertThat(graph.loadFailures).isEmpty()
        assertThat(graph.resolvedReference(rootUri, "#/components/schemas/FirstUse").target)
            .isSameAs(firstTarget)
        assertThat(graph.resolvedReference(firstUri, "#").target)
            .isSameAs(secondTarget)
        assertThat(graph.resolvedReference(secondUri, "#").target)
            .isSameAs(firstTarget)
    }

    @Test
    fun `loads schemas from an external OpenAPI document`() {
        val rootUri = URI("https://example.test/root.yaml")
        val externalUri = URI("https://example.test/external.yaml")
        val loader =
            SourceDocumentLoader { uri ->
                assertThat(uri).isEqualTo(externalUri)
                openApiWithSchemas(
                    """
                    External:
                      type: string
                    """,
                )
            }

        val graph =
            SourceOpenApiDocumentGraphParser.parse(
                openApiWithSchemas(
                    """
                    Alias:
                      ${'$'}ref: 'external.yaml#/components/schemas/External'
                    """,
                ),
                rootUri,
                loader,
            )
        val externalTarget =
            graph.documentsByUri
                .getValue(externalUri)
                .schemasByLocation
                .getValue("#/components/schemas/External")

        assertThat(graph.resolvedReference(rootUri, "#/components/schemas/Alias").target)
            .isSameAs(externalTarget)
    }

    @Test
    fun `records load failures once and reports missing targets in loaded documents`() {
        val rootUri = URI("https://example.test/root.yaml")
        val loadedUri = URI("https://example.test/loaded.yaml")
        val unavailableUri = URI("https://example.test/unavailable.yaml")
        val loadCounts = mutableMapOf<URI, Int>()
        val loader =
            SourceDocumentLoader { uri ->
                loadCounts.merge(uri, 1, Int::plus)
                when (uri) {
                    loadedUri -> "type: object"
                    else -> throw IOException("Unavailable: $uri")
                }
            }

        val graph =
            SourceOpenApiDocumentGraphParser.parse(
                openApiWithSchemas(
                    """
                    MissingTarget:
                      ${'$'}ref: 'loaded.yaml#/${'$'}defs/Missing'
                    UnavailableOnce:
                      ${'$'}ref: unavailable.yaml
                    UnavailableTwice:
                      ${'$'}ref: unavailable.yaml
                    """,
                ),
                rootUri,
                loader,
            )

        assertThat(loadCounts).containsExactlyInAnyOrderEntriesOf(mapOf(loadedUri to 1, unavailableUri to 1))
        assertThat(graph.reference(rootUri, "#/components/schemas/MissingTarget"))
            .isInstanceOf(SourceSchemaReferenceResolution.Missing::class.java)
        assertThat(graph.reference(rootUri, "#/components/schemas/UnavailableOnce"))
            .isInstanceOf(SourceSchemaReferenceResolution.External::class.java)
        assertThat(graph.reference(rootUri, "#/components/schemas/UnavailableTwice"))
            .isInstanceOf(SourceSchemaReferenceResolution.External::class.java)
        assertThat(graph.loadFailures.getValue(unavailableUri).exception)
            .isInstanceOf(IOException::class.java)
            .hasMessage("Unavailable: $unavailableUri")
    }

    private fun openApiWithSchemas(schemas: String): String {
        val document =
            """
            openapi: 3.1.0
            info:
              title: Test
              version: "1.0"
            paths: {}
            components:
              schemas:
            """.trimIndent()
        return "$document\n${schemas.trimIndent().prependIndent("    ")}"
    }

    private fun SourceOpenApiDocumentGraph.reference(
        documentUri: URI,
        schemaLocation: String,
    ): SourceSchemaReferenceResolution =
        schemaReferenceResolutions.getValue(
            SourceSchemaReferenceLocation(documentUri, schemaLocation),
        )

    private fun SourceOpenApiDocumentGraph.resolvedReference(
        documentUri: URI,
        schemaLocation: String,
    ): SourceSchemaReferenceResolution.Resolved = reference(documentUri, schemaLocation) as SourceSchemaReferenceResolution.Resolved

    private fun URI.toPath(): Path = Path.of(this)
}
