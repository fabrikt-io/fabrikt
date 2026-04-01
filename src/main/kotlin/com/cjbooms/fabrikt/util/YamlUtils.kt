package com.cjbooms.fabrikt.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLFactoryBuilder
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.reprezen.kaizen.oasparser.OpenApi3Parser
import com.reprezen.kaizen.oasparser.model3.OpenApi3
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import java.nio.file.Path
import java.nio.file.Paths

object YamlUtils {

    val objectMapper: ObjectMapper =
        ObjectMapper(
            YAMLFactory.builder()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                .increaseMaxFileSize()
                .build()
        )
            .registerKotlinModule()
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
    private val internalMapper: ObjectMapper =
        ObjectMapper(
            YAMLFactory.builder()
                .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                .increaseMaxFileSize()
                .build()
        )

    private fun YAMLFactoryBuilder.increaseMaxFileSize(): YAMLFactoryBuilder = loaderOptions(
        LoaderOptions().apply {
            codePointLimit = 100 * 1024 * 1024 // 100MB
        }
    )

    /**
     * Returns true only if the content contains at least one anchor definition that is also
     * referenced by an alias, e.g.:
     *   enum: &status_values
     *   enum: *status_values
     */
    internal fun containsYamlAnchorsAndAliases(content: String): Boolean {
        val anchorNames = Regex("""\w+:\s+&(\w+)""").findAll(content).map { it.groupValues[1] }.toList()
        if (anchorNames.isEmpty()) return false
        return anchorNames.any { content.contains(Regex("""\w+:\s+\*$it""")) }
    }

    /**
     * Returns the YAML content with all anchors and aliases expanded inline. Jackson's YAML parser
     * does not resolve aliases, causing consumers like the kaizen OpenAPI parser to see a plain
     * string instead of the anchored value. SnakeYAML is used to pre-process the content so that
     * every alias is replaced by a full copy of its anchored value before Jackson parses it.
     */
    fun expandYamlAliases(content: String): String {
        if (!containsYamlAnchorsAndAliases(content)) return content
        val loaderOptions = LoaderOptions().also { it.maxAliasesForCollections = Int.MAX_VALUE }
        val data = runCatching { Yaml(loaderOptions).load<Map<*, *>>(content) }.getOrNull() ?: return content
        val options = DumperOptions().also { it.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK }
        // Deep copy breaks shared object references so SnakeYAML's dumper writes values inline
        // instead of re-emitting aliases, which would undo the expansion.
        return Yaml(options).dump(deepCopy(data))
    }

    private fun deepCopy(value: Any?): Any? = when (value) {
        is Map<*, *> -> value.entries.associateTo(LinkedHashMap()) { (k, v) -> k to deepCopy(v) }
        is List<*> -> value.map { deepCopy(it) }
        else -> value
    }

    fun mergeYamlTrees(mainTree: String, updateTree: String) =
        internalMapper.writeValueAsString(
            mergeNodes(
                internalMapper.readTree(mainTree),
                internalMapper.readTree(updateTree),
            ),
        )!!

    fun parseOpenApi(input: String, inputDir: Path = Paths.get("").toAbsolutePath()): OpenApi3 =
        try {
            val root: JsonNode = objectMapper.readTree(input)
            OpenApi31Downgrader.downgradeIncompatibleElements(root)
            cleanEmptyTypes(root)
            OpenApi3Parser().parse(root, inputDir.toUri().toURL())
        } catch (ex: NullPointerException) {
            throw IllegalArgumentException(
                "The Kaizen openapi-parser library threw a NPE exception when parsing this API. " +
                    "This is commonly due to an external schema reference that is unresolvable, " +
                    "possibly due to a lack of internet connection",
                ex,
            )
        }

    fun cleanEmptyTypes(node: JsonNode) {
        when {
            node.isObject -> {
                val objectNode = node as ObjectNode
                val fieldsToProcess = objectNode.fields().asSequence().toList()

                for ((key, value) in fieldsToProcess) {
                    if (key == "type" && (value.isNull || (value.isTextual && value.asText().isBlank()))) {
                        objectNode.remove("type")
                    } else {
                        cleanEmptyTypes(value)
                    }
                }
            }

            node.isArray -> {
                node.forEach { cleanEmptyTypes(it) }
            }
        }
    }

    /**
     * The below merge function has been shamelessly stolen from Stackoverflow: https://stackoverflow.com/a/32447591/1026785
     * and converted to much nicer Kotlin implementation
     */
    private fun mergeNodes(currentTree: JsonNode, incomingTree: JsonNode): JsonNode {
        incomingTree.fieldNames().forEach { fieldName ->
            val currentNode = currentTree.get(fieldName)
            val incomingNode = incomingTree.get(fieldName)
            if (currentNode is ArrayNode && incomingNode is ArrayNode) {
                incomingNode.forEach {
                    if (currentNode.contains(it)) {
                        mergeNodes(
                            currentNode.get(currentNode.indexOf(it)),
                            it,
                        )
                    } else {
                        currentNode.add(it)
                    }
                }
            } else if (currentNode is ObjectNode) {
                mergeNodes(currentNode, incomingNode)
            } else {
                (currentTree as? ObjectNode)?.replace(fieldName, incomingNode)
            }
        }
        return currentTree
    }
}
