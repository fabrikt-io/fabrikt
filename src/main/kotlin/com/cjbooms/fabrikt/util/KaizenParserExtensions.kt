package com.cjbooms.fabrikt.util

import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType
import com.cjbooms.fabrikt.generators.MutableSettings
import com.cjbooms.fabrikt.model.OasType
import com.cjbooms.fabrikt.model.PropertyInfo
import com.cjbooms.fabrikt.util.NormalisedString.toModelClassName
import com.reprezen.jsonoverlay.Overlay
import com.reprezen.kaizen.oasparser.model3.Discriminator
import com.reprezen.kaizen.oasparser.model3.OpenApi3
import com.reprezen.kaizen.oasparser.model3.Path
import com.reprezen.kaizen.oasparser.model3.Schema
import java.net.URI

object KaizenParserExtensions {

    private val invalidNames =
        listOf(
            "anyOf",
            "oneOf",
            "allOf",
            "items",
            "schema",
            "application~1json",
            "content",
            "additionalProperties",
            "properties",
        )
    private val simpleTypes = listOf(
        OasType.Text.type,
        OasType.Number.type,
        OasType.Integer.type,
        OasType.Boolean.type,
    )

    private const val EXTENSIBLE_ENUM_KEY = "x-extensible-enum"

    private fun isSealedInterfacesForOneOfEnabled(): Boolean = 
        ModelCodeGenOptionType.SEALED_INTERFACES_FOR_ONE_OF in MutableSettings.modelOptions

    fun Schema.isPolymorphicSuperType(): Boolean = discriminator?.propertyName != null ||
        getDiscriminatorForInLinedObjectUnderAllOf()?.propertyName != null

    fun Schema.printPathFromRoot(): String = Overlay.of(this).pathFromRoot

    fun Schema.isUnsupportedComplexInlinedDefinition() =
        Overlay.of(this).pathFromRoot.contains("paths") &&
            name == null &&
            (isObjectType() || isEnumDefinition())

    fun Schema.isInlinedObjectDefinition() =
        (isObjectType() || isAggregatedObject()) && !isSchemaLess() && (
            Overlay.of(this).pathFromRoot.contains("properties") ||
                Overlay.of(this).pathFromRoot.contains("items")
            )

    private fun Schema.isAggregatedObject(): Boolean =
        combinedAnyOfAndAllOfSchemas().size > 1

    fun Schema.isInlinedTypedAdditionalProperties() =
        isObjectType() && !isSchemaLess() && Overlay.of(this).pathFromRoot.contains("additionalProperties")

    fun Schema.isInlinedEnumDefinition() =
        isEnumDefinition() && !isSchemaLess() && (
            Overlay.of(this).pathFromRoot.contains("properties") ||
                Overlay.of(this).pathFromRoot.contains("items")
            )

    fun Schema.isInlinedArrayDefinition() =
        isArrayType() && !isSchemaLess() && this.itemsSchema.isInlinedObjectDefinition()

    fun Schema.isSchemaLess() = isObjectType() && properties?.isEmpty() == true && (
        oneOfSchemas?.isNotEmpty() != true &&
            allOfSchemas?.isNotEmpty() != true &&
            anyOfSchemas?.isNotEmpty() != true
        )

    fun Schema.isSet(): Boolean =
        isArrayType() && this.isUniqueItems

    fun Schema.isSimpleMapDefinition() = hasAdditionalProperties() && properties?.isEmpty() == true

    fun Schema.isSimpleOneOfAnyDefinition() = oneOfSchemas?.isNotEmpty() == true &&
        !isOneOfWhereAllTypesInheritFromACommonAllOfSuperType() &&
        anyOfSchemas?.isEmpty() == true &&
        allOfSchemas?.isEmpty() == true &&
        properties?.isEmpty() == true

    fun Schema.isEnumDefinition(): Boolean = this.type == OasType.Text.type && (
        this.hasEnums() || (
            MutableSettings.modelOptions.contains(ModelCodeGenOptionType.X_EXTENSIBLE_ENUMS) &&
                extensions.containsKey(EXTENSIBLE_ENUM_KEY)
            )
        )

    fun Schema.isStringDefinitionWithFormat(format: String): Boolean =
        this.type == OasType.Text.type && (this.format?.equals(format, ignoreCase = true) == true)

