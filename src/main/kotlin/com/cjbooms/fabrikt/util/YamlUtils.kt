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
import org.yaml.snakeyaml.LoaderOptions
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
