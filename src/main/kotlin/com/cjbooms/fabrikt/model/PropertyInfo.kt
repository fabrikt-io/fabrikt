package com.cjbooms.fabrikt.model

import com.cjbooms.fabrikt.util.KaizenParserExtensions.getKeyIfSingleDiscriminatorValue
import com.cjbooms.fabrikt.util.KaizenParserExtensions.hasAdditionalProperties
import com.cjbooms.fabrikt.util.KaizenParserExtensions.hasNoDiscriminator
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isDiscriminatorProperty
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isInLinedObjectUnderAllOf
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isInlinedArrayDefinition
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isInlinedDiscriminatedOneOfSuperInterface
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isInlinedEnumDefinition
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isInlinedObjectDefinition
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isRequired
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isSchemaLess
import com.cjbooms.fabrikt.util.KaizenParserExtensions.isSimpleMapDefinition
import com.cjbooms.fabrikt.util.KaizenParserExtensions.safeName
import com.cjbooms.fabrikt.util.KaizenParserExtensions.safeType
import com.cjbooms.fabrikt.util.NormalisedString.camelCase
import com.cjbooms.fabrikt.util.NormalisedString.toEnumName
import com.reprezen.kaizen.oasparser.model3.OpenApi3
import com.reprezen.kaizen.oasparser.model3.Schema

sealed class PropertyInfo {
    abstract val oasKey: String
    abstract val typeInfo: KotlinTypeInfo
    abstract val schema: Schema
    open val isRequired: Boolean = false
    open val isInherited: Boolean = false

    val name: String
        get() = oasKey.camelCase()

    companion object {

        data class Settings(
            val markAsInherited: Boolean = false,
            val markReadWriteOnlyOptional: Boolean = true,
            val markAllOptional: Boolean = false,
            val excludeWriteOnly: Boolean = false
        )

        val HTTP_SETTINGS = Settings()

        fun Schema.topLevelProperties(
            settings: Settings,
            api: OpenApi3,
            enclosingSchema: Schema? = null,
            additionalRequiredFields: Collection<String> = emptySet()
        ): Collection<PropertyInfo> {
            val combinedRequiredFields = this.requiredFields + additionalRequiredFields
            
            val results = mutableListOf<PropertyInfo>() +
                allOfSchemas.flatMap {
                    it.topLevelProperties(
                        settings = maybeMarkInherited(settings, enclosingSchema, it),
                        api = api,
                        enclosingSchema = if (this.isInlinedObjectDefinition()) enclosingSchema else this,
                        additionalRequiredFields = combinedRequiredFields
                    )
                } +
                (if (oneOfSchemas.isEmpty()) emptyList() else listOf(OneOfAny(oneOfSchemas.first()))) +
                anyOfSchemas.flatMap {
                    it.topLevelProperties(
                        settings = settings.copy(markAllOptional = true),
                        api = api,
                        enclosingSchema = if (this.isInlinedObjectDefinition()) enclosingSchema else this
                    )
                } +
                getInLinedProperties(settings, api, enclosingSchema, combinedRequiredFields)
            return results.distinctBy { it.oasKey }
        }

        private fun maybeMarkInherited(settings: Settings, enclosingSchema: Schema?, it: Schema): Settings {
            val isInherited = when {
                it.safeName() == enclosingSchema?.name -> false
                it.hasNoDiscriminator() -> settings.markAsInherited
                it.isInLinedObjectUnderAllOf() && it.hasNoDiscriminator() -> settings.markAsInherited
                else -> true
            }
            return settings.copy(markAsInherited = isInherited)
        }

        private fun Schema.getInLinedProperties(
            settings: Settings,
            api: OpenApi3,
            enclosingSchema: Schema? = null,
            additionalRequiredFields: Collection<String> = emptySet()
        ): Collection<PropertyInfo> {
            val mainProperties: List<PropertyInfo> = properties.map { property ->
                when (property.value.safeType()) {
                    OasType.Set.type ->
                        ListField(
                            isRequired = isRequired(
                                api, property, settings.markReadWriteOnlyOptional, settings.markAllOptional, additionalRequiredFields = additionalRequiredFields
                            ),
                            oasKey = property.key,
                            schema = property.value,
                            isInherited = settings.markAsInherited,
                            parentSchema = this,
                            enclosingSchema = enclosingSchema,
                            hasUniqueItems = property.value.isUniqueItems
                        )

                    OasType.Array.type ->
                        ListField(
                            isRequired = isRequired(
                                api, property, settings.markReadWriteOnlyOptional, settings.markAllOptional, additionalRequiredFields = additionalRequiredFields
                            ),
                            oasKey = property.key,
                            schema = property.value,
                            isInherited = settings.markAsInherited,
                            parentSchema = this,
                            enclosingSchema = enclosingSchema,
                            hasUniqueItems = property.value.isUniqueItems
                        )

                    OasType.Object.type ->
                        if (property.value.isSimpleMapDefinition() || property.value.isSchemaLess())
                            MapField(
                                isRequired = isRequired(
                                    api, property, settings.markReadWriteOnlyOptional, settings.markAllOptional, additionalRequiredFields = additionalRequiredFields
                                ),
                                oasKey = property.key,
                                schema = property.value,
                                isInherited = settings.markAsInherited,
                                parentSchema = this
                            )
                        else if (property.value.isInlinedObjectDefinition())
                            ObjectInlinedField(
                                isRequired = isRequired(
                                    api, property, settings.markReadWriteOnlyOptional, settings.markAllOptional, additionalRequiredFields = additionalRequiredFields
                                ),
                                oasKey = property.key,
                                schema = property.value,
                                isInherited = settings.markAsInherited,
                                parentSchema = this,
                                enclosingSchema = enclosingSchema
                            )
                        else
                            ObjectRefField(
                                isRequired = isRequired(
                                    api, property, settings.markReadWriteOnlyOptional, settings.markAllOptional, additionalRequiredFields = additionalRequiredFields
                                ),
                                oasKey = property.key,
                                schema = property.value,
                                isInherited = settings.markAsInherited,
                                parentSchema = this
                            )

                    else ->
                        if (property.value.isWriteOnly && settings.excludeWriteOnly) {
                            null
                        } else {
                            Field(
                                isRequired = isRequired(
                                    api, property, settings.markReadWriteOnlyOptional, settings.markAllOptional, additionalRequiredFields = additionalRequiredFields
                                ),
                                oasKey = property.key,
                                schema = property.value,
                                isInherited = settings.markAsInherited,
                                isPolymorphicDiscriminator = isDiscriminatorProperty(api, property),
                                maybeDiscriminator = enclosingSchema?.let {
                                    this.getKeyIfSingleDiscriminatorValue(api, property, it)
                                },
                                enclosingSchema = if (property.value.isInlinedEnumDefinition()) this else null
                            )
                        }
                }
            }.filterNotNull()

            return if (hasAdditionalProperties())
                mainProperties
                    .plus(
                        AdditionalProperties(additionalPropertiesSchema, settings.markAsInherited, this)
                    )
            else mainProperties
        }
    }

