package com.cjbooms.fabrikt.cli

import com.beust.jcommander.ParameterException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CodeGenArgsTest {
    @Test
    fun `parses schema-conversion-pointer alone leaving the other two schema flags null`() {
        val args =
            CodeGenArgs.parse(
                arrayOf(
                    "--base-package",
                    "com.example",
                    "--schema-conversion-pointer",
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
                    "--schema-conversion-pointer",
                    "/spec/schemaObject",
                    "--schema-conversion-root-name",
                    "OffersConfig",
                    "--schema-conversion-emit",
                    "/tmp/converted.yaml",
                ),
            )

        assertThat(args.schemaPointer).isEqualTo("/spec/schemaObject")
        assertThat(args.schemaRootName).isEqualTo("OffersConfig")
        assertThat(args.emitConvertedSchema).isNotNull()
        assertThat(args.emitConvertedSchema.toString()).isEqualTo("/tmp/converted.yaml")
    }

    @Test
    fun `rejects schema-conversion-emit without schema-conversion-pointer`() {
        val ex =
            assertThrows<ParameterException> {
                CodeGenArgs.parse(
                    arrayOf(
                        "--base-package",
                        "com.example",
                        "--schema-conversion-emit",
                        "/tmp/converted.yaml",
                    ),
                )
            }
        assertThat(ex.message).contains("requires --schema-conversion-pointer")
    }

    @Test
    fun `rejects schema-conversion-pointer combined with api-fragment`() {
        val ex =
            assertThrows<ParameterException> {
                CodeGenArgs.parse(
                    arrayOf(
                        "--base-package",
                        "com.example",
                        "--schema-conversion-pointer",
                        "/spec/schemaObject",
                        "--api-fragment",
                        "fragment.yaml",
                    ),
                )
            }
        assertThat(ex.message).contains("cannot be combined")
    }
}
