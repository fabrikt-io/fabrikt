package com.cjbooms.fabrikt.cli

import com.beust.jcommander.ParameterException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CodeGenArgsTest {
    @Test
    fun `parses api-file without a fragment leaving json-schema-root-name null`() {
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
        assertThat(args.jsonSchemaRootName).isNull()
    }

    @Test
    fun `parses an api-file JSON Pointer fragment together with json-schema-root-name`() {
        val args =
            CodeGenArgs.parse(
                arrayOf(
                    "--base-package",
                    "com.example",
                    "--api-file",
                    "manifest.yaml#/spec/schemaObject",
                    "--json-schema-root-name",
                    "OffersConfig",
                ),
            )

        assertThat(args.apiFile).isEqualTo("manifest.yaml#/spec/schemaObject")
        assertThat(args.jsonSchemaRootName).isEqualTo("OffersConfig")
    }

    @Test
    fun `rejects json-schema-root-name without an api-file fragment`() {
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
        assertThat(ex.message).contains("requires a JSON Pointer fragment on --api-file")
    }

    @Test
    fun `rejects an api-file fragment combined with api-fragment`() {
        val ex =
            assertThrows<ParameterException> {
                CodeGenArgs.parse(
                    arrayOf(
                        "--base-package",
                        "com.example",
                        "--api-file",
                        "manifest.yaml#/spec/schemaObject",
                        "--api-fragment",
                        "fragment.yaml",
                    ),
                )
            }
        assertThat(ex.message).contains("cannot be combined")
    }
}