    @Suppress("UNCHECKED_CAST")
    fun Schema.getEnumValues(): List<String> = when {
        this.hasEnums() -> this.enums.filterNotNull().map { it.toString() }.filterNot { it.isBlank() }
        !MutableSettings.modelOptions.contains(ModelCodeGenOptionType.X_EXTENSIBLE_ENUMS) -> emptyList()
        else -> extensions[EXTENSIBLE_ENUM_KEY]?.let { it as List<String?> }?.filterNotNull()
            ?.filterNot { it.isBlank() } ?: emptyList()
    }

    fun Schema.hasAdditionalProperties(): Boolean = Overlay.of(additionalPropertiesSchema).isPresent && additionalProperties != false

    fun Schema.isUnknownAdditionalProperties(oasKey: String) = type == null &&
        (getSchemaNameInParent() ?: oasKey) == "additionalProperties" && properties?.isEmpty() == true

    fun Schema.isUntypedAdditionalProperties(oasKey: String) = type == OasType.Object.type &&
        (getSchemaNameInParent() ?: oasKey) == "additionalProperties" && properties?.isEmpty() == true

    fun Schema.isTypedAdditionalProperties(oasKey: String) = type == OasType.Object.type &&
        (getSchemaNameInParent() == "additionalProperties" || oasKey == "additionalProperties") && properties?.isEmpty() != true

    fun Schema.isSimpleTypedAdditionalProperties(oasKey: String) = isSimpleType() &&
        (getSchemaNameInParent() == "additionalProperties" || oasKey == "additionalProperties") && properties?.isEmpty() == true

    fun Schema.isMapTypeAdditionalProperties(oasKey: String) = type == OasType.Object.type &&
        (oasKey == "additionalProperties") && properties?.isEmpty() == true &&
        hasAdditionalProperties()

    fun Schema.isComplexTypedAdditionalProperties(oasKey: String) = (getSchemaNameInParent() ?: oasKey) ==
        "additionalProperties" && properties?.isEmpty() != true && !isSimpleType()

    fun Schema.isSimpleType(): Boolean =
        !isOneOfSuperInterface() && ((simpleTypes.contains(type) && !isEnumDefinition()) || isSimpleMapDefinition() || isSimpleOneOfAnyDefinition())

    private fun Schema.isObjectType() =
        OasType.Object.type == type || properties?.isNotEmpty() == true

    private fun Schema.isArrayType() = OasType.Array.type == type

    fun Schema.isNotDefined() = !Overlay.of(this).isPresent &&
        type == null && !(hasAllOfSchemas() || hasOneOfSchemas() || hasAnyOfSchemas())

    private fun Schema.getSchemaNameInParent(): String? = Overlay.of(this).pathInParent

    fun Schema.isPolymorphicSubType(api: OpenApi3): Boolean =
        getEnclosingSchema(api)?.let { schema ->
            schema.allOfSchemas.any { it.isPolymorphicSuperType() }
        } ?: false

    fun Schema.getSuperType(api: OpenApi3): Schema? =
        getEnclosingSchema(api)?.let { schema ->
            schema.allOfSchemas.firstOrNull { it.isPolymorphicSuperType() }
        }

    fun Schema.getDiscriminatorForInLinedObjectUnderAllOf(): Discriminator? =
        this.allOfSchemas.firstOrNull { it.isInLinedObjectUnderAllOf() }?.discriminator

    private fun Schema.getEnclosingSchema(api: OpenApi3): Schema? =
        api.schemas.values.firstOrNull { it.name == safeName() }

    fun Schema.isRequired(
        api: OpenApi3,
        prop: Map.Entry<String, Schema>,
        markReadWriteOnlyOptional: Boolean,
        markAllOptional: Boolean,
    ): Boolean =
        if (markAllOptional || (prop.value.isReadOnly && markReadWriteOnlyOptional) || (prop.value.isWriteOnly && markReadWriteOnlyOptional)) {
            false
        } else {
            requiredFields.contains(prop.key) || isDiscriminatorProperty(api, prop) // A discriminator property should be required
        }

    fun Schema.getSchemaRefName() = Overlay.of(this).jsonReference.split("/").last()

    fun Schema.isDiscriminatorProperty(api: OpenApi3, prop: Map.Entry<String, Schema>): Boolean =
        discriminator?.propertyName == prop.key ||
            findOneOfSuperInterface(api.schemas.values.toList()).any { oneOf ->
                oneOf.discriminator?.propertyName == prop.key
                        && oneOf.discriminator?.mappings?.values?.any { it.endsWith("/$name") } ?: false
            }

