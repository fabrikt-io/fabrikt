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

internal sealed interface SourceSchemaType {
    val value: String

    enum class Recognised(
        override val value: String,
    ) : SourceSchemaType {
        ARRAY("array"),
        BOOLEAN("boolean"),
        INTEGER("integer"),
        NULL("null"),
        NUMBER("number"),
        OBJECT("object"),
        STRING("string"),
    }

    data class Unrecognised(
        override val value: String,
    ) : SourceSchemaType

    companion object {
        val ARRAY: SourceSchemaType = Recognised.ARRAY
        val BOOLEAN: SourceSchemaType = Recognised.BOOLEAN
        val INTEGER: SourceSchemaType = Recognised.INTEGER
        val NULL: SourceSchemaType = Recognised.NULL
        val NUMBER: SourceSchemaType = Recognised.NUMBER
        val OBJECT: SourceSchemaType = Recognised.OBJECT
        val STRING: SourceSchemaType = Recognised.STRING

        fun from(value: String): SourceSchemaType = Recognised.entries.firstOrNull { it.value == value } ?: Unrecognised(value)
    }
}
