package com.cjbooms.fabrikt.util

import com.cjbooms.fabrikt.generators.MutableSettings
import com.cjbooms.fabrikt.model.SchemaInfo
import com.cjbooms.fabrikt.util.NormalisedString.toModelClassName
import com.cjbooms.fabrikt.util.SchemaParserExtensions.safeName
import java.net.URL
import com.cjbooms.fabrikt.model.OpenApiSchema as Schema

/**
 * Model name registry to avoid name collisions
 */
object ModelNameRegistry {
    private val allocatedNames: MutableSet<String> = mutableSetOf()
    private val tagToName: MutableMap<String, String> = mutableMapOf()
    private val referenceToName: MutableMap<String, String> = mutableMapOf()
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
        val suggestion = if (allocate) allocateUniqueName(modelClassName) else modelClassName

        if (allocate) {
            val tag = resolveTag(schema, modelClassName, disambiguateByPosition = enclosingSchema.contributesToName())
            val replaced = tagToName.put(tag, suggestion)
            if (replaced != null) {
                // Only allow unique tags to be registered
                throw IllegalArgumentException("tag $tag cannot be used for both '$replaced' and '$suggestion'")
            }
        }

        return suggestion
    }

    private fun allocateUniqueName(modelClassName: String): String {
        if (allocatedNames.add(modelClassName)) return modelClassName

        var collisionIndex = 1
        while (true) {
            val numericSuffix = if (collisionIndex == 1) "" else collisionIndex.toString()
            val suggestion = "$modelClassName$SUFFIX$numericSuffix"
            if (allocatedNames.add(suggestion)) return suggestion
            collisionIndex++
        }
    }

    private fun Schema.toModelClassName(
        schemaInfoName: String? = null,
        enclosingSchema: Schema? = null,
        valueSuffix: Boolean = false,
    ): String =
        buildString {
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

    // Mirrors toModelClassName's own guard: an array enclosingSchema contributes nothing to the
    // computed name (line 65 above), so two calls for the same schema — one with no enclosing
    // schema, one with an enclosing array — compute the identical string and must share a tag.
    private fun Schema?.contributesToName(): Boolean = this != null && type != "array"

    private fun resolveTag(
        schema: Schema,
        enclosingSchema: Schema? = null,
        valueSuffix: Boolean = false,
        schemaInfoName: String? = null,
    ): String =
        resolveTag(
            schema,
            schema.toModelClassName(schemaInfoName, enclosingSchema, valueSuffix),
            disambiguateByPosition = enclosingSchema.contributesToName(),
        )

    // The tag normally keys only on the computed name string, since several code paths
    // deliberately alias an inline schema's name onto an unrelated named schema (e.g. a oneOf
    // whose members share a common discriminated allOf supertype takes that supertype's name).
    // Those aliases always compute their name with no enclosingSchema. A property whose name is
    // built by concatenating an enclosingSchema's name with its own (e.g. Product + state ->
    // "ProductState") can coincidentally collide with an unrelated schema that happens to share
    // that exact string — that case must not share a tag, so it's disambiguated by jsonPathFromRoot.
    private fun resolveTag(
        schema: Schema,
        modelClassName: String,
        disambiguateByPosition: Boolean = false,
    ): String {
        val uri = URL(schema.jsonReference)
        val position = if (disambiguateByPosition) schema.jsonPathFromRoot else ""
        return "file:${uri.file}#$position#$modelClassName"
    }

    /** Retrieve a model class name created with [ModelNameRegistry.register]. */
    operator fun get(tag: String): Result<String> =
        runCatching {
            requireNotNull(tagToName[tag]) { "unknown tag: $tag" }
        }

    fun getOrRegister(
        schema: Schema,
        enclosingSchema: Schema? = null,
        valueSuffix: Boolean = false,
    ): String {
        getByReference(schema)?.let { return it }
        return this[resolveTag(schema, enclosingSchema, valueSuffix)]
            .getOrElse { register(schema, enclosingSchema, valueSuffix) }
    }

    fun getOrRegister(schemaInfo: SchemaInfo): String {
        getByReference(schemaInfo.schema)?.let { return it }
        return this[resolveTag(schemaInfo.schema, schemaInfoName = schemaInfo.name)]
            .getOrElse { register(schemaInfo.schema, schemaInfoName = schemaInfo.name) }
    }

    fun preRegisterByReference(
        schema: Schema,
        name: String,
    ) {
        val ref = schema.jsonReference
        if (!referenceToName.containsKey(ref)) {
            val modelClassName = name.toModelClassName() + MutableSettings.modelSuffix
            referenceToName[ref] = allocateUniqueName(modelClassName)
        }
    }

    private fun getByReference(schema: Schema): String? {
        val ref = schema.jsonReference
        return referenceToName[ref]
    }

    private val inlineSchemaTracking: MutableMap<Schema, String> = mutableMapOf()

    /**
     * Pre-compute what name an inline schema will get without allocating it yet.
     * Called from findOneOfSuperInterface to map schema -> future name.
     */
    fun preRegisterInlineSchema(
        schema: Schema,
        enclosingSchema: Schema,
    ) {
        if (inlineSchemaTracking.containsKey(schema)) return
        val modelClassName = register(schema, enclosingSchema, valueSuffix = false, schemaInfoName = null, allocate = false)
        inlineSchemaTracking[schema] = modelClassName
    }

    fun getBySchema(schema: Schema): String? = inlineSchemaTracking[schema]

    fun clear() {
        allocatedNames.clear()
        tagToName.clear()
        inlineSchemaTracking.clear()
        referenceToName.clear()
    }
}