    fun Schema.findOneOfSuperInterface(allSchemas: List<Schema>): Set<Schema> {
        if (!isSealedInterfacesForOneOfEnabled()) {
            return emptySet()
        }
        return allSchemas
            .filter { it.oneOfSchemas.isNotEmpty() }
            .mapNotNull { schema ->
                if (schema.oneOfSchemas.toList().contains(this)) schema else null
            }
            .toSet()
    }

    fun Schema.getKeyIfSingleDiscriminatorValue(
        api: OpenApi3,
        prop: Map.Entry<String, Schema>,
        enclosingSchema: Schema,
    ): Map<String, PropertyInfo.DiscriminatorKey>? =
        if (isDiscriminatorProperty(api, prop)) {
            val discriminator = findDiscriminator(api)
            discriminator.mappingKeys(enclosingSchema).map {
                if (prop.value.isEnumDefinition()) {
                    it.key to PropertyInfo.DiscriminatorKey.EnumKey(it.key, it.value)
                } else {
                    it.key to PropertyInfo.DiscriminatorKey.StringKey(it.key, it.value)
                }
            }.toMap()
        } else {
            null
        }

    private fun Schema.findDiscriminator(api: OpenApi3): Discriminator {
        val bestDiscriminator = if (this.hasDiscriminator()) {
            this.discriminator
        } else {
            val oneOfDiscriminator = findOneOfSuperInterface(api.schemas.values.toList()).firstOrNull { oneOfInterface ->
                oneOfInterface.hasDiscriminator()
            }?.discriminator
            oneOfDiscriminator ?: this.discriminator
        }
        return bestDiscriminator
    }

    fun Discriminator.mappingKeys(enclosingSchema: Schema): Map<String, String> {
        val discriminatorMappings = mappings?.map { it.key to it.value.split("/").last() }?.toMap()
        return if (discriminatorMappings.isNullOrEmpty()) {
            mapOf(enclosingSchema.name to enclosingSchema.safeName().toModelClassName())
        } else {
            discriminatorMappings
        }
    }

    fun Discriminator.mappingKeyForSchemaName(schemaName: String): String? =
        mappings.filter { it.value.endsWith(schemaName) }.keys.firstOrNull()

    fun Schema.isInLinedObjectUnderAllOf(): Boolean =
        Overlay.of(this).pathFromRoot
            .splitToSequence("/")
            .toList()
            .let { path ->
                path[path.lastIndex].toIntOrNull() != null && (path[path.lastIndex - 1] == "allOf")
            }

    fun Schema.hasNoDiscriminator(): Boolean = this.discriminator.propertyName == null
    fun Schema.hasDiscriminator(): Boolean = !hasNoDiscriminator()

    fun Schema.safeName(): String =
        when {
            isOneOfWhereAllTypesInheritFromACommonAllOfSuperType() && !isSealedInterfacesForOneOfEnabled() -> 
                this.oneOfSchemas.first().allOfSchemas.first().safeName()
            isInlinedAggregationOfExactlyOne() -> combinedAnyOfAndAllOfSchemas().first().safeName()
            name != null -> name
            else -> Overlay.of(this).pathFromRoot
                .splitToSequence("/")
                .filterNot { invalidNames.contains(it) }
                .filter { it.toIntOrNull() == null } // Ignore numeric-identifiers path-parts in: allOf / oneOf / anyOf
                .last()
                .replace("~1", "-") // so application~1octet-stream becomes application-octet-stream
        }

    fun Schema.safeType(): String? {
        // 1. Direct type is always authoritative
        if (type != null) return type

        // 2. Clear object-like cues
        if (properties?.isNotEmpty() == true) return "object"
        if (allOfSchemas.hasAnyDefinedProperties()) return "object"
        if (oneOfSchemas.hasAnyDefinedProperties()) return "object"
        if (anyOfSchemas.hasAnyDefinedProperties()) return "object"
        if (isOneOfWhereAllTypesInheritFromACommonAllOfSuperType()) return "object"
        if (isUnknownAdditionalProperties("")) return "object"
        if (Overlay.of(additionalPropertiesSchema).isPresent) return "object"

        // 3. All types in anyOf/oneOf are the same? Use that
        val consistentOneOfType = oneOfSchemas.consistentSchemaType()
        if (consistentOneOfType != null) return consistentOneOfType

        val consistentAnyOfType = anyOfSchemas.consistentSchemaType()
        if (consistentAnyOfType != null) return consistentAnyOfType

        val consistentAllOfType = allOfSchemas.consistentSchemaType()
        if (consistentAllOfType != null) return consistentAllOfType

        // 4. Default fallback
        return null
    }

