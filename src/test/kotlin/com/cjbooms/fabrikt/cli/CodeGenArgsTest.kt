package com.cjbooms.fabrikt.cli

import com.beust.jcommander.ParameterException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CodeGenArgsTest {
    @Test
    fun `parses json-schema-pointer alone leaving the other two schema flags null`() {
        val args =
            CodeGenArgs.parse(
                arrayOf(
                    "--base-package",
                    "com.example",
                    "--json-schema-pointer",
                    "/spec/schemaObject",
                ),
            )

        assertThat(args.schemaPointer).isEqualTo("/spec/schemaObject")
        assertThat(args.schemaRootName).isNull()
        assertThat(args.emitConvertedSchema).isNull()
    }

    @Test
    fun `parses all three schema flags together`() {
        val args =
            CodeGenArgs.parse(
                arrayOf(
                    "--base-package",
                    "com.example",
                    "--json-schema-pointer",
                    "/spec/schemaObject",
                    "--json-schema-root-name",
                    "OffersConfig",
                    "--json-schema-emit",
                    "/tmp/converted.yaml",
                ),
            )

        assertThat(args.schemaPointer).isEqualTo("/spec/schemaObject")
        assertThat(args.schemaRootName).isEqualTo("OffersConfig")
        assertThat(args.emitConvertedSchema).isNotNull()
        assertThat(args.emitConvertedSchema.toString()).isEqualTo("/tmp/converted.yaml")
    }

    @Test
    fun `rejects json-schema-emit without json-schema-pointer`() {
        val ex =
            assertThrows<ParameterException> {
                CodeGenArgs.parse(
                    arrayOf(
                        "--base-package",
                        "com.example",
                        "--json-schema-emit",
                        "/tmp/converted.yaml",
                    ),
                )
            }
        assertThat(ex.message).contains("requires --json-schema-pointer")
    }

    @Test
    fun `rejects json-schema-pointer combined with api-fragment`() {
        val ex =
            assertThrows<ParameterException> {
                CodeGenArgs.parse(
                    arrayOf(
                        "--base-package",
                        "com.example",
                        "--json-schema-pointer",
                        "/spec/schemaObject",
                        "--api-fragment",
                        "fragment.yaml",
                    ),
                )
            }
        assertThat(ex.message).contains("cannot be combined")
    }
}
