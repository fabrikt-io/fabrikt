package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.model.KotlinTypeInfo
import com.cjbooms.fabrikt.util.NormalisedString.toEnumName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeName
import java.math.BigDecimal
import java.net.URI
import java.util.Base64
import java.util.logging.Logger

sealed class OasDefault {
    abstract fun getDefault(): CodeBlock

    data class StringValue(
        val strValue: String,
    ) : OasDefault() {
        override fun getDefault(): CodeBlock = CodeBlock.of("%S", strValue)
    }

    data class BigDecimalValue(
        val decimalValue: Number,
    ) : OasDefault() {
        override fun getDefault(): CodeBlock = CodeBlock.of("%T($decimalValue)", BigDecimal::class)
    }

    data class NumberValue(
        val numericValue: Number,
        val typeInfo: KotlinTypeInfo,
    ) : OasDefault() {
        override fun getDefault(): CodeBlock = CodeBlock.of("%L", "$numericValue$suffix")

        private val suffix: String =
            when (typeInfo) {
                is KotlinTypeInfo.Float -> "f"
                is KotlinTypeInfo.Double ->
                    if (!numericValue.toString().contains(".") && !numericValue.toString().contains("e")) {
                        ".0"
                    } else {
                        ""
                    }

                else -> ""
            }
    }

    data class UriValue(
        val strValue: String,
    ) : OasDefault() {
        override fun getDefault(): CodeBlock = CodeBlock.of("%T(\"$strValue\")", URI::class)
    }

    data class ByteValue(
        val base64String: String,
    ) : OasDefault() {
        override fun getDefault(): CodeBlock = CodeBlock.of("%T.getDecoder().decode(\"$base64String\")", Base64::class)
    }

    data class BooleanValue(
        val boolValue: Boolean,
    ) : OasDefault() {
        override fun getDefault(): CodeBlock = CodeBlock.of("%L", boolValue)
    }

    data class EnumValue(
        val type: TypeName,
        val enumValue: String,
    ) : OasDefault() {
        override fun getDefault(): CodeBlock = CodeBlock.of("%T.${enumValue.toEnumName()}", type)
    }

    data class JsonNullableValue(
        val inner: OasDefault,
    ) : OasDefault() {
        override fun getDefault(): CodeBlock =
            CodeBlock.of(
                "%T.of(${inner.getDefault()})",
                ClassName(
                    "org.openapitools.jackson.nullable",
                    "JsonNullable",
                ),
            )
    }

    companion object {
        fun isCompatible(
            typeInfo: KotlinTypeInfo,
            value: Any,
        ): Boolean =
            when (typeInfo) {
                is KotlinTypeInfo.Enum -> value is String && value in typeInfo.entries
                KotlinTypeInfo.Text, KotlinTypeInfo.Uri, KotlinTypeInfo.ByteArray -> value is String
                KotlinTypeInfo.Boolean -> value is Boolean
                KotlinTypeInfo.Integer -> value is Int
                KotlinTypeInfo.BigInt -> value is Int || value is Long
                KotlinTypeInfo.Numeric -> value is Number && value.toString().toBigDecimalOrNull() != null
                KotlinTypeInfo.Float -> value is Number && value.toFloat().isFinite()
                KotlinTypeInfo.Double -> value is Number && value.toDouble().isFinite()
                else -> false
            }

        fun from(
            typeInfo: KotlinTypeInfo,
            type: TypeName?,
            default: Any,
        ): OasDefault? {
            if (!isCompatible(typeInfo, default)) {
                Logger.getGlobal().warning(
                    "Ignoring default incompatible with generated type '${type ?: typeInfo.modelKClass.qualifiedName}'. " +
                        "Use a default supported by the generated Kotlin type or remove it.",
                )
                return null
            }
            return when (default) {
                is String -> {
                    when (typeInfo) {
                        is KotlinTypeInfo.Enum -> {
                            if (type != null) {
                                OasDefault.EnumValue(type, default)
                            } else {
                                null
                            }
                        }

                        is KotlinTypeInfo.Text -> OasDefault.StringValue(default)
                        is KotlinTypeInfo.Uri -> OasDefault.UriValue(default)
                        is KotlinTypeInfo.ByteArray -> OasDefault.ByteValue(default)
                        else -> null
                    }
                }

                is Number ->
                    when (typeInfo) {
                        is KotlinTypeInfo.Numeric -> OasDefault.BigDecimalValue(default)
                        else -> OasDefault.NumberValue(default, typeInfo)
                    }

                is Boolean -> OasDefault.BooleanValue(default)
                else -> null
            }
        }
    }
}
