package com.cjbooms.fabrikt.parser

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class SourceSchemaEntryPointCollectorTest {
    @ParameterizedTest
    @ValueSource(strings = ["3.0.4", "3.1.2", "3.2.0"])
    fun `collects shared schema entry points across OpenAPI versions`(version: String) {
        val document =
            SourceOpenApiDocumentParser.parse(
                """
                openapi: $version
                info:
                  title: Test
                  version: "1.0"
                paths:
                  /pets:
                    parameters:
                      - name: pathFilter
                        in: query
                        schema:
                          type: string
                    post:
                      parameters:
                        - name: operationFilter
                          in: query
                          content:
                            application/json:
                              schema:
                                type: string
                      requestBody:
                        content:
                          application/json:
                            schema:
                              type: object
                              properties:
                                names/with~escape:
                                  type: array
                                  items:
                                    oneOf:
                                      - type: string
                                      - type: boolean
                            encoding:
                              value:
                                headers:
                                  X-Encoding:
                                    schema:
                                      type: string
                      responses:
                        '200':
                          description: Success
                          headers:
                            X-Response:
                              schema:
                                type: integer
                          content:
                            application/json:
                              schema:
                                type: object
                      callbacks:
                        changed:
                          '{${'$'}request.body#/callbackUrl}':
                            post:
                              requestBody:
                                content:
                                  application/json:
                                    schema:
                                      type: object
                              responses:
                                '204':
                                  description: Accepted
                components:
                  schemas:
                    Pet:
                      type: object
                  parameters:
                    Limit:
                      name: limit
                      in: query
                      schema:
                        type: integer
                  headers:
                    RequestId:
                      content:
                        text/plain:
                          schema:
                            type: string
                  requestBodies:
                    PetBody:
                      content:
                        application/json:
                          schema:
                            type: object
                  responses:
                    PetResponse:
                      description: Pet response
                      headers:
                        X-Component:
                          schema:
                            type: string
                      content:
                        application/json:
                          schema:
                            type: object
                  callbacks:
                    ComponentCallback:
                      '{${'$'}request.body#/callbackUrl}':
                        post:
                          requestBody:
                            content:
                              application/json:
                                schema:
                                  type: object
                          responses:
                            '204':
                              description: Accepted
                """.trimIndent(),
            )

        assertThat(document.schemaEntryPoints.keys)
            .containsExactlyInAnyOrder(
                "#/components/schemas/Pet",
                "#/components/parameters/Limit/schema",
                "#/components/headers/RequestId/content/text~1plain/schema",
                "#/components/requestBodies/PetBody/content/application~1json/schema",
                "#/components/responses/PetResponse/headers/X-Component/schema",
                "#/components/responses/PetResponse/content/application~1json/schema",
                "#/components/callbacks/ComponentCallback/{${'$'}request.body#~1callbackUrl}/post/requestBody/content/application~1json/schema",
                "#/paths/~1pets/parameters/0/schema",
                "#/paths/~1pets/post/parameters/0/content/application~1json/schema",
                "#/paths/~1pets/post/requestBody/content/application~1json/schema",
                "#/paths/~1pets/post/requestBody/content/application~1json/encoding/value/headers/X-Encoding/schema",
                "#/paths/~1pets/post/responses/200/headers/X-Response/schema",
                "#/paths/~1pets/post/responses/200/content/application~1json/schema",
                "#/paths/~1pets/post/callbacks/changed/{${'$'}request.body#~1callbackUrl}/post/requestBody/content/application~1json/schema",
            )
        assertThat(document.componentSchemas["Pet"])
            .isSameAs(document.schemaEntryPoints["#/components/schemas/Pet"])

        val requestSchemaLocation = "#/paths/~1pets/post/requestBody/content/application~1json/schema"
        val propertyLocation = "$requestSchemaLocation/properties/names~1with~0escape"
        val itemsLocation = "$propertyLocation/items"
        val requestSchema = document.schemaEntryPoints.getValue(requestSchemaLocation) as SourceObjectSchema
        val propertySchema = requestSchema.properties.getValue("names/with~escape") as SourceObjectSchema
        val itemsSchema = propertySchema.items as SourceObjectSchema

        assertThat(document.schemasByLocation).containsAllEntriesOf(document.schemaEntryPoints)
        assertThat(document.schemasByLocation[propertyLocation]).isSameAs(propertySchema)
        assertThat(document.schemasByLocation[itemsLocation]).isSameAs(itemsSchema)
        assertThat(document.schemasByLocation["$itemsLocation/oneOf/0"]).isSameAs(itemsSchema.oneOf[0])
        assertThat(document.schemasByLocation["$itemsLocation/oneOf/1"]).isSameAs(itemsSchema.oneOf[1])
        assertThat(document.schemasByLocation).hasSize(document.schemaEntryPoints.size + 4)
    }

    @ParameterizedTest
    @ValueSource(strings = ["3.1.2", "3.2.0"])
    fun `collects webhooks and reusable path items in OpenAPI 3_1 and later`(version: String) {
        val document =
            SourceOpenApiDocumentParser.parse(
                """
                openapi: $version
                info:
                  title: Test
                  version: "1.0"
                paths: {}
                webhooks:
                  x-newPet:
                    post:
                      requestBody:
                        content:
                          application/json:
                            schema:
                              type: object
                      responses:
                        '204':
                          description: Accepted
                components:
                  pathItems:
                    Reusable:
                      get:
                        responses:
                          '200':
                            description: Success
                            content:
                              application/json:
                                schema:
                                  type: object
                """.trimIndent(),
            )

        assertThat(document.schemaEntryPoints.keys)
            .containsExactlyInAnyOrder(
                "#/webhooks/x-newPet/post/requestBody/content/application~1json/schema",
                "#/components/pathItems/Reusable/get/responses/200/content/application~1json/schema",
            )
    }

    @Test
    fun `collects OpenAPI 3_2 media type and operation schema entry points`() {
        val document =
            SourceOpenApiDocumentParser.parse(
                """
                openapi: 3.2.0
                info:
                  title: Test
                  version: "1.0"
                paths:
                  /events:
                    query:
                      responses:
                        '200':
                          description: Events
                          content:
                            application/json-seq:
                              itemSchema:
                                type: object
                    additionalOperations:
                      PURGE:
                        requestBody:
                          content:
                            multipart/mixed:
                              schema:
                                type: array
                              prefixEncoding:
                                - headers:
                                    X-Prefix:
                                      schema:
                                        type: string
                              itemEncoding:
                                headers:
                                  X-Item:
                                    schema:
                                      type: integer
                              encoding:
                                envelope:
                                  encoding:
                                    nested:
                                      headers:
                                        X-Nested:
                                          schema:
                                            type: boolean
                        responses:
                          '204':
                            description: Purged
                components:
                  mediaTypes:
                    EventStream:
                      schema:
                        type: array
                      itemSchema:
                        type: object
                """.trimIndent(),
            )

        assertThat(document.schemaEntryPoints.keys)
            .containsExactlyInAnyOrder(
                "#/components/mediaTypes/EventStream/schema",
                "#/components/mediaTypes/EventStream/itemSchema",
                "#/paths/~1events/query/responses/200/content/application~1json-seq/itemSchema",
                "#/paths/~1events/additionalOperations/PURGE/requestBody/content/multipart~1mixed/schema",
                "#/paths/~1events/additionalOperations/PURGE/requestBody/content/multipart~1mixed/prefixEncoding/0/headers/X-Prefix/schema",
                "#/paths/~1events/additionalOperations/PURGE/requestBody/content/multipart~1mixed/itemEncoding/headers/X-Item/schema",
                "#/paths/~1events/additionalOperations/PURGE/requestBody/content/multipart~1mixed/encoding/envelope/encoding/nested/headers/X-Nested/schema",
            )
    }
}
