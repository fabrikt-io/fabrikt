package com.cjbooms.fabrikt.model

import com.cjbooms.fabrikt.parser.OpenApiDocumentParser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.logging.Logger
import java.util.logging.SimpleFormatter
import java.util.logging.StreamHandler

class QueryOperationTest {
    @TempDir
    lateinit var directory: Path

    @ParameterizedTest
    @ValueSource(strings = ["null", "false", "[]", "invalid", "{}", "{responses: {}}", "{responses: []}"])
    fun `malformed QUERY operations do not prevent generation of other operations`(value: String) {
        val spec =
            """
            openapi: 3.2.0
            info:
              title: Malformed query
              version: '1'
            paths:
              /search:
                query: $value
                get:
                  responses:
                    '204':
                      description: Empty
            """.trimIndent()
        val messages = ByteArrayOutputStream()
        val handler = StreamHandler(messages, SimpleFormatter())
        val logger = Logger.getGlobal()
        logger.addHandler(handler)
        try {
            val document = OpenApiDocumentParser.parse(spec).asOpenApi3Document()
            assertThat(
                document.paths
                    .getValue("/search")
                    .operations.keys,
            ).containsExactly("get")
            handler.flush()
            assertThat(messages.toString()).contains("Ignoring malformed QUERY /search")
        } finally {
            logger.removeHandler(handler)
            handler.close()
        }
    }

    @Test
    fun `QUERY resolves relative schema references using the source document location`() {
        Files.writeString(
            directory.resolve("requests.yaml"),
            """
            SearchRequest:
              type: object
              properties:
                name:
                  type: string
            """.trimIndent(),
        )
        val document =
            OpenApiDocumentParser
                .parse(
                    """
                    openapi: 3.2.0
                    info:
                      title: External query
                      version: '1'
                    paths:
                      /search:
                        query:
                          requestBody:
                            content:
                              application/json:
                                schema:
                                  ${'$'}ref: 'requests.yaml#/SearchRequest'
                          responses:
                            '204':
                              description: Empty
                    """.trimIndent(),
                    directory.resolve("api.yaml").toUri(),
                ).asOpenApi3Document()
        val schema =
            document.paths
                .getValue("/search")
                .operations
                .getValue("query")
                .requestBody.contentMediaTypes
                .getValue("application/json")
                .schema
        assertThat(schema.properties.getValue("name").type).isEqualTo("string")
    }

    @Test
    fun `QUERY preserves operation metadata references and inline schema semantics alongside GET`() {
        val document =
            OpenApiDocumentParser
                .parse(
                    """
                    openapi: 3.2.0
                    info:
                      title: Query operations
                      version: '1'
                    paths:
                      /search:
                        parameters:
                          - name: tenant
                            in: header
                            schema:
                              type: string
                        get:
                          operationId: getSearch
                          responses:
                            '204':
                              description: Empty
                        query:
                          operationId: search
                          summary: Find matches
                          tags: [Search]
                          deprecated: true
                          security:
                            - Token: []
                          parameters:
                            - ${'$'}ref: '#/components/parameters/Limit'
                          requestBody:
                            required: true
                            content:
                              application/json:
                                schema:
                                  type: [string, integer, 'null']
                          responses:
                            '200':
                              ${'$'}ref: '#/components/responses/Matches'
                    components:
                      parameters:
                        Limit:
                          name: limit
                          in: query
                          schema:
                            type: integer
                      responses:
                        Matches:
                          description: Matches
                          content:
                            application/json:
                              schema:
                                ${'$'}ref: '#/components/schemas/Result'
                      schemas:
                        Result:
                          type: object
                          properties:
                            name:
                              type: string
                      securitySchemes:
                        Token:
                          type: http
                          scheme: bearer
                    """.trimIndent(),
                ).asOpenApi3Document()
        val path = document.paths.getValue("/search")
        assertThat(path.operations.keys).containsExactly("get", "query")
        assertThat(path.parameters.single().name).isEqualTo("tenant")
        assertThat(path.operations.getValue("get").operationId).isEqualTo("getSearch")
        val query = path.operations.getValue("query")
        assertThat(query.operationId).isEqualTo("search")
        assertThat(query.summary).isEqualTo("Find matches")
        assertThat(query.tags).containsExactly("Search")
        assertThat(query.isDeprecated).isTrue()
        assertThat(query.hasSecurityRequirements()).isTrue()
        assertThat(query.parameters.single().name).isEqualTo("limit")
        assertThat(
            query.parameters
                .single()
                .schema.type,
        ).isEqualTo("integer")
        val request =
            query.requestBody.contentMediaTypes
                .getValue("application/json")
                .schema
        assertThat(request.jsonPathFromRoot).isEqualTo("/paths/~1search/query/requestBody/content/application~1json/schema")
        assertThat(request.type).isNull()
        assertThat(request.hasMultipleNonNullTypes).isTrue()
        assertThat(request.isNullable).isTrue()
        assertThat(
            query.responses
                .getValue("200")
                .contentMediaTypes
                .getValue("application/json")
                .schema,
        ).isEqualTo(document.schemas.getValue("Result"))
        assertThat(
            document.paths
                .getValue("/search")
                .operations
                .getValue("query"),
        ).isEqualTo(query)
    }
}
