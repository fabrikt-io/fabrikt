package com.cjbooms.fabrikt.parser

import com.beust.jcommander.ParameterException
import com.cjbooms.fabrikt.model.SchemaConversionOptions
import com.cjbooms.fabrikt.util.YamlObjectMapper
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.util.logging.Logger

/**
 * Reshapes a JSON Schema document (draft-04 through 2020-12) into an OpenAPI 3.1 document.
 *
 * Reads and writes only Jackson nodes, independent of the OpenAPI parser implementation.
 */
internal object JsonSchemaToOpenApiConverter {
    private val logger = Logger.getGlobal()
    private val mapper = YamlObjectMapper.instance

    private const val OPENAPI_VERSION = "3.1.0"
    private const val INFO_VERSION = "0.0.0"
    private const val COMPONENT_REF_PREFIX = "#/components/schemas/"
    private const val DEFAULT_ROOT_SCHEMA_NAME = "Schema"

    private val DEFINITION_KEYS = listOf("definitions", "\$defs")
    private val DEFINITION_REF_PREFIXES = listOf("#/definitions/", "#/\$defs/")

    private val DROPPED_KEYWORDS =
        setOf(
            "\$schema",
            "\$id",
            "id",
            "\$anchor",
            "\$comment",
            "\$dynamicRef",
            "\$dynamicAnchor",
            "\$recursiveRef",
            "\$recursiveAnchor",
            "definitions",
            "\$defs",
            "dependencies",
            "dependentRequired",
            "dependentSchemas",
            "if",
            "then",
            "else",
            "not",
            "contains",
            "minContains",
            "maxContains",
            "propertyNames",
            "patternProperties",
            "additionalItems",
            "unevaluatedItems",
            "unevaluatedProperties",
            "prefixItems",
        )

    fun convert(
        resourceRoot: JsonNode,
        options: SchemaConversionOptions,
    ): ObjectNode {
        val schemaNode = resolveSchemaNode(resourceRoot, options.schemaPointer)
        val rootSchemaName = deriveRootSchemaName(resourceRoot, schemaNode, options.rootSchemaNameOverride)

        schemaNode.get("\$schema")?.takeIf { it.isTextual }?.let {
            logger.info("Converting JSON Schema dialect '${it.asText()}' to OpenAPI $OPENAPI_VERSION")
        }

        val schemas = mapper.createObjectNode()
        collectDefinitions(schemaNode).properties().forEach { (name, definition) ->
            schemas.set<JsonNode>(name, convertSchema(definition, isTopLevelDefinition = true))
        }

        val properties = schemaNode.get("properties")?.takeIf { it.isObject } as ObjectNode?
        if (schemas.isEmpty && properties == null) {
            throw ParameterException(
                "The JSON Schema at pointer '${options.schemaPointer}' has neither definitions nor " +
                    "properties, so there is nothing to generate from.",
            )
        }

        if (properties != null) {
            if (schemas.has(rootSchemaName)) {
                throw ParameterException(
                    "Cannot name the converted root schema '$rootSchemaName': a definition with that " +
                        "name already exists. Choose a different --json-schema-root-name.",
                )
            }
            val root = mapper.createObjectNode()
            root.put("type", "object")
            root.set<JsonNode>("properties", convertProperties(properties))
            schemaNode.get("required")?.let { root.set<JsonNode>("required", it) }
            schemas.set<JsonNode>(rootSchemaName, root)
        }

        resolveTransitiveComponentRefs(schemas, resourceRoot)

        val info = mapper.createObjectNode()
        info.put("title", rootSchemaName)
        info.put("version", INFO_VERSION)

        val components = mapper.createObjectNode()
        components.set<JsonNode>("schemas", schemas)

        val document = mapper.createObjectNode()
        document.put("openapi", OPENAPI_VERSION)
        document.set<JsonNode>("info", info)
        document.set<JsonNode>("paths", mapper.createObjectNode())
        document.set<JsonNode>("components", components)

        validateRefs(document, schemas)
        return document
    }

