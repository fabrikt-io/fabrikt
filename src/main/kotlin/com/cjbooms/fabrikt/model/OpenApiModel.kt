package com.cjbooms.fabrikt.model

import com.fasterxml.jackson.databind.JsonNode
import com.reprezen.jsonoverlay.Overlay
import com.reprezen.kaizen.oasparser.model3.Discriminator
import com.reprezen.kaizen.oasparser.model3.MediaType
import com.reprezen.kaizen.oasparser.model3.OpenApi3
import com.reprezen.kaizen.oasparser.model3.Operation
import com.reprezen.kaizen.oasparser.model3.Parameter
import com.reprezen.kaizen.oasparser.model3.Path
import com.reprezen.kaizen.oasparser.model3.RequestBody
import com.reprezen.kaizen.oasparser.model3.Response
import com.reprezen.kaizen.oasparser.model3.Schema
import com.reprezen.kaizen.oasparser.model3.SecurityRequirement
import com.reprezen.kaizen.oasparser.model3.Server

/**
 * Fabrikt-owned facade over the Kaizen parser model.
 *
 * Each delegate wraps a Kaizen type in an [internal] [kaizen] reference and exposes the
 * same member names generation uses. The [kaizen] back-reference is the seam `Overlay.of(...)`
 * uses; the delegate classes are the seam a future parser swap re-points.
 */
class OpenApiSchema(
    internal val kaizen: Schema,
) {
    val name: String? get() = kaizen.name
    val type: String? get() = kaizen.type
    val format: String? get() = kaizen.format
    val properties: Map<String, OpenApiSchema>
        get() = kaizen.properties?.mapValues { OpenApiSchema(it.value) } ?: emptyMap()
    val requiredFields: List<String> get() = kaizen.requiredFields ?: emptyList()
    val allOfSchemas: List<OpenApiSchema>
        get() = kaizen.allOfSchemas?.map(::OpenApiSchema) ?: emptyList()
    val anyOfSchemas: List<OpenApiSchema>
        get() = kaizen.anyOfSchemas?.map(::OpenApiSchema) ?: emptyList()
    val oneOfSchemas: List<OpenApiSchema>
        get() = kaizen.oneOfSchemas?.map(::OpenApiSchema) ?: emptyList()
    val itemsSchema: OpenApiSchema get() = OpenApiSchema(kaizen.itemsSchema)
    val additionalProperties: Boolean? get() = kaizen.additionalProperties
    val additionalPropertiesSchema: OpenApiSchema get() = OpenApiSchema(kaizen.additionalPropertiesSchema)
    val discriminator: OpenApiDiscriminator get() = OpenApiDiscriminator(kaizen.discriminator)
    val default: Any? get() = kaizen.default
    val extensions: Map<String, Any> get() = kaizen.extensions ?: emptyMap()
    val enums: List<Any?> get() = kaizen.enums ?: emptyList()
    val isNullable: Boolean get() = kaizen.isNullable ?: false
    val isReadOnly: Boolean get() = kaizen.isReadOnly ?: false
    val isWriteOnly: Boolean get() = kaizen.isWriteOnly ?: false
    val isDeprecated: Boolean get() = kaizen.isDeprecated ?: false
    val isUniqueItems: Boolean get() = kaizen.isUniqueItems ?: false
    val minimum: Number? get() = kaizen.minimum
    val maximum: Number? get() = kaizen.maximum
    val isExclusiveMinimum: Boolean get() = kaizen.isExclusiveMinimum ?: false
    val isExclusiveMaximum: Boolean get() = kaizen.isExclusiveMaximum ?: false
    val minLength: Int? get() = kaizen.minLength
    val maxLength: Int? get() = kaizen.maxLength
    val pattern: String? get() = kaizen.pattern
    val minItems: Int? get() = kaizen.minItems
    val maxItems: Int? get() = kaizen.maxItems
    val minProperties: Int? get() = kaizen.minProperties
    val maxProperties: Int? get() = kaizen.maxProperties
    val example: Any? get() = kaizen.example
    val title: String? get() = kaizen.title
    val description: String? get() = kaizen.description
    val jsonPathFromRoot: String get() = Overlay.of(kaizen).pathFromRoot
    val jsonPathInParent: String? get() = Overlay.of(kaizen).pathInParent
    val jsonReference: String get() = Overlay.of(kaizen).jsonReference
    val isPresent: Boolean get() = Overlay.of(kaizen).isPresent
    val documentUrl: String?
        get() =
            Overlay
                .of(kaizen)
                .positionInfo
                ?.orElse(null)
                ?.documentUrl
    val parsedJson: JsonNode? get() = Overlay.of(kaizen).parsedJson

    fun hasEnums(): Boolean = kaizen.hasEnums()

    fun hasAllOfSchemas(): Boolean = kaizen.hasAllOfSchemas()

    fun hasOneOfSchemas(): Boolean = kaizen.hasOneOfSchemas()

    fun hasAnyOfSchemas(): Boolean = kaizen.hasAnyOfSchemas()

    override fun equals(other: Any?): Boolean = other is OpenApiSchema && kaizen == other.kaizen

    override fun hashCode(): Int = kaizen.hashCode()

    override fun toString(): String = kaizen.toString()
}

