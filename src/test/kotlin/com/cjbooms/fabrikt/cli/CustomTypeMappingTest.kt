package com.cjbooms.fabrikt.cli

import com.beust.jcommander.ParameterException
import com.squareup.kotlinpoet.ClassName
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CustomTypeMappingTest {
    @Test
    fun `parses a custom type mapping with a kotlinx serializer`() {
        val mapping =
            CustomTypeMapping.parse(
                "string:Duration=java.time.Duration;kotlinx=com.example.DurationAsIsoStringSerializer",
            )

        assertThat(mapping.openApiType).isEqualTo("string")
        assertThat(mapping.format).isEqualTo("duration")
        assertThat(mapping.kotlinType.canonicalName).isEqualTo("java.time.Duration")
        assertThat(mapping.kotlinxSerializer?.canonicalName).isEqualTo("com.example.DurationAsIsoStringSerializer")
    }

    @Test
    fun `rejects an incomplete custom type mapping`() {
        assertThrows<ParameterException> {
            CustomTypeMapping.parse("duration=java.time.Duration")
        }
    }

    @Test
    fun `rejects a Kotlin type without a package`() {
        assertThrows<ParameterException> {
            CustomTypeMapping.parse("string:duration=Duration")
        }
    }

    @Test
    fun `accepts comma separated CLI mappings`() {
        val args =
            CodeGenArgs.parse(
                arrayOf(
                    "--base-package",
                    "com.example",
                    "--custom-type-mapping",
                    "string:duration=java.time.Duration;" +
                        "kotlinx=com.example.DurationAsIsoStringSerializer,string:uri=java.net.URI",
                ),
            )

        assertThat(args.customTypeMappings).containsExactly(
            CustomTypeMapping(
                openApiType = "string",
                format = "duration",
                kotlinType = ClassName("java.time", "Duration"),
                kotlinxSerializer = ClassName("com.example", "DurationAsIsoStringSerializer"),
            ),
            CustomTypeMapping(
                openApiType = "string",
                format = "uri",
                kotlinType = ClassName("java.net", "URI"),
            ),
        )
    }
}