    fun resolveSchemaNode(
        resourceRoot: JsonNode,
        schemaPointer: String,
    ): ObjectNode {
        // JSON Pointer (RFC 6901); do not URL-decode.
        val resolved = resourceRoot.at(schemaPointer.removePrefix("#"))
        if (resolved.isMissingNode || !resolved.isObject) {
            throw ParameterException(
                "No JSON Schema object found at pointer '$schemaPointer' in the supplied resource. " +
                    "Check the '#/json/pointer' fragment on --json-schema-file.",
            )
        }
        return resolved as ObjectNode
    }

    fun deriveRootSchemaName(
        resourceRoot: JsonNode,
        schemaNode: JsonNode,
        explicitOverride: String?,
    ): String {
        explicitOverride?.takeIf { it.isNotBlank() }?.let { return it }
        schemaNode.get("title")?.takeIf { it.isTextual && it.asText().isNotBlank() }?.let { return it.asText() }
        resourceRoot
            .at("/metadata/name")
            .takeIf { it.isTextual && it.asText().isNotBlank() }
            ?.let { return it.asText() }
        logger.warning(
            "No root schema name found (no 'title', no '/metadata/name', no --json-schema-root-name); " +
                "defaulting to '$DEFAULT_ROOT_SCHEMA_NAME'. Pass --json-schema-root-name to override.",
        )
        return DEFAULT_ROOT_SCHEMA_NAME
    }

    private fun collectDefinitions(schemaNode: ObjectNode): ObjectNode {
        val definitions = mapper.createObjectNode()
        DEFINITION_KEYS.forEach { key ->
            (schemaNode.get(key)?.takeIf { it.isObject } as ObjectNode?)?.properties()?.forEach { (name, definition) ->
                if (definitions.has(name)) {
                    throw ParameterException(
                        "Duplicate schema name '$name' appears in both 'definitions' and '\$defs'.",
                    )
                }
                definitions.set<JsonNode>(name, definition)
            }
        }
        return definitions
    }

    /**
     * Hoists `#/components/schemas/<name>` siblings referenced but not yet collected (e.g. an
     * AsyncAPI document's own `components.schemas`). Resolves transitively.
     */
    private fun resolveTransitiveComponentRefs(
        schemas: ObjectNode,
        resourceRoot: JsonNode,
    ) {
        var pending = true
        while (pending) {
            pending = false
            val referencedNames = mutableSetOf<String>()
            collectComponentRefNames(schemas, referencedNames)
            referencedNames.forEach { name ->
                if (!schemas.has(name)) {
                    val sibling = resourceRoot.at("/components/schemas/$name")
                    if (sibling.isObject) {
                        schemas.set<JsonNode>(name, convertSchema(sibling))
                        pending = true
                    }
                }
            }
        }
    }

    private fun collectComponentRefNames(
        node: JsonNode,
        out: MutableSet<String>,
    ) {
        when {
            node.isObject -> {
                node.get("\$ref")?.takeIf { it.isTextual }?.asText()?.let { ref ->
                    if (ref.startsWith(COMPONENT_REF_PREFIX) && !ref.removePrefix(COMPONENT_REF_PREFIX).contains('/')) {
                        out.add(ref.removePrefix(COMPONENT_REF_PREFIX))
                    }
                }
                node.properties().forEach { (_, child) -> collectComponentRefNames(child, out) }
            }
            node.isArray -> node.forEach { collectComponentRefNames(it, out) }
        }
    }