class OpenApi3Document(
    internal val kaizen: OpenApi3,
) {
    val schemas: Map<String, OpenApiSchema>
        get() = kaizen.schemas?.mapValues { OpenApiSchema(it.value) } ?: emptyMap()
    val paths: Map<String, OpenApiPath>
        get() = kaizen.paths?.mapValues { OpenApiPath(it.value) } ?: emptyMap()
    val parameters: Map<String, OpenApiParameter>
        get() = kaizen.parameters?.mapValues { OpenApiParameter(it.value) } ?: emptyMap()
    val requestBodies: Map<String, OpenApiRequestBody>
        get() = kaizen.requestBodies?.mapValues { OpenApiRequestBody(it.value) } ?: emptyMap()
    val responses: Map<String, OpenApiResponse>
        get() = kaizen.responses?.mapValues { OpenApiResponse(it.value) } ?: emptyMap()
    val servers: List<OpenApiServer> get() = kaizen.servers?.map(::OpenApiServer) ?: emptyList()
    val securityRequirements: List<OpenApiSecurityRequirement>
        get() = kaizen.securityRequirements?.map(::OpenApiSecurityRequirement) ?: emptyList()
    val parsedJson: JsonNode? get() = Overlay.of(kaizen).parsedJson

    override fun equals(other: Any?): Boolean = other is OpenApi3Document && kaizen == other.kaizen

    override fun hashCode(): Int = kaizen.hashCode()

    override fun toString(): String = kaizen.toString()
}

class OpenApiOperation(
    internal val kaizen: Operation,
) {
    val parameters: List<OpenApiParameter>
        get() = kaizen.parameters?.map(::OpenApiParameter) ?: emptyList()
    val responses: Map<String, OpenApiResponse>
        get() = kaizen.responses?.mapValues { OpenApiResponse(it.value) } ?: emptyMap()
    val operationId: String? get() = kaizen.operationId
    val tags: List<String> get() = kaizen.tags ?: emptyList()
    val summary: String? get() = kaizen.summary
    val description: String? get() = kaizen.description
    val requestBody: OpenApiRequestBody get() = OpenApiRequestBody(kaizen.requestBody)
    val securityRequirements: List<OpenApiSecurityRequirement>
        get() = kaizen.securityRequirements?.map(::OpenApiSecurityRequirement) ?: emptyList()
    val extensions: Map<String, Any> get() = kaizen.extensions ?: emptyMap()

    fun hasSecurityRequirements(): Boolean = kaizen.hasSecurityRequirements()

    override fun equals(other: Any?): Boolean = other is OpenApiOperation && kaizen == other.kaizen

    override fun hashCode(): Int = kaizen.hashCode()

    override fun toString(): String = kaizen.toString()
}

