package com.cjbooms.fabrikt.parser

import com.fasterxml.jackson.databind.JsonNode

internal sealed interface SourceSchema {
    val location: String
    val node: JsonNode
}

internal data class SourceBooleanSchema(
    override val location: String,
    override val node: JsonNode,
    val allowsAnyValue: Boolean,
) : SourceSchema

internal data class SourceObjectSchema(
    override val location: String,
    override val node: JsonNode,
    val types: Set<SourceSchemaType>,
    val reference: String?,
    val properties: Map<String, SourceSchema>,
    val items: SourceSchema?,
    val allOf: List<SourceSchema>,
    val anyOf: List<SourceSchema>,
    val oneOf: List<SourceSchema>,
    val not: SourceSchema?,
    val additionalProperties: SourceSchema?,
) : SourceSchema

internal enum class SourceSchemaType(
    val value: String,
) {
    ARRAY("array"),
    BOOLEAN("boolean"),
    INTEGER("integer"),
    NULL("null"),
    NUMBER("number"),
    OBJECT("object"),
    STRING("string"),
    ;

    companion object {
        fun from(value: String): SourceSchemaType? = entries.firstOrNull { it.value == value }
    }
}
