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
            val tag = resolveTag(schema, modelClassName, enclosingSchema.qualifiesModelClassName())
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
            if (enclosingSchema.qualifiesModelClassName()) {
                append(enclosingSchema!!.toModelClassName())
            }
            val modelClassName = schemaInfoName?.toModelClassName() ?: safeName().toModelClassName()
            append(modelClassName)
            if (valueSuffix) {
                append("Value")
            }
            val modelClassNameSuffix = MutableSettings.modelSuffix
            append(modelClassNameSuffix)
        }

    // An array enclosingSchema contributes nothing to toModelClassName's output above, so a
    // schema named through a null enclosingSchema and the same schema named through an array
    // enclosingSchema compute identical strings and must be treated as the same registration.
    private fun Schema?.qualifiesModelClassName(): Boolean = this != null && type != "array"

    private fun resolveTag(
        schema: Schema,
        enclosingSchema: Schema? = null,
        valueSuffix: Boolean = false,
        schemaInfoName: String? = null,
    ): String =
        resolveTag(
            schema,
            schema.toModelClassName(schemaInfoName, enclosingSchema, valueSuffix),
            qualifiedByEnclosingSchema = enclosingSchema.qualifiesModelClassName(),
        )

    // Two schemas can coincidentally compute the same modelClassName (e.g. a property named
    // through its qualifying enclosingSchema colliding with an unrelated top-level schema of
    // that same computed name). Fold in the schema's own position so they get distinct tags,
    // unless the name isn't enclosing-schema-qualified in the first place — in which case two
    // calls for the same schema (e.g. with and without an array enclosingSchema) must share one.
    private fun resolveTag(
        schema: Schema,
        modelClassName: String,
        qualifiedByEnclosingSchema: Boolean,
    ): String {
        val uri = URL(schema.jsonReference)
        val position = if (qualifiedByEnclosingSchema) schema.jsonPathFromRoot else ""
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

    // Names a parameter's $ref'd component property the same way getOrRegister would for that
    // property, so client and model agree regardless of which generator runs first.
    fun getOrRegisterPropertyRef(
        schema: Schema,
        enclosingComponentName: String,
    ): String {
        getByReference(schema)?.let { return it }
        val modelClassName =
            enclosingComponentName.toModelClassName() +
                schema.safeName().toModelClassName() +
                MutableSettings.modelSuffix
        val tag = resolveTag(schema, modelClassName, qualifiedByEnclosingSchema = true)
        return this[tag].getOrElse {
            val suggestion = allocateUniqueName(modelClassName)
            tagToName[tag] = suggestion
            suggestion
        }
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