    private fun List<Schema>?.consistentSchemaType(): String? {
        if (this.isNullOrEmpty()) return null

        val nonNullTypes = this.mapNotNull { it.type }.toSet()
        return if (nonNullTypes.size == 1) nonNullTypes.first() else null
    }

    private fun List<Schema>?.hasAnyDefinedProperties(): Boolean =
        this?.any { it.properties?.isNotEmpty() == true } == true

    fun Schema.isOneOfWhereAllTypesInheritFromACommonAllOfSuperType(): Boolean {
        val maybeAllOfInFirstOneOf = this.oneOfSchemas?.firstOrNull()?.allOfSchemas?.firstOrNull()
        // This identifies the OLD allOf-based polymorphism pattern where the BASE type has the discriminator
        return if (maybeAllOfInFirstOneOf != null && maybeAllOfInFirstOneOf.hasDiscriminator())  {
            this.oneOfSchemas.all { it.allOfSchemas.contains(maybeAllOfInFirstOneOf) }
        } else false
    }

    fun Schema.isOneOfResolvingToAnyType(): Boolean {
        // oneOf schemas with discriminators that resolve to Any when sealed interfaces are not enabled
        return this.hasDiscriminator() && 
               this.oneOfSchemas.isNotEmpty() && 
               !isSealedInterfacesForOneOfEnabled()
    }


    fun Schema.isInlinedOneOfSuperInterface() = isOneOfSuperInterfaceOnly() && isInlinedPropertySchema()

    // Not part of any other aggregations
    fun Schema.isOneOfSuperInterfaceOnly() =
        oneOfSchemas.isNotEmpty() && allOfSchemas.isEmpty() && anyOfSchemas.isEmpty() && properties.isEmpty() &&
            oneOfSchemas.all { it.isObjectType() }

    fun Schema.isOneOfSuperInterface() =
        oneOfSchemas.isNotEmpty() && allOfSchemas.isEmpty() && anyOfSchemas.isEmpty() && properties.isEmpty() &&
            oneOfSchemas.all { it.isObjectType() || it.isAggregatedObject() }

    fun Schema.isOneOfSuperInterfaceWithDiscriminator() =
        discriminator != null && discriminator.propertyName != null && isOneOfSuperInterface()

    private fun Schema.isInlinedAggregationOfExactlyOne() =
        combinedAnyOfAndAllOfSchemas().size == 1 && isInlinedPropertySchema()

    private fun Schema.combinedAnyOfAndAllOfSchemas(): List<Schema> =
        (allOfSchemas ?: emptyList()) + (anyOfSchemas ?: emptyList())

    /**
    * Recognises two inlining patterns:
    * - A direct property schema:              /properties/<name>
    * - An array item schema under a property: /properties/<name>/items
    */
    private fun Schema.isInlinedPropertySchema(): Boolean {
        val path = Overlay.of(this).pathFromRoot

        val isDirectProperty = Regex(".*/properties/[^/]+$").matches(path)
        val isArrayItem = Regex(".*/properties/[^/]+/items$").matches(path)

        return isDirectProperty || isArrayItem
    }

    fun OpenApi3.basePath(): String =
        servers
            .firstOrNull()
            ?.url?.let { URI.create(it) }
            ?.path.orEmpty()
            .removeSuffix("/")

    /**
     * Returns a Map of String to list of openapi Path objects,
     * where the String is the uri, but in pascal case suitable
     * for naming classes for controllers and services.
     */
    fun OpenApi3.routeToPaths(): Map<String, Map<String, Path>> = paths
        .map { (name, path) -> name to path }
        .groupBy { it.first.uriToClassName() }
        .mapValues { it.value.toMap() }

    private fun String.uriToClassName(): String = toResourceNames().joinToString("-").toModelClassName()

    private fun String.toResourceNames(): Collection<String> = split("/")
        .filterNot { it.isBlank() || it.matches("\\{.*}".toRegex()) }

    fun String.isSingleResource(): Boolean =
        count { it == '/' } % 2 == 0 && endsWith("}")
}
