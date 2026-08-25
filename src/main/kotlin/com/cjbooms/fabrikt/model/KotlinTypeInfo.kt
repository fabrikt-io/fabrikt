package com.cjbooms.fabrikt.model

import com.cjbooms.fabrikt.cli.CodeGenTypeOverride
import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.InstantLibrary
import com.cjbooms.fabrikt.cli.SerializationLibrary.KOTLINX_SERIALIZATION
import com.cjbooms.fabrikt.generators.MutableSettings
import com.cjbooms.fabrikt.model.OasType.Companion.toOasType
import com.cjbooms.fabrikt.util.KaizenParserExtensions.getEnumValues
import com.cjbooms.fabrikt.util.KaizenParserExtensions.hasInlinedItemsSchemaOfTypeObject
import com.cjbooms.fabrikt.util.KaizenParserExtensions.hasInlinedItemsSchemaWithOneOf
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isEnumDefinition
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isInlinedDiscriminatedOneOfSuperInterface
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isInlinedObjectDefinition
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isInlinedTypedAdditionalProperties
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isNotDefined
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isOneOfSuperInterface
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isOneOfSuperInterfaceWithDiscriminator
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isSubTypeDeductionEnabled
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isUnsupportedComplexInlinedDefinition
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.reprezen.kaizen.oasparser.model3.Schema
import java.math.BigDecimal
import java.net.URI
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.reflect.KClass

