package com.cjbooms.fabrikt.util

import com.cjbooms.fabrikt.generators.MutableSettings
import com.cjbooms.fabrikt.model.SchemaInfo
import com.cjbooms.fabrikt.util.KaizenParserExtensions.safeName
import com.cjbooms.fabrikt.util.NormalisedString.toModelClassName
import com.reprezen.jsonoverlay.Overlay
import com.reprezen.kaizen.oasparser.model3.Schema
import java.net.URL

/**
 * Model name registry to avoid name collisions
 */
object ModelNameRegistry {
    private val allocatedNames: MutableSet<String> = mutableSetOf()
    private val tagToName: MutableMap<String, String> = mutableMapOf()
    private const val SUFFIX = "Extra"

    /**
     * Registers a new model class name using `schema` and if it is inlined type also based on enclosed schema.
     * The returned value can be queried multiple times by passing `tag` to
     * [ModelNameRegistry.get].
     */
    private fun register(
        schema: Schema,
        enclosingSchema: Schema? = null,
        valueSuffix: Boolean = false,
        schemaInfoName: String? = null,
        allocate: Boolean = true,
    ): String {
        val modelClassName = schema.toModelClassName(schemaInfoName, enclosingSchema, valueSuffix)
        var suggestion = modelClassName
        
        if (allocate) {
            while (allocatedNames.contains(suggestion)) {
                suggestion += SUFFIX
            }
            allocatedNames.add(suggestion)

            val tag = resolveTag(schema, modelClassName)
            val replaced = tagToName.put(tag, suggestion)
            if (replaced != null) {
                // Only allow unique tags to be registered
                throw IllegalArgumentException("tag $tag cannot be used for both '$replaced' and '$suggestion'")
            }
        }

        return suggestion
    }

    private fun Schema.toModelClassName(
        schemaInfoName: String? = null,
        enclosingSchema: Schema? = null,
        valueSuffix: Boolean = false,
    ): String = buildString {
        val enclosingClassName = enclosingSchema?.toModelClassName()
        if (enclosingClassName != null && enclosingSchema.type != "array") {
            append(enclosingClassName)
        }
        val modelClassName = schemaInfoName?.toModelClassName() ?: safeName().toModelClassName()
        append(modelClassName)
        if (valueSuffix) {
            append("Value")
        }
        val modelClassNameSuffix = MutableSettings.modelSuffix
        append(modelClassNameSuffix)
    }

    private fun resolveTag(
        schema: Schema,
        enclosingSchema: Schema? = null,
        valueSuffix: Boolean = false,
        schemaInfoName: String? = null,
    ): String =
        resolveTag(schema, schema.toModelClassName(schemaInfoName, enclosingSchema, valueSuffix))

    private fun resolveTag(schema: Schema, modelClassName: String): String {
        val overlay = Overlay.of(schema)
        val uri = URL(overlay.jsonReference)
        return "file:${uri.file}#$modelClassName"
    }

    /** Retrieve a model class name created with [ModelNameRegistry.register]. */
    operator fun get(tag: String): Result<String> = runCatching {
        requireNotNull(tagToName[tag]) { "unknown tag: $tag" }
    }

    fun getOrRegister(
        schema: Schema,
        enclosingSchema: Schema? = null,
        valueSuffix: Boolean = false,
    ) = this[resolveTag(schema, enclosingSchema, valueSuffix)]
        .getOrElse { register(schema, enclosingSchema, valueSuffix) }

    fun getOrRegister(
        schemaInfo: SchemaInfo,
    ) =
        this[resolveTag(schemaInfo.schema, schemaInfoName = schemaInfo.name)]
            .getOrElse { register(schemaInfo.schema, schemaInfoName = schemaInfo.name) }

    private val inlineSchemaTracking: MutableMap<Schema, String> = mutableMapOf()
    
    /**
     * Pre-compute what name an inline schema will get without allocating it yet.
     * Called from findOneOfSuperInterface to map schema -> future name.
     */
    fun preRegisterInlineSchema(schema: Schema, enclosingSchema: Schema) {
        if (inlineSchemaTracking.containsKey(schema)) return
        val modelClassName = register(schema, enclosingSchema, valueSuffix = false, schemaInfoName = null, allocate = false)
        inlineSchemaTracking[schema] = modelClassName
    }
    

    fun getBySchema(schema: Schema): String? =
        inlineSchemaTracking[schema]

    fun clear() {
        allocatedNames.clear()
        tagToName.clear()
        inlineSchemaTracking.clear()
    }
}
