package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType
import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType.EXCLUDE_READ_ONLY
import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType.EXCLUDE_WRITE_ONLY
import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType.REQUEST_RESPONSE_MODELS
import com.cjbooms.fabrikt.cli.SerializationLibrary
import com.cjbooms.fabrikt.cli.ValidationLibrary
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.model.ModelGenerator
import com.cjbooms.fabrikt.model.KotlinTypeInfo
import com.cjbooms.fabrikt.model.ModelDirection
import com.cjbooms.fabrikt.model.PropertyInfo
import com.cjbooms.fabrikt.model.PropertyInfo.Companion.topLevelProperties
import com.cjbooms.fabrikt.model.SchemaInfo
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.GeneratedCodeAsserter.Companion.assertThatGenerated
import com.cjbooms.fabrikt.util.Linter
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.ResourceHelper.getFileNamesInFolder
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.nio.file.Path

class DirectionalModelGeneratorTest {
    @AfterEach
    fun reset() {
        MutableSettings.updateSettings()
        ModelNameRegistry.clear()
    }

    @ParameterizedTest
    @ValueSource(strings = ["3.0.3", "3.1.2", "3.2.0"])
    @Suppress("DEPRECATION")
    fun `generates directional model graphs`(version: String) {
        val modes =
            mapOf(
                "request" to setOf(EXCLUDE_READ_ONLY),
                "response" to setOf(EXCLUDE_WRITE_ONLY),
                "both" to setOf(EXCLUDE_READ_ONLY, EXCLUDE_WRITE_ONLY),
                "dual" to setOf(REQUEST_RESPONSE_MODELS),
            )
        SerializationLibrary.entries.filterNot { it == SerializationLibrary.JACKSON }.forEach { library ->
            modes.forEach { (mode, options) ->
                val files = generate(version, library, options)
                files.forEach { (name, code) ->
                    assertThatGenerated(code).isEqualTo("/examples/directionalModels/models/$mode/${library.name.lowercase()}/$name.kt")
                }
                assertThat(files.keys.map { "$it.kt" }).containsExactlyInAnyOrderElementsOf(
                    getFileNamesInFolder(Path.of("src/test/resources/examples/directionalModels/models/$mode/${library.name.lowercase()}")),
                )
                val request = files[if (mode == "dual") "PetRequest" else "Pet"]!!
                val response = files[if (mode == "dual") "PetResponse" else "Pet"]!!
                if (mode in setOf("request", "both", "dual")) {
                    assertThat(request).doesNotContain("val name:", "val readList:", "val readMap:")
                }
                if (mode in setOf("response", "both", "dual")) {
                    assertThat(response).doesNotContain("val age:", "val writeObject:", "val nullableSecret:")
                }
                if (mode in setOf("request", "dual")) {
                    assertThat(request).contains("val age: Int,", "val nickname: String? = null", "val nullableSecret: String?")
                }
                if (mode in setOf("response", "dual")) {
                    assertThat(response).contains("val name: String,")
                }
                if (mode == "dual") {
                    assertThat(request).contains("val child: PetRequest?", "List<PetRequest>", "PetNestedRequest", "StateRequest")
                    assertThat(response).contains("val child: PetResponse?", "List<PetResponse>", "PetNestedResponse", "StateResponse")
                    assertThat(files["ReadOnlyRecordRequest"]).contains("object ReadOnlyRecordRequest")
                }
            }
        }
    }

    @Test
    fun `keeps custom suffix before direction`() {
        val files = generate("3.0.3", SerializationLibrary.JACKSON, setOf(REQUEST_RESPONSE_MODELS), "Dto")
        assertThat(files.keys).contains("PetDtoRequest", "PetDtoResponse", "StateDtoRequest", "StateDtoResponse")
    }