    sealed class DiscriminatorKey(val stringValue: String, val modelName: String) {
        class StringKey(value: String, modelName: String) : DiscriminatorKey(value, modelName)
        class EnumKey(value: String, modelName: String) : DiscriminatorKey(value, modelName) {
            val enumKey = value.toEnumName()
        }
    }

    data class Field(
        override val isRequired: Boolean,
        override val oasKey: String,
        override val schema: Schema,
        override val isInherited: Boolean,
        val isPolymorphicDiscriminator: Boolean,
        val maybeDiscriminator: Map<String, DiscriminatorKey>?,
        val enclosingSchema: Schema? = null
    ) : PropertyInfo() {
        override val typeInfo: KotlinTypeInfo =
            KotlinTypeInfo.from(schema, oasKey, enclosingSchema)
        val pattern: String? = schema.safeField(Schema::getPattern)
        val maxLength: Int? = schema.safeField(Schema::getMaxLength)
        val minLength: Int? = schema.safeField(Schema::getMinLength)
        val minimum: Number? = schema.safeField(Schema::getMinimum)
        val exclusiveMinimum: Boolean? = schema.safeField(Schema::getExclusiveMinimum)
        val maximum: Number? = schema.safeField(Schema::getMaximum)
        val exclusiveMaximum: Boolean? = schema.safeField(Schema::getExclusiveMaximum)

        private fun <T> Schema.safeField(getField: Schema.() -> T?): T? = this.getField()
    }

    interface CollectionValidation {
        val minItems: Int?
        val maxItems: Int?
    }

    data class ListField(
        override val isRequired: Boolean,
        override val oasKey: String,
        override val schema: Schema,
        override val isInherited: Boolean,
        val parentSchema: Schema,
        val enclosingSchema: Schema?,
        val hasUniqueItems: Boolean,
    ) : PropertyInfo(), CollectionValidation {
        override val typeInfo: KotlinTypeInfo =
            if (isInherited) {
                KotlinTypeInfo.from(schema, oasKey, parentSchema.takeIf { isInlined() })
            } else {
                KotlinTypeInfo.from(schema, oasKey, enclosingSchema.takeIf { isInlined() })
            }
        override val minItems: Int? = schema.minItems
        override val maxItems: Int? = schema.maxItems

        private fun isInlined(): Boolean =
            schema
                .itemsSchema
                .let { it.isInlinedObjectDefinition() || it.isInlinedEnumDefinition() || it.isInlinedArrayDefinition() || it.isInlinedDiscriminatedOneOfSuperInterface() }
    }

    data class MapField(
        override val isRequired: Boolean,
        override val oasKey: String,
        override val schema: Schema,
        override val isInherited: Boolean,
        val parentSchema: Schema
    ) : PropertyInfo() {
        override val typeInfo: KotlinTypeInfo = KotlinTypeInfo.from(schema, oasKey)
    }

    data class ObjectRefField(
        override val isRequired: Boolean,
        override val oasKey: String,
        override val schema: Schema,
        override val isInherited: Boolean,
        val parentSchema: Schema
    ) : PropertyInfo() {
        override val typeInfo: KotlinTypeInfo = KotlinTypeInfo.from(schema, oasKey)
    }

    data class ObjectInlinedField(
        override val isRequired: Boolean,
        override val oasKey: String,
        override val schema: Schema,
        override val isInherited: Boolean,
        val parentSchema: Schema,
        val enclosingSchema: Schema?
    ) : PropertyInfo() {
        override val typeInfo: KotlinTypeInfo =
            if (isInherited) {
                KotlinTypeInfo.from(schema, oasKey, parentSchema)
            } else {
                KotlinTypeInfo.from(schema, oasKey, enclosingSchema)
            }
    }

    data class AdditionalProperties(
        override val schema: Schema,
        override val isInherited: Boolean,
        val parentSchema: Schema
    ) : PropertyInfo() {
        override val oasKey: String = "properties"
        override val typeInfo: KotlinTypeInfo = KotlinTypeInfo.from(schema, "additionalProperties")
        override val isRequired: Boolean = true
    }

    data class OneOfAny(
        override val schema: Schema,
    ) : PropertyInfo() {
        override val oasKey: String = "oneOf"
        override val typeInfo: KotlinTypeInfo = KotlinTypeInfo.AnyType
    }
}