class OpenApiPath(
    internal val kaizen: Path,
) {
    val parameters: List<OpenApiParameter>
        get() = kaizen.parameters?.map(::OpenApiParameter) ?: emptyList()
    val operations: Map<String, OpenApiOperation>
        get() = kaizen.operations?.mapValues { OpenApiOperation(it.value) } ?: emptyMap()
    val pathString: String get() = kaizen.pathString

    override fun equals(other: Any?): Boolean = other is OpenApiPath && kaizen == other.kaizen

    override fun hashCode(): Int = kaizen.hashCode()

    override fun toString(): String = kaizen.toString()
}

class OpenApiParameter(
    internal val kaizen: Parameter,
) {
    val name: String get() = kaizen.name
    val `in`: String get() = kaizen.`in`
    val schema: OpenApiSchema get() = OpenApiSchema(kaizen.schema)
    val isRequired: Boolean get() = kaizen.isRequired ?: false
    val description: String? get() = kaizen.description
    val explode: Boolean? get() = kaizen.explode

    override fun equals(other: Any?): Boolean = other is OpenApiParameter && kaizen == other.kaizen

    override fun hashCode(): Int = kaizen.hashCode()

    override fun toString(): String = kaizen.toString()
}

class OpenApiResponse(
    internal val kaizen: Response,
) {
    val contentMediaTypes: Map<String, OpenApiMediaType>
        get() = kaizen.contentMediaTypes?.mapValues { OpenApiMediaType(it.value) } ?: emptyMap()
    val description: String? get() = kaizen.description

    fun hasContentMediaTypes(): Boolean = kaizen.hasContentMediaTypes()

    override fun equals(other: Any?): Boolean = other is OpenApiResponse && kaizen == other.kaizen

    override fun hashCode(): Int = kaizen.hashCode()

    override fun toString(): String = kaizen.toString()
}

class OpenApiRequestBody(
    internal val kaizen: RequestBody,
) {
    val contentMediaTypes: Map<String, OpenApiMediaType>
        get() = kaizen.contentMediaTypes?.mapValues { OpenApiMediaType(it.value) } ?: emptyMap()
    val description: String? get() = kaizen.description
    val isRequired: Boolean get() = kaizen.isRequired ?: false

    override fun equals(other: Any?): Boolean = other is OpenApiRequestBody && kaizen == other.kaizen

    override fun hashCode(): Int = kaizen.hashCode()

    override fun toString(): String = kaizen.toString()
}

class OpenApiMediaType(
    internal val kaizen: MediaType,
) {
    val schema: OpenApiSchema get() = OpenApiSchema(kaizen.schema)

    override fun equals(other: Any?): Boolean = other is OpenApiMediaType && kaizen == other.kaizen

    override fun hashCode(): Int = kaizen.hashCode()

    override fun toString(): String = kaizen.toString()
}

class OpenApiDiscriminator(
    internal val kaizen: Discriminator,
) {
    val propertyName: String? get() = kaizen.propertyName
    val mappings: Map<String, String> get() = kaizen.mappings ?: emptyMap()

    override fun equals(other: Any?): Boolean = other is OpenApiDiscriminator && kaizen == other.kaizen

    override fun hashCode(): Int = kaizen.hashCode()

    override fun toString(): String = kaizen.toString()
}

class OpenApiSecurityRequirement(
    internal val kaizen: SecurityRequirement,
) {
    val requirements: Map<String, List<String>>
        get() = kaizen.requirements?.mapValues { it.value?.parameters ?: emptyList() } ?: emptyMap()

    override fun equals(other: Any?): Boolean = other is OpenApiSecurityRequirement && kaizen == other.kaizen

    override fun hashCode(): Int = kaizen.hashCode()

    override fun toString(): String = kaizen.toString()
}

class OpenApiServer(
    internal val kaizen: Server,
) {
    val url: String? get() = kaizen.url

    override fun equals(other: Any?): Boolean = other is OpenApiServer && kaizen == other.kaizen

    override fun hashCode(): Int = kaizen.hashCode()

    override fun toString(): String = kaizen.toString()
}