    @ParameterizedTest
    @ValueSource(strings = ["3.0.3", "3.1.2", "3.2.0"])
    fun `interleaved resolution keeps direction local to types and properties`(version: String) {
        MutableSettings.updateSettings(modelOptions = setOf(REQUEST_RESPONSE_MODELS))
        val spec = readTextResource("/examples/directionalModels/api.yaml").replace("3.0.3", version)
        val api = SourceApi(spec)
        val schema = api.openApi3.schemas.getValue("Pet")
        val original = SchemaInfo("Pet", schema)
        val request = SchemaInfo("Pet", schema, ModelDirection.REQUEST)
        val response = SchemaInfo("Pet", schema, ModelDirection.RESPONSE)
        val requestProperties = schema.topLevelProperties(PropertyInfo.httpSettings(ModelDirection.REQUEST), api.openApi3, schema)
        val responseProperties = schema.topLevelProperties(PropertyInfo.httpSettings(ModelDirection.RESPONSE), api.openApi3, schema)

        listOf(ModelDirection.RESPONSE, ModelDirection.REQUEST, null, ModelDirection.RESPONSE).forEach { direction ->
            val suffix = direction?.suffix ?: ""
            assertThat(KotlinTypeInfo.from(schema, direction = direction).generatedModelClassName).isEqualTo("Pet$suffix")
            val children = KotlinTypeInfo.from(schema.properties.getValue("children"), direction = direction) as KotlinTypeInfo.Array
            assertThat(children.parameterizedType.generatedModelClassName).isEqualTo("Pet$suffix")
            val index = KotlinTypeInfo.from(schema.properties.getValue("index"), direction = direction) as KotlinTypeInfo.Map
            assertThat(index.parameterizedType.generatedModelClassName).isEqualTo("Pet$suffix")
            val state = KotlinTypeInfo.from(schema.properties.getValue("state"), direction = direction)
            assertThat(state.generatedModelClassName).isEqualTo("State$suffix")
        }

        assertThat(original.typeInfo.generatedModelClassName).isEqualTo("Pet")
        assertThat(request.typeInfo.generatedModelClassName).isEqualTo("PetRequest")
        assertThat(response.typeInfo.generatedModelClassName).isEqualTo("PetResponse")
        assertThat(requestProperties.map { it.name }).contains("age").doesNotContain("name")
        assertThat(responseProperties.map { it.name }).contains("name").doesNotContain("age")
        assertThat(requestProperties.first { it.name == "child" }.typeInfo.generatedModelClassName).isEqualTo("PetRequest")
        assertThat(responseProperties.first { it.name == "child" }.typeInfo.generatedModelClassName).isEqualTo("PetResponse")
        assertThat(KotlinTypeInfo.from(schema).generatedModelClassName).isEqualTo("Pet")
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "discriminatedOneOf",
            "polymorphicModels",
            "nestedPolymorphicModels",
            "inlinedAggregatedObjects",
            "oneOfMarkerInterface",
            "mapExamples",
            "arrays",
            "externalReferences/targeted",
            "externalReferences/relativeSchemaDocument",
        ],
    )
    fun `keeps composition and external references within each model set`(example: String) {
        ModelNameRegistry.clear()
        MutableSettings.updateSettings(modelOptions = setOf(REQUEST_RESPONSE_MODELS))
        val location = javaClass.getResource("/examples/$example/api.yaml")!!
        val models =
            ModelGenerator(
                Packages("examples.directionalModels"),
                SourceApi(location.readText(), baseUri = location.toURI()),
            ).generate()
        val names = models.models.map { it.className.simpleName }
        assertThat(names).isNotEmpty().doesNotHaveDuplicates()
        val reference = Regex("examples\\.directionalModels\\.models\\.([A-Za-z0-9_]+)")
        models.models.forEach { model ->
            val direction = if (model.className.simpleName.endsWith("Request")) "Request" else "Response"
            assertThat(model.className.simpleName).endsWith(direction)
            reference.findAll(model.spec.toString()).forEach { match ->
                assertThat(match.groupValues[1]).isIn(names).endsWith(direction)
            }
        }
    }

    private fun generate(
        version: String,
        library: SerializationLibrary,
        options: Set<ModelCodeGenOptionType>,
        suffix: String = "",
    ): Map<String, String> {
        ModelNameRegistry.clear()
        MutableSettings.updateSettings(
            modelOptions = options,
            modelSuffix = suffix,
            serializationLibrary = library,
            validationLibrary = ValidationLibrary.NO_VALIDATION,
        )
        val spec = readTextResource("/examples/directionalModels/api.yaml").replace("3.0.3", version)
        return ModelGenerator(Packages("examples.directionalModels"), SourceApi(spec))
            .generate()
            .files
            .associate { it.name to Linter.lintString(it.toString()) }
    }
}