sealed class KotlinTypeInfo(
    val modelKClass: KClass<*>,
    val generatedModelClassName: String? = null,
) {
    object Text : KotlinTypeInfo(String::class)

    object Date : KotlinTypeInfo(LocalDate::class)

    object KotlinxLocalDate : KotlinTypeInfo(kotlinx.datetime.LocalDate::class)

    object DateTime : KotlinTypeInfo(OffsetDateTime::class)

    object Instant : KotlinTypeInfo(java.time.Instant::class)

    object KotlinxInstant : KotlinTypeInfo(kotlinx.datetime.Instant::class)

    object KotlinInstant : KotlinTypeInfo(kotlin.time.Instant::class)

    object LocalDateTime : KotlinTypeInfo(java.time.LocalDateTime::class)

    object Double : KotlinTypeInfo(kotlin.Double::class)

    object Float : KotlinTypeInfo(kotlin.Float::class)

    object Numeric : KotlinTypeInfo(BigDecimal::class)

    object Integer : KotlinTypeInfo(Int::class)

    object BigInt : KotlinTypeInfo(Long::class)

    object Uuid : KotlinTypeInfo(UUID::class)

    object Uri : KotlinTypeInfo(URI::class)

    object ByteArray : KotlinTypeInfo(kotlin.ByteArray::class)

    object InputStream : KotlinTypeInfo(java.io.InputStream::class)

    object Boolean : KotlinTypeInfo(kotlin.Boolean::class)

    object UntypedObject : KotlinTypeInfo(Any::class)

    object AnyType : KotlinTypeInfo(Any::class)

    object JsonElement : KotlinTypeInfo(kotlinx.serialization.json.JsonElement::class)

    object JsonObject : KotlinTypeInfo(kotlinx.serialization.json.JsonObject::class)

    data class Object(
        val simpleClassName: String,
    ) : KotlinTypeInfo(GeneratedType::class, simpleClassName)

    data class Array(
        val parameterizedType: KotlinTypeInfo,
        val isParameterizedTypeNullable: kotlin.Boolean = false,
        val hasUniqueItems: kotlin.Boolean = false,
    ) : KotlinTypeInfo(if (hasUniqueItems) Set::class else List::class)

    data class Map(
        val parameterizedType: KotlinTypeInfo,
    ) : KotlinTypeInfo(Map::class)

    object UnknownAdditionalProperties : KotlinTypeInfo(Any::class)

    object UntypedObjectAdditionalProperties : KotlinTypeInfo(Any::class)

    data class GeneratedTypedAdditionalProperties(
        val simpleClassName: String,
    ) : KotlinTypeInfo(GeneratedType::class, simpleClassName)

    data class SimpleTypedAdditionalProperties(
        val parameterizedType: KotlinTypeInfo,
    ) : KotlinTypeInfo(Map::class)

    data class MapTypeAdditionalProperties(
        val parameterizedType: KotlinTypeInfo,
    ) : KotlinTypeInfo(Map::class)

    data class Enum(
        val entries: List<String>,
        val enumClassName: String,
    ) : KotlinTypeInfo(GeneratedType::class, enumClassName)

    val isComplexType: kotlin.Boolean
        get() =
            when (this) {
                is Array, is Object, is Map, is GeneratedTypedAdditionalProperties -> true
                else -> false
            }

    val isPrimitiveType: kotlin.Boolean
        get() =
            when (this) {
                is Integer, is BigInt, is Double, is Float, is Boolean -> true
                else -> false
            }

    companion object {
        private val logger = Logger.getGlobal()

        fun from(
            schema: Schema,
            oasKey: String = "",
            enclosingSchema: Schema? = null,
        ): KotlinTypeInfo {
            if (schema.isUnsupportedComplexInlinedDefinition()) {
                /*
                 * Defaults to Any for complex schemas inlined under the paths section.
                 * Necessary until support for generating inlined models like these is added.
                 */
                return if (schema.isEnumDefinition()) Text else getOverridableAnyType()
            }
            return when (schema.toOasType(oasKey)) {
                OasType.Date -> {
                    if (MutableSettings.typeOverrides.contains(CodeGenTypeOverride.DATE_AS_STRING)) {
                        Text
                    } else if (MutableSettings.serializationLibrary == KOTLINX_SERIALIZATION) {
                        KotlinxLocalDate
                    } else {
                        Date
                    }
                }

                OasType.DateTime -> {
                    if (MutableSettings.typeOverrides.contains(CodeGenTypeOverride.DATETIME_AS_STRING)) {
                        Text
                    } else if (MutableSettings.typeOverrides.contains(CodeGenTypeOverride.DATETIME_AS_INSTANT)) {
                        Instant
                    } else if (MutableSettings.typeOverrides.contains(CodeGenTypeOverride.DATETIME_AS_LOCALDATETIME)) {
                        LocalDateTime
                    } else if (MutableSettings.serializationLibrary == KOTLINX_SERIALIZATION) {
                        if (MutableSettings.instantLibrary == InstantLibrary.KOTLINX_INSTANT) {
                            KotlinxInstant
                        } else {
                            KotlinInstant
                        }
                    } else {
                        DateTime
                    }
                }

                OasType.Text -> Text
                OasType.Enum ->
                    Enum(schema.getEnumValues(), ModelNameRegistry.getOrRegister(schema, enclosingSchema))

                OasType.Uuid -> {
                    if (MutableSettings.typeOverrides.contains(CodeGenTypeOverride.UUID_AS_STRING)) {
                        Text
                    } else {
                        Uuid
                    }
                }

                OasType.Uri -> {
                    if (MutableSettings.typeOverrides.contains(CodeGenTypeOverride.URI_AS_STRING)) {
                        Text
                    } else {
                        Uri
                    }
                }

                OasType.Base64String -> {
                    if (MutableSettings.typeOverrides.contains(CodeGenTypeOverride.BYTE_AS_STRING)) {
                        Text
                    } else {
                        ByteArray
                    }
                }

                OasType.Binary -> {
                    if (MutableSettings.typeOverrides.contains(CodeGenTypeOverride.BINARY_AS_STRING)) {
                        Text
                    } else {
                        getOverridableByteArray()
                    }
                }

                OasType.Double -> Double
                OasType.Float -> Float
                OasType.Number -> Numeric
                OasType.Int32 -> Integer
                OasType.Int64 -> BigInt
                OasType.Integer -> Integer
                OasType.Boolean -> Boolean
                OasType.Set -> {
                    val parameterizedType = getParameterizedTypeForArray(schema, enclosingSchema, oasKey)
                    Array(parameterizedType, schema.itemsSchema.isNullable, true)
                }

                OasType.Array -> {
                    val parameterizedType = getParameterizedTypeForArray(schema, enclosingSchema, oasKey)
                    Array(parameterizedType, schema.itemsSchema.isNullable, schema.itemsSchema.isUniqueItems)
                }

                OasType.Object -> Object(ModelNameRegistry.getOrRegister(schema, enclosingSchema))
                OasType.Map ->
                    Map(from(schema.additionalPropertiesSchema, OasType.ADDITIONAL_PROPERTIES_VALUE, enclosingSchema))

                OasType.TypedObjectAdditionalProperties ->
                    GeneratedTypedAdditionalProperties(
                        ModelNameRegistry.getOrRegister(schema, valueSuffix = schema.isInlinedTypedAdditionalProperties()),
                    )

                OasType.SimpleTypedAdditionalProperties ->
                    SimpleTypedAdditionalProperties(from(schema, OasType.ADDITIONAL_PROPERTIES_VALUE))

                OasType.UntypedObjectAdditionalProperties -> UntypedObjectAdditionalProperties
                OasType.UntypedObject -> if (isAnyAsJsonElementActive()) JsonObject else UntypedObject
                OasType.UnknownAdditionalProperties -> UnknownAdditionalProperties
                OasType.TypedMapAdditionalProperties ->
                    MapTypeAdditionalProperties(
                        from(schema.additionalPropertiesSchema, "", enclosingSchema),
                    )

                OasType.Any -> getOverridableAnyType()
                OasType.OneOfAny ->
                    if (schema.isOneOfSuperInterfaceWithDiscriminator() ||
                        (
                            schema.isOneOfSuperInterface() &&
                                schema.isSubTypeDeductionEnabled() &&
                                MutableSettings.serializationLibrary != KOTLINX_SERIALIZATION
                        )
                    ) {
                        Object(ModelNameRegistry.getOrRegister(schema, enclosingSchema))
                    } else {
                        getOverridableAnyType()
                    }
            }
        }

        private fun getParameterizedTypeForArray(
            arraySchema: Schema,
            enclosingSchema: Schema?,
            oasKey: String,
        ): KotlinTypeInfo {
            val itemsSchema = arraySchema.itemsSchema
            return when {
                (
                    itemsSchema.isInlinedObjectDefinition() ||
                        itemsSchema.isInlinedDiscriminatedOneOfSuperInterface()
                ) &&
                    !itemsSchema.isUnsupportedComplexInlinedDefinition() ->
                    Object(ModelNameRegistry.getOrRegister(arraySchema, enclosingSchema))
                arraySchema.hasInlinedItemsSchemaWithOneOf() || arraySchema.hasInlinedItemsSchemaOfTypeObject() ->
                    Object(ModelNameRegistry.getOrRegister(arraySchema))
                arraySchema.itemsSchema.isNotDefined() -> getOverridableAnyType()
                else -> from(arraySchema.itemsSchema, oasKey, enclosingSchema)
            }
        }

        private fun getOverridableAnyType(): KotlinTypeInfo = if (isAnyAsJsonElementActive()) JsonElement else AnyType

        /**
         * When the ANY_AS_JSONELEMENT override is active, untyped map values (additional properties
         * without a schema) are represented as [JsonElement] instead of `Any`. Returns null when the
         * override is not active, so callers keep their existing `Any`-based type.
         */
        fun anyAsJsonElementMapValueOverride(): KotlinTypeInfo? = if (isAnyAsJsonElementActive()) JsonElement else null

        private fun isAnyAsJsonElementActive(): kotlin.Boolean =
            when {
                CodeGenTypeOverride.ANY_AS_JSONELEMENT !in MutableSettings.typeOverrides -> false
                MutableSettings.serializationLibrary == KOTLINX_SERIALIZATION -> true
                else -> {
                    logger.log(
                        Level.WARNING,
                        """
                        The override flag 'ANY_AS_JSONELEMENT' requires the KOTLINX_SERIALIZATION serialization
                        library and is ignored. Defaulting to `Any`...
                        """.trimIndent(),
                    )
                    false
                }
            }

        private fun getOverridableByteArray(): KotlinTypeInfo {
            val types: Set<CodeGenerationType> = MutableSettings.generationTypes
            val typeOverrides = MutableSettings.typeOverrides
            return when {
                CodeGenerationType.CLIENT in types && CodeGenTypeOverride.BYTEARRAY_AS_INPUTSTREAM in typeOverrides -> {
                    logger.log(
                        Level.WARNING,
                        """
                        Client code generation does not support streaming, yet. The override flag 
                        'BYTEARRAY_AS_INPUTSTREAM' is ignored. If generating server side code, please consider 
                        splitting the client & server in different fabrikt executions. Defaulting to `ByteArray`...
                        """.trimIndent(),
                    )
                    ByteArray
                }

                CodeGenTypeOverride.BYTEARRAY_AS_INPUTSTREAM in typeOverrides -> InputStream
                else -> ByteArray
            }
        }
    }
}
