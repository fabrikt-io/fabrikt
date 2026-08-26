package com.cjbooms.fabrikt.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLFactoryBuilder
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.yaml.snakeyaml.LoaderOptions

internal object YamlObjectMapper {
    val instance: ObjectMapper =
        ObjectMapper(
            YAMLFactory
                .builder()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                .increaseMaxFileSize()
                .build(),
        ).registerKotlinModule()
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
}

internal fun YAMLFactoryBuilder.increaseMaxFileSize(): YAMLFactoryBuilder =
    loaderOptions(
        LoaderOptions().apply {
            codePointLimit = 100 * 1024 * 1024 // 100MB
        },
    )
