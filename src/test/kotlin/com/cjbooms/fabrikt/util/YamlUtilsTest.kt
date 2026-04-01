package com.cjbooms.fabrikt.util

import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.KaizenParserExtensions.getEnumValues
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class YamlUtilsTest {

    private val inputYaml =
        """
        | ValidPreferentialCountries:
        |   type: "string"
        |   x-extensible-enum:
        |     - "NO"
        """.trimMargin()

    @Test
    fun `reading yaml tree does not strip inverted commas from string enum values`() {
        val jsonNode: JsonNode = YamlUtils.objectMapper.readValue(inputYaml)

        val enum = jsonNode.findValue("x-extensible-enum")
        assertTrue(enum.isArray)
        assertThat(enum.first().toString()).isEqualTo("\"NO\"")
    }

    @Test
    fun `merging yaml trees does not strip inverted commas from string enum values`() {
        val result = YamlUtils.mergeYamlTrees(inputYaml, inputYaml)
        val jsonNode: JsonNode = YamlUtils.objectMapper.readValue(result)

        val enum = jsonNode.findValue("x-extensible-enum")
        assertTrue(enum.isArray)
        assertThat(enum.first().toString()).isEqualTo("\"NO\"")
    }

    @Test
    fun `enum values referenced via YAML aliases are generated correctly`() {
        val spec =
            """
            |openapi: "3.0.0"
            |info:
            |  title: Test
            |  version: "1.0"
            |paths: {}
            |components:
            |  schemas:
            |    AnchorOwner:
            |      type: object
            |      properties:
            |        status:
            |          type: string
            |          enum: &status_values
            |            - Active
            |            - Inactive
            |            - Pending
            |    AliasConsumer:
            |      type: object
            |      properties:
            |        status:
            |          type: string
            |          enum: *status_values
            """.trimMargin()

        val sourceApi = SourceApi.create(spec, emptyList())
        val aliasConsumer = sourceApi.openApi3.schemas["AliasConsumer"]!!
        val statusSchema = aliasConsumer.properties["status"]!!

        assertThat(statusSchema.getEnumValues()).containsExactly("Active", "Inactive", "Pending")
    }

    @Test
    fun `containsYamlAnchorsAndAliases returns true when both anchor and alias are present`() {
        val spec =
            """
            |openapi: "3.0.0"
            |info:
            |  title: Test
            |  version: "1.0"
            |paths: {}
            |components:
            |  schemas:
            |    AnchorOwner:
            |      type: object
            |      properties:
            |        status:
            |          type: string
            |          enum: &status_values
            |            - Active
            |            - Inactive
            |    AliasConsumer:
            |      type: object
            |      properties:
            |        status:
            |          type: string
            |          enum: *status_values
            """.trimMargin()

        assertThat(YamlUtils.containsYamlAnchorsAndAliases(spec)).isTrue()
    }

    @Test
    fun `containsYamlAnchorsAndAliases returns false when only anchor is present`() {
        val spec =
            """
            |openapi: "3.0.0"
            |info:
            |  title: Test
            |  version: "1.0"
            |paths: {}
            |components:
            |  schemas:
            |    AnchorOwner:
            |      type: object
            |      properties:
            |        status:
            |          type: string
            |          enum: &status_values
            |            - Active
            |            - Inactive
            """.trimMargin()

        assertThat(YamlUtils.containsYamlAnchorsAndAliases(spec)).isFalse()
    }

    @Test
    fun `containsYamlAnchorsAndAliases returns false when only alias is present`() {
        val spec =
            """
            |openapi: "3.0.0"
            |info:
            |  title: Test
            |  version: "1.0"
            |paths: {}
            |components:
            |  schemas:
            |    AliasConsumer:
            |      type: object
            |      properties:
            |        status:
            |          type: string
            |          enum: *status_values
            """.trimMargin()

        assertThat(YamlUtils.containsYamlAnchorsAndAliases(spec)).isFalse()
    }

    @Test
    fun `containsYamlAnchorsAndAliases returns true when alias is referenced in flow-style mapping`() {
        val spec =
            """
            |openapi: "3.0.0"
            |info:
            |  title: Test
            |  version: "1.0"
            |paths: {}
            |components:
            |  schemas:
            |    AnchorOwner:
            |      type: object
            |      properties:
            |        status:
            |          type: string
            |          enum: &status_values
            |            - Active
            |            - Inactive
            |    AliasConsumer:
            |      type: object
            |      properties:
            |        status: {type: string, enum: *status_values}
            """.trimMargin()

        assertThat(YamlUtils.containsYamlAnchorsAndAliases(spec)).isTrue()
    }

    @Test
    fun `containsYamlAnchorsAndAliases returns false when neither anchor nor alias is present`() {
        val spec =
            """
            |openapi: "3.0.0"
            |info:
            |  title: Test
            |  version: "1.0"
            |paths: {}
            |components:
            |  schemas:
            |    Simple:
            |      type: object
            |      properties:
            |        status:
            |          type: string
            |          enum:
            |            - Active
            |            - Inactive
            """.trimMargin()

        assertThat(YamlUtils.containsYamlAnchorsAndAliases(spec)).isFalse()
    }

    @Test
    fun `encoding object is correctly parsed by Kaizen parser`() {
        val encodingInput =
            """
        | openapi: 3.0.1
        | info:
        |   title: Sample API
        |   version: "1.0"
        | paths:
        |   /endpoint:
        |     post:
        |       requestBody:
        |         required: true
        |         content:
        |           application/json:
        |             schema:
        |               type: object
        |               properties:
        |                 image:
        |                   type: string
        |                   format: binary
        |             encoding:
        |               image:
        |                 contentType: image/png, image/jpeg, image/webp
        |       responses:
        |         200:
        |           description: Everything OK
        """.trimMargin()

        val api = YamlUtils.parseOpenApi(encodingInput)

        assertThat(api.isValid).isEqualTo(true)
    }
}
