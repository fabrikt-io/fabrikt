package com.cjbooms.fabrikt.model

import com.cjbooms.fabrikt.util.KaizenParserExtensions.isEnumDefinition
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isMapTypeAdditionalProperties
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isOpenEnumDefinition
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isSchemaLess
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isSet
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isSimpleMapDefinition
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isSimpleOneOfAnyDefinition
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isSimpleTypedAdditionalProperties
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isStringDefinitionWithFormat
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isTypedAdditionalProperties
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isUnknownAdditionalProperties
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isUntypedAdditionalProperties
import com.cjbooms.fabrikt.util.KaizenParserExtensions.printPathFromRoot
import com.cjbooms.fabrikt.util.KaizenParserExtensions.safeType
import com.reprezen.kaizen.oasparser.model3.Schema
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

sealed class OasType(
    val type: String?,
    val format: String? = null,
    val specialization: Specialization = Specialization.NONE,
) {
    object Any : OasType(null)
    object OneOfAny : OasType(WILD_CARD_TYPE, specialization = Specialization.ONE_OF_ANY)
    object Boolean : OasType("boolean")
    object Date : OasType("string", "date")
    object DateTime : OasType("string", "date-time")
    object Text : OasType("string")
    object Float : OasType("number", "float")
    object Double : OasType("number", "double")
    object Number : OasType("number")
    object Int32 : OasType("integer", "int32")
    object Int64 : OasType("integer", "int64")
    object Integer : OasType("integer")
    object Object : OasType("object")
    object Array : OasType("array")
    object Set : OasType("array", specialization = Specialization.SET)
    object UntypedObject : OasType("object", specialization = Specialization.UNTYPED_OBJECT)
    object Enum : OasType("string", specialization = Specialization.ENUM)
    object Uuid : OasType("string", specialization = Specialization.UUID)
    object Uri : OasType("string", specialization = Specialization.URI)
    object Base64String : OasType("string", specialization = Specialization.BYTE)
    object Binary : OasType("string", specialization = Specialization.BINARY)
    object Map : OasType("object", specialization = Specialization.MAP)
    object UnknownAdditionalProperties :
        OasType("object", specialization = Specialization.UNKNOWN_ADDITIONAL_PROPERTIES)

    object UntypedObjectAdditionalProperties :
        OasType("object", specialization = Specialization.UNTYPED_OBJECT_ADDITIONAL_PROPERTIES)

    object TypedObjectAdditionalProperties :
        OasType("object", specialization = Specialization.TYPED_OBJECT_ADDITIONAL_PROPERTIES)
    object SimpleTypedAdditionalProperties :
        OasType(WILD_CARD_TYPE, specialization = Specialization.SIMPLE_TYPED_ADDITIONAL_PROPERTIES)

    object TypedMapAdditionalProperties :
        OasType("object", specialization = Specialization.TYPED_MAP_ADDITIONAL_PROPERTIES)

    companion object {
        private const val WILD_CARD_TYPE: String = "wildcard"
        const val ADDITIONAL_PROPERTIES_VALUE: String = "additionalPropertiesValue"
        fun Schema.toOasType(oasKey: String): OasType =
            values(OasType::class)
                .filter {
                    it.type == safeType() ||
                        it.type == WILD_CARD_TYPE &&
                        listOf(
                            Specialization.ONE_OF_ANY,
                            Specialization.SIMPLE_TYPED_ADDITIONAL_PROPERTIES
                        ).contains(getSpecialization(oasKey))
                }
                .filter { it.specialization == getSpecialization(oasKey) }
                .filter { it.format == format || it.format == null }
                .let { candidates ->
                    if (candidates.size > 1) candidates.find { it.format == format }
                    else candidates.firstOrNull()
                } ?: throw IllegalStateException(
                "Unknown OAS type: ${safeType()} and format: $format and specialization: ${getSpecialization(oasKey)} for path: ${this.printPathFromRoot()}"
            )

        private fun values(clazz: KClass<OasType>) =
            clazz.nestedClasses.filter { it.isFinal && it.isSubclassOf(clazz) }
                .map { it.objectInstance as OasType }

        private fun Schema.getSpecialization(oasKey: String): Specialization =
            when {
                isStringDefinitionWithFormat("uuid") -> Specialization.UUID
                isStringDefinitionWithFormat("uri") -> Specialization.URI
                isStringDefinitionWithFormat("byte") -> Specialization.BYTE
                isStringDefinitionWithFormat("binary") -> Specialization.BINARY
                isEnumDefinition() || isOpenEnumDefinition() -> Specialization.ENUM
                isMapTypeAdditionalProperties(oasKey) -> Specialization.TYPED_MAP_ADDITIONAL_PROPERTIES
                isSimpleMapDefinition() -> Specialization.MAP
                isTypedAdditionalProperties(oasKey) -> Specialization.TYPED_OBJECT_ADDITIONAL_PROPERTIES
                isUntypedAdditionalProperties(oasKey) -> Specialization.UNTYPED_OBJECT_ADDITIONAL_PROPERTIES
                isUnknownAdditionalProperties(oasKey) -> Specialization.UNKNOWN_ADDITIONAL_PROPERTIES
                isSimpleOneOfAnyDefinition() -> Specialization.ONE_OF_ANY
                isSchemaLess() -> Specialization.UNTYPED_OBJECT
                isSet() -> Specialization.SET
                oasKey != ADDITIONAL_PROPERTIES_VALUE && isSimpleTypedAdditionalProperties(oasKey) ->
                    Specialization.SIMPLE_TYPED_ADDITIONAL_PROPERTIES
                else -> Specialization.NONE
            }
    }

    enum class Specialization {
        ENUM,
        MAP,
        UNKNOWN_ADDITIONAL_PROPERTIES,
        TYPED_OBJECT_ADDITIONAL_PROPERTIES,
        SIMPLE_TYPED_ADDITIONAL_PROPERTIES,
        TYPED_MAP_ADDITIONAL_PROPERTIES,
        UNTYPED_OBJECT_ADDITIONAL_PROPERTIES,
        UNTYPED_OBJECT,
        UUID,
        URI,
        NONE,
        ONE_OF_ANY,
        BYTE,
        BINARY,
        SET
    }
}