    private fun convertSchema(
        schema: JsonNode,
        isTopLevelDefinition: Boolean = false,
    ): JsonNode {
        if (schema !is ObjectNode) return schema

        val examples = schema.get("examples")
        val isEnumHint =
            isTopLevelDefinition &&
                examples != null &&
                examples.isArray &&
                schema.get("type")?.asText() == "string" &&
                (schema.properties().map { it.key }.toSet() - setOf("type", "examples", "description")).isEmpty()

        val result = mapper.createObjectNode()
        schema.properties().forEach { (key, value) ->
            when {
                key in DROPPED_KEYWORDS -> Unit
                key == "\$ref" -> result.put("\$ref", rewriteRef(value.asText()))
                key == "properties" -> result.set<JsonNode>("properties", convertProperties(value as ObjectNode))
                key == "items" -> if (!value.isArray) result.set<JsonNode>("items", convertSchema(value))
                key == "additionalProperties" -> result.set<JsonNode>("additionalProperties", convertSchema(value))
                key == "anyOf" || key == "oneOf" || key == "allOf" -> {
                    val branches = value.filterNot { it.isConstraintOnlyBranch() }
                    if (branches.isNotEmpty()) {
                        val converted = mapper.createArrayNode()
                        branches.forEach { converted.add(convertSchema(it)) }
                        result.set<JsonNode>(key, converted)
                    }
                }
                key == "examples" -> if (!isEnumHint) result.set<JsonNode>("example", value.get(0))
                else -> result.set<JsonNode>(key, value)
            }
        }

        if (isEnumHint) {
            result.set<JsonNode>("x-extensible-enum", examples)
        }
        result.normaliseConst()
        result.normaliseExclusiveBound("minimum", "exclusiveMinimum")
        result.normaliseExclusiveBound("maximum", "exclusiveMaximum")
        if (result.has("properties") && !result.has("type")) {
            result.put("type", "object")
        }
        return result
    }

    private fun convertProperties(properties: ObjectNode): ObjectNode {
        val result = mapper.createObjectNode()
        properties.properties().forEach { (name, propertySchema) ->
            result.set<JsonNode>(name, convertSchema(propertySchema, isTopLevelDefinition = false))
        }
        return result
    }

    private fun ObjectNode.normaliseConst() {
        val constValue = remove("const") ?: return
        if (!has("enum")) {
            set<JsonNode>("enum", mapper.createArrayNode().add(constValue))
        }
    }

    private fun ObjectNode.normaliseExclusiveBound(
        inclusiveKey: String,
        exclusiveKey: String,
    ) {
        val exclusive = get(exclusiveKey) ?: return
        if (!exclusive.isBoolean) return
        remove(exclusiveKey)
        if (!exclusive.booleanValue()) return
        val bound = remove(inclusiveKey) ?: return
        set<JsonNode>(exclusiveKey, bound)
    }

    private fun JsonNode.isConstraintOnlyBranch(): Boolean {
        if (!isObject) return false
        if (has("\$ref") ||
            has("type") ||
            has("enum") ||
            has("const") ||
            has("allOf") ||
            has("oneOf") ||
            has("anyOf")
        ) {
            return false
        }
        val properties = get("properties")?.takeIf { it.isObject } ?: return true
        return properties.properties().none { (_, propertySchema) -> propertySchema.carriesTypeInformation() }
    }

    private fun JsonNode.carriesTypeInformation(): Boolean =
        isObject &&
            (has("type") || has("\$ref") || has("enum") || has("const") || has("items") || has("properties"))

    private fun rewriteRef(ref: String): String {
        DEFINITION_REF_PREFIXES.forEach { prefix ->
            if (ref.startsWith(prefix)) return COMPONENT_REF_PREFIX + ref.removePrefix(prefix)
        }
        return ref
    }

    private fun validateRefs(
        node: JsonNode,
        schemas: ObjectNode,
    ) {
        when {
            node.isObject -> {
                node.get("\$ref")?.takeIf { it.isTextual }?.asText()?.let { ref ->
                    if (!ref.startsWith(COMPONENT_REF_PREFIX)) {
                        throw ParameterException(
                            "Unsupported reference '$ref' in the converted schema: only local references " +
                                "to sibling definitions are supported.",
                        )
                    }
                    val name = ref.removePrefix(COMPONENT_REF_PREFIX)
                    if (name.contains('/')) {
                        throw ParameterException(
                            "Unsupported reference '$ref' in the converted schema: references that point " +
                                "inside another schema are not supported.",
                        )
                    }
                    if (!schemas.has(name)) {
                        throw ParameterException(
                            "Unresolved reference '$ref' in the converted schema: no schema named '$name' " +
                                "was found in the source document's 'definitions', '\$defs', or " +
                                "'components/schemas'.",
                        )
                    }
                }
                node.properties().forEach { (_, child) -> validateRefs(child, schemas) }
            }
            node.isArray -> node.forEach { validateRefs(it, schemas) }
        }
    }
}
