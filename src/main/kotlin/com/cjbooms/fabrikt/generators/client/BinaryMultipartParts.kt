package com.cjbooms.fabrikt.generators.client

import com.cjbooms.fabrikt.generators.GeneratorUtils.getMultipartSchema
import com.cjbooms.fabrikt.generators.GeneratorUtils.hasMultipartRequestBody
import com.cjbooms.fabrikt.model.IncomingParameter
import com.cjbooms.fabrikt.model.OpenApiOperation
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.joinToCode

internal data class BinaryMultipartParts(
    val parameterName: String,
    val patterns: List<String>,
    val fixedNames: Set<String>,
) {
    fun validateName(name: String): CodeBlock =
        CodeBlock
            .builder()
            .apply {
                add(
                    "require(%N.isNotEmpty() && %N.all { it in %S }) { %S }\n",
                    name,
                    name,
                    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789._-",
                    "Dynamic multipart names must contain only ASCII letters, digits, '.', '_' or '-'",
                )
                if (fixedNames.isNotEmpty()) {
                    add(
                        "require(%N !in setOf(%L)) { %S }\n",
                        name,
                        fixedNames.map { CodeBlock.of("%S", it) }.joinToCode(", "),
                        "Dynamic multipart name conflicts with a fixed property",
                    )
                }
                val matches = patterns.map { CodeBlock.of("Regex(%S).containsMatchIn(%N)", it, name) }.joinToCode(" || ")
                add("require(%L) { %S }", matches, "Dynamic multipart name does not match patternProperties")
            }.build()

    companion object {
        fun forOperation(
            operation: OpenApiOperation,
            parameters: List<IncomingParameter>,
            warn: Boolean = false,
        ): BinaryMultipartParts? {
            if (!operation.hasMultipartRequestBody()) return null
            val schema = operation.requestBody.getMultipartSchema() ?: return null
            val declaration = schema.binaryMultipartPatterns ?: return null
            val reason =
                declaration.unsupportedReason
                    ?: declaration.patterns.firstOrNull { !isSupportedPattern(it) }?.let { "unsupported name pattern '$it'" }
                    ?: schema.properties.entries
                        .firstOrNull { (name, value) ->
                            declaration.patterns.any { Regex(it).containsMatchIn(name) } &&
                                (value.type != "string" || value.format != "binary")
                        }?.let { "fixed property '${it.key}' also matches a binary pattern but is not binary" }
            if (reason != null) {
                if (warn) {
                    java.util.logging.Logger.getGlobal().warning(
                        "Skipping dynamic multipart uploads for ${operation.operationId ?: schema.jsonPathFromRoot}: $reason. " +
                            "Fixed parts are retained. Declare uploads as fixed properties, or use plain binary pattern values " +
                            "and the documented ASCII name-pattern subset without additional object constraints.",
                    )
                }
                return null
            }
            val occupied = parameters.map { it.name } + schema.properties.keys + listOf("additionalHeaders", "additionalQueryParameters")
            val parameterName = generateSequence("dynamicParts") { it + "_" }.first { it !in occupied }
            return BinaryMultipartParts(parameterName, declaration.patterns, schema.properties.keys)
        }

        internal fun isSupportedPattern(pattern: String): Boolean {
            // Restrict syntax to an ASCII subset shared by JSON Schema and java.util.regex.
            val atom = "(?:[A-Za-z0-9_.-]|\\\\[.\\-]|\\[[A-Za-z0-9._-]+\\])"
            val quantifier = "(?:[?*+]|\\{[0-9]+(?:,[0-9]*)?\\})?"
            if (!Regex("\\^?(?:$atom$quantifier)+\\$?").matches(pattern)) return false
            return runCatching { Regex(pattern) }.isSuccess
        }
    }
}
