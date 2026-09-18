package com.cjbooms.fabrikt.parser

import com.beust.jcommander.ParameterException
import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.MutableSettings
import com.cjbooms.fabrikt.generators.model.ModelGenerator
import com.cjbooms.fabrikt.model.SchemaConversionOptions
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import com.cjbooms.fabrikt.util.YamlObjectMapper
import com.fasterxml.jackson.databind.JsonNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.net.URI
import java.util.stream.Stream

/**
 * Tests over unmodified third-party schemas vendored under
 * `src/test/resources/examples/jsonSchemaConversion/dialects/`. Each is downloaded verbatim and unedited;
 * refresh by re-downloading from its source URL. GeoJSON and AsyncAPI go all the way through to
 * generated Kotlin models; the GitHub Actions workflow schema and the Kubernetes CRD only assert
 * conversion behaviour (a rejected nested `$ref`, and a documented non-goal, respectively).
 *
 * - `geojson-featurecollection.json` — <https://geojson.org/schema/FeatureCollection.json>
 *   (geojson/schema, MIT License)
 * - `asyncapi-streetlights-kafka.yml` — <https://raw.githubusercontent.com/asyncapi/spec/master/examples/streetlights-kafka-asyncapi.yml>
 *   (asyncapi/spec, Apache License 2.0)
 * - `schemastore-github-workflow.json` — <https://json.schemastore.org/github-workflow.json>
 *   (SchemaStore/schemastore, Apache License 2.0)
 * - `prometheus-operator-podmonitors.yaml` — <https://raw.githubusercontent.com/prometheus-operator/prometheus-operator/main/example/prometheus-operator-crd/monitoring.coreos.com_podmonitors.yaml>
 *   (prometheus-operator/prometheus-operator, Apache License 2.0)
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JsonSchemaDialectsTest {
    @BeforeEach
    fun init() {
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.HTTP_MODELS),
        )
        ModelNameRegistry.clear()
    }

    private fun resource(name: String): JsonNode =
        YamlObjectMapper.instance.readTree(readTextResource("/examples/jsonSchemaConversion/dialects/$name"))

    @Test
    fun `converts and generates from an unmodified draft-07 GeoJSON FeatureCollection schema`() {
        val doc =
            JsonSchemaToOpenApiConverter.convert(
                resource("geojson-featurecollection.json"),
                SchemaConversionOptions("", "FeatureCollection"),
            )
        assertThat(doc.at("/openapi").asText()).isEqualTo("3.1.0")
        assertThat(doc.at("/components/schemas").properties()).isNotEmpty()

        val sourceApi =
            SourceApi(
                YamlObjectMapper.instance.writeValueAsString(doc),
                baseUri = URI.create("file:///geojson-featurecollection.yaml"),
            )
        val models = ModelGenerator(Packages("examples.geojson"), sourceApi).generate()
        assertThat(models.files).isNotEmpty()
    }

    @Test
    fun `plucks a payload schema from an AsyncAPI wrapper that is neither Nakadi nor Kubernetes`() {
        val doc =
            JsonSchemaToOpenApiConverter.convert(
                resource("asyncapi-streetlights-kafka.yml"),
                SchemaConversionOptions("/components/schemas/lightMeasuredPayload", "LightMeasured"),
            )
        assertThat(doc.at("/components/schemas/LightMeasured").isMissingNode).isFalse()

        val sourceApi =
            SourceApi(
                YamlObjectMapper.instance.writeValueAsString(doc),
                baseUri = URI.create("file:///asyncapi-streetlights-kafka.yaml"),
            )
        val models = ModelGenerator(Packages("examples.streetlights"), sourceApi).generate()
        val lightMeasured = models.files.single { it.name == "LightMeasured" }
        assertThat(lightMeasured.toString()).contains("lumens")
    }

    @Test
    fun `rejects a large draft-07 schema whose defaults carry a nested-path ref, with a precise diagnostic`() {
        // Nested-path ref: #/definitions/workflowDispatchInput/properties/options.
        val ex =
            assertThrows<ParameterException> {
                JsonSchemaToOpenApiConverter.convert(
                    resource("schemastore-github-workflow.json"),
                    SchemaConversionOptions("", "GithubWorkflow"),
                )
            }
        assertThat(ex.message).contains("inside another schema")
    }

    @Test
    fun `does not throw when converting a Kubernetes CRD structural schema, without asserting generation succeeds`() {
        assertDoesNotThrow {
            JsonSchemaToOpenApiConverter.convert(
                resource("prometheus-operator-podmonitors.yaml"),
                SchemaConversionOptions("/spec/versions/0/schema/openAPIV3Schema", "PodMonitor"),
            )
        }
    }

    private fun testCases(): Stream<Pair<String, String>> =
        Stream.of(
            "geojson-featurecollection.json" to "",
            "asyncapi-streetlights-kafka.yml" to "/components/schemas/lightMeasuredPayload",
        )

    @ParameterizedTest
    @MethodSource("testCases")
    fun `every convertible dialect fixture upholds the universal output invariants`(testCase: Pair<String, String>) {
        val (fileName, pointer) = testCase
        val doc =
            JsonSchemaToOpenApiConverter.convert(
                resource(fileName),
                SchemaConversionOptions(pointer, "Root"),
            )

        assertThat(doc.at("/openapi").asText()).isEqualTo("3.1.0")

        val exclusiveBooleans = mutableListOf<JsonNode>()
        collectExclusiveBooleanBounds(doc, exclusiveBooleans)
        assertThat(exclusiveBooleans).isEmpty()

        val droppedKeywords = mutableListOf<String>()
        collectKeys(doc, setOf("if", "not", "patternProperties", "unevaluatedProperties", "\$schema"), droppedKeywords)
        assertThat(droppedKeywords).isEmpty()
    }

    private fun collectExclusiveBooleanBounds(
        node: JsonNode,
        out: MutableList<JsonNode>,
    ) {
        when {
            node.isObject -> {
                listOf("exclusiveMinimum", "exclusiveMaximum").forEach { key ->
                    node.get(key)?.takeIf { it.isBoolean }?.let { out.add(it) }
                }
                node.properties().forEach { (_, child) -> collectExclusiveBooleanBounds(child, out) }
            }
            node.isArray -> node.forEach { collectExclusiveBooleanBounds(it, out) }
        }
    }

    private fun collectKeys(
        node: JsonNode,
        forbidden: Set<String>,
        out: MutableList<String>,
    ) {
        when {
            node.isObject -> {
                node.properties().forEach { (key, child) ->
                    if (key in forbidden) out.add(key)
                    collectKeys(child, forbidden, out)
                }
            }
            node.isArray -> node.forEach { collectKeys(it, forbidden, out) }
        }
    }
}
