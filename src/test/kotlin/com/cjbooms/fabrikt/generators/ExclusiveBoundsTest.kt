package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.model.ModelGenerator
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.ModelNameRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URI

class ExclusiveBoundsTest {
    @BeforeEach
    fun init() {
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.HTTP_MODELS),
        )
        ModelNameRegistry.clear()
    }

    @Test
    fun `numeric exclusiveMinimum and exclusiveMaximum from a 3_1 spec render as exclusive validation annotations`() {
        val api =
            """
            openapi: 3.1.0
            info:
              title: exclusive bounds
              version: 1.0.0
            paths: {}
            components:
              schemas:
                Threshold:
                  type: object
                  required:
                    - score
                    - ceiling
                  properties:
                    score:
                      type: integer
                      exclusiveMinimum: 5
                    ceiling:
                      type: integer
                      exclusiveMaximum: 10
            """.trimIndent()

        val sourceApi = SourceApi(api, baseUri = URI.create("file:///exclusive-bounds.yaml"))
        val models = ModelGenerator(Packages("examples.exclusiveBounds"), sourceApi).generate()
        val generated = models.files.joinToString("\n") { it.toString() }
        assertThat(generated).contains(
            "@get:DecimalMin(\n" +
                "    value = \"5\",\n" +
                "    inclusive = false,\n" +
                "  )",
        )
        assertThat(generated).contains(
            "@get:DecimalMax(\n" +
                "    value = \"10\",\n" +
                "    inclusive = false,\n" +
                "  )",
        )
    }
}
