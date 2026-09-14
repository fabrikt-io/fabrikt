package com.cjbooms.fabrikt.cli

import com.beust.jcommander.IStringConverter
import com.beust.jcommander.ParameterException
import com.squareup.kotlinpoet.ClassName

data class CustomTypeMapping(
    val openApiType: String,
    val format: String,
    val kotlinType: ClassName,
    val kotlinxSerializer: ClassName? = null,
) {
    companion object {
        private val customTypeMappingPattern =
            Regex(
                """^(?!.*\s)(?<openApiType>[^:]+):(?<format>[^=]+)=(?<kotlinType>[^;]+)(?:;kotlinx=(?<kotlinxSerializer>.+))?$""",
            )

        fun parse(value: String): CustomTypeMapping {
            val match =
                customTypeMappingPattern.matchEntire(value)
                    ?: throw ParameterException("Custom type mapping must have the form type:format=KotlinFqcn[;kotlinx=SerializerFqcn].")

            return CustomTypeMapping(
                openApiType = match.groups["openApiType"]!!.value.lowercase(),
                format = match.groups["format"]!!.value.lowercase(),
                kotlinType = className(match.groups["kotlinType"]!!.value, "Kotlin type"),
                kotlinxSerializer = match.groups["kotlinxSerializer"]?.value?.let { className(it, "kotlinx serializer") },
            )
        }

        private fun className(
            value: String,
            description: String,
        ): ClassName =
            try {
                ClassName.bestGuess(value).also {
                    require(it.packageName.isNotEmpty())
                }
            } catch (_: IllegalArgumentException) {
                throw ParameterException("$description '$value' must be a fully qualified class name.")
            }
    }
}

class CustomTypeMappingConverter : IStringConverter<CustomTypeMapping> {
    override fun convert(value: String): CustomTypeMapping = CustomTypeMapping.parse(value)
}
