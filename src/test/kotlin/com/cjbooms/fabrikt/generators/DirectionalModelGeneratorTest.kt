package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType
import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType.EXCLUDE_READ_ONLY
import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType.EXCLUDE_WRITE_ONLY
import com.cjbooms.fabrikt.cli.SerializationLibrary
import com.cjbooms.fabrikt.cli.ValidationLibrary
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.model.ModelGenerator
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.GeneratedCodeAsserter.Companion.assertThatGenerated
import com.cjbooms.fabrikt.util.Linter
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.ResourceHelper.getFileNamesInFolder
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
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
    fun `generates directional model graphs`(version: String) {
        val modes =
            mapOf(
                "request" to setOf(EXCLUDE_READ_ONLY),
                "response" to setOf(EXCLUDE_WRITE_ONLY),
                "both" to setOf(EXCLUDE_READ_ONLY, EXCLUDE_WRITE_ONLY),
            )
        SerializationLibrary.entries.forEach { library ->
            val exampleLibrary =
                if (library ==
                    SerializationLibrary.JACKSON_2
                ) {
                    "jackson"
                } else if (library.isJackson3) {
                    "jackson_3"
                } else {
                    library.name.lowercase()
                }
            modes.forEach { (mode, options) ->
                val files = generate(version, library, options)
                files.forEach { (name, code) ->
                    assertThatGenerated(code).isEqualTo("/examples/directionalModels/models/$mode/$exampleLibrary/$name.kt")
                }
                assertThat(files.keys.map { "$it.kt" }).containsExactlyInAnyOrderElementsOf(
                    getFileNamesInFolder(Path.of("src/test/resources/examples/directionalModels/models/$mode/$exampleLibrary")),
                )
                val request = files["Pet"]!!
                val response = files["Pet"]!!
                if (mode in setOf("request", "both")) {
                    assertThat(request).doesNotContain("val name:", "val readList:", "val readMap:")
                }
                if (mode in setOf("response", "both")) {
                    assertThat(response).doesNotContain("val age:", "val writeObject:", "val nullableSecret:")
                }
                if (mode in setOf("request")) {
                    assertThat(request).contains("val age: Int,", "val nickname: String? = null", "val nullableSecret: String?")
                }
                if (mode in setOf("response")) {
                    assertThat(response).contains("val name: String,")
                }
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
