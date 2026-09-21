package com.cjbooms.fabrikt.cli

import com.beust.jcommander.ParameterException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CodeGenArgsTest {
    @Test
    fun `parses api-file with no json-schema-file leaving it null`() {
        val args =
            CodeGenArgs.parse(
                arrayOf(
                    "--base-package",
                    "com.example",
                    "--api-file",
                    "manifest.yaml",
                ),
            )

        assertThat(args.apiFile).isEqualTo("manifest.yaml")
        assertThat(args.jsonSchemaFile).isNull()
        assertThat(args.jsonSchemaRootName).isNull()
    }

    @Test
    fun `parses json-schema-file with a pointer fragment together with json-schema-root-name`() {
        val args =
            CodeGenArgs.parse(
                arrayOf(
                    "--base-package",
                    "com.example",
                    "--json-schema-file",
                    "manifest.yaml#/spec/schemaObject",
                    "--json-schema-root-name",
                    "OffersConfig",
                ),
            )

        assertThat(args.jsonSchemaFile).isEqualTo("manifest.yaml#/spec/schemaObject")
        assertThat(args.jsonSchemaRootName).isEqualTo("OffersConfig")
    }

    @Test
    fun `rejects json-schema-root-name without json-schema-file`() {
        val ex =
            assertThrows<ParameterException> {
                CodeGenArgs.parse(
                    arrayOf(
                        "--base-package",
                        "com.example",
                        "--api-file",
                        "manifest.yaml",
                        "--json-schema-root-name",
                        "OffersConfig",
                    ),
                )
            }
        assertThat(ex.message).contains("requires --json-schema-file")
    }

    @Test
    fun `rejects json-schema-file combined with a non-default api-file`() {
        val ex =
            assertThrows<ParameterException> {
                CodeGenArgs.parse(
                    arrayOf(
                        "--base-package",
                        "com.example",
                        "--api-file",
                        "openapi.yaml",
                        "--json-schema-file",
                        "manifest.yaml#/spec/schemaObject",
                    ),
                )
            }
        assertThat(ex.message).contains("--api-file and --json-schema-file cannot be combined")
    }

    @Test
    fun `accepts json-schema-file combined with api-fragment`() {
        val args =
            CodeGenArgs.parse(
                arrayOf(
                    "--base-package",
                    "com.example",
                    "--json-schema-file",
                    "manifest.yaml#/spec/schemaObject",
                    "--api-fragment",
                    "common.yaml",
                ),
            )

        assertThat(args.jsonSchemaFile).isEqualTo("manifest.yaml#/spec/schemaObject")
        assertThat(args.apiFragments).containsExactly("common.yaml")
    }
}
