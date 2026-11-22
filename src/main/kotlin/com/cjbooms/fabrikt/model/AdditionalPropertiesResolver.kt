package com.cjbooms.fabrikt.model

import com.cjbooms.fabrikt.model.PropertyInfo.AdditionalProperties
import com.cjbooms.fabrikt.util.SchemaParserExtensions.isPolymorphicSuperType
import com.cjbooms.fabrikt.model.OpenApi3Document as OpenApi3
import com.cjbooms.fabrikt.model.OpenApiSchema as Schema

internal object AdditionalPropertiesResolver {
    // Follows the intent of the author, not the strict `allOf` semantics of JSON Schema (see README).
    fun Schema.resolveAdditionalProperties(
        properties: List<PropertyInfo>,
        api: OpenApi3,
    ): List<PropertyInfo> {
        val maps = properties.filterIsInstance<AdditionalProperties>()
        val resolved =
            inheritedMap(maps)?.let { if (forbidsAdditionalProperties) it.copy(closed = true) else it }
                ?: ownMap(maps)?.takeIf { isPolymorphicSuperType() }?.widenedAcross(api.allOfDescendants(this))
                ?: return properties
        return properties.filterNot { it is AdditionalProperties } + resolved
    }

    private fun Schema.inheritedMap(maps: List<AdditionalProperties>) = maps.firstOrNull { it.isInherited && !it.isDeclaredBy(this) }

    private fun Schema.ownMap(maps: List<AdditionalProperties>) = maps.firstOrNull { it.isDeclaredBy(this) }

    private fun AdditionalProperties.isDeclaredBy(schema: Schema) = parentSchema.jsonReference == schema.jsonReference

    private fun AdditionalProperties.widenedAcross(descendants: List<Schema>) =
        if (descendants.any { valueTypeDiffersIn(it) }) copy(widened = true) else this

    private fun AdditionalProperties.valueTypeDiffersIn(descendant: Schema) =
        descendant.declaresAdditionalProperties &&
            !descendant.forbidsAdditionalProperties &&
            AdditionalProperties.typeInfoOf(descendant.additionalPropertiesSchema, direction) != typeInfo
}
