package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.SerializationLibrary
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.model.ModelGenerator
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.GeneratedCodeAsserter.Companion.assertThatGenerated
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import com.cjbooms.fabrikt.util.TestFileUtils.toSingleFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class AggregatedEnumGeneratorTest {
    @ParameterizedTest
    @MethodSource("versionsAndLibraries")
    fun `wrapped enums retain their type and wrapper metadata`(
        version: String,
        library: SerializationLibrary,
    ) {
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.HTTP_MODELS),
            serializationLibrary = library,
            modelSuffix = "Dto",
        )
        ModelNameRegistry.clear()
        val spec = readTextResource("/examples/aggregatedEnum/api.yaml").replace("3.0.4", version)
        val models = ModelGenerator(Packages("examples.aggregatedEnum"), SourceApi(spec)).generate()
        assertThat(models.models.map { it.spec.name }).containsExactlyInAnyOrder("SomeEnumDto", "SomeObjectDto")
        val generated = models.toSingleFile()

        assertThat(generated)
            .contains("withDefault: SomeEnumDto = SomeEnumDto.BAZ")
            .contains("withoutDefault: SomeEnumDto? = null")
            .contains("nullable: SomeEnumDto? = SomeEnumDto.BAZ")
            .contains("anyOfDefault: SomeEnumDto = SomeEnumDto.BAR")
            .contains("values: List<SomeEnumDto>? = null")
            .contains("A wrapped enum with its own default.")
        assertThatGenerated(generated)
            .isEqualTo("/examples/aggregatedEnum/models/${library.name.lowercase()}/Models.kt")
    }

    companion object {
        @JvmStatic
        @Suppress("DEPRECATION")
        fun versionsAndLibraries(): Stream<Arguments> =
            listOf("3.0.4", "3.1.2", "3.2.0")
                .flatMap { version ->
                    SerializationLibrary.entries.filterNot { it == SerializationLibrary.JACKSON }.map { Arguments.of(version, it) }
                }.stream()
    }
}
