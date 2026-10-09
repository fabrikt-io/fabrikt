package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.JacksonNullabilityMode
import com.cjbooms.fabrikt.cli.SerializationLibrary
import com.cjbooms.fabrikt.cli.SerializationLibraryOptionConverter
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.client.OkHttpSimpleClientGenerator
import com.cjbooms.fabrikt.generators.client.OpenFeignInterfaceGenerator
import com.cjbooms.fabrikt.generators.controller.SpringControllerInterfaceGenerator
import com.cjbooms.fabrikt.generators.model.ModelGenerator
import com.cjbooms.fabrikt.model.SimpleFile
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import com.cjbooms.fabrikt.util.TestFileUtils.toSingleFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class Jackson3GeneratorTest {
    private val packages = Packages("examples.jackson3")

    private fun sourceApi() = SourceApi(readTextResource("/examples/multiMediaType/api.yaml"))

    @BeforeEach
    fun init() {
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.CLIENT),
            serializationLibrary = SerializationLibrary.JACKSON_3,
        )
        ModelNameRegistry.clear()
    }

    @Test
    fun `Jackson 3 remains the default serialization library`() {
        assertThat(SerializationLibrary.default).isEqualTo(SerializationLibrary.JACKSON_3)
    }

    @Test
    fun `Jackson 3 can be selected using the CLI value`() {
        assertThat(SerializationLibraryOptionConverter().convert("jackson_3"))
            .isEqualTo(SerializationLibrary.JACKSON_3)
        assertThat(SerializationLibraryOptionConverter().convert("jackson"))
            .isEqualTo(SerializationLibrary.JACKSON)
        assertThat(SerializationLibraryOptionConverter().convert("jackson_2"))
            .isEqualTo(SerializationLibrary.JACKSON_2)
    }

    @ParameterizedTest
    @EnumSource(SerializationLibrary::class, names = ["JACKSON", "JACKSON_2", "JACKSON_3"])
    fun `Jackson models keep using the shared Jackson annotations`(library: SerializationLibrary) {
        MutableSettings.updateSettings(serializationLibrary = library)
        val models = ModelGenerator(packages, sourceApi()).generate().toSingleFile()

        assertThat(models)
            .contains("import com.fasterxml.jackson.`annotation`.JsonProperty")
            .doesNotContain("import tools.jackson.annotation")
    }

    @ParameterizedTest
    @EnumSource(SerializationLibrary::class, names = ["JACKSON", "JACKSON_2", "JACKSON_3"])
    fun `Jackson nullability modes apply to all Jackson selections`(library: SerializationLibrary) {
        MutableSettings.updateSettings(
            serializationLibrary = library,
            jacksonNullabilityMode = JacksonNullabilityMode.STRICT,
        )

        assertThat(MutableSettings.effectiveJacksonNullabilityMode)
            .isEqualTo(JacksonNullabilityMode.STRICT)
    }

    @ParameterizedTest
    @EnumSource(SerializationLibrary::class, names = ["JACKSON", "JACKSON_2", "JACKSON_3"])
    fun `OkHttp clients use the selected Jackson runtime types`(library: SerializationLibrary) {
        MutableSettings.updateSettings(genTypes = setOf(CodeGenerationType.CLIENT), serializationLibrary = library)
        val sourceApi = sourceApi()
        val generator = OkHttpSimpleClientGenerator(packages, sourceApi)
        val client = generator.generateDynamicClientCode().toSingleFile()
        val httpUtil =
            generator
                .generateLibrary(emptySet())
                .filterIsInstance<SimpleFile>()
                .first { it.path.fileName.toString() == "HttpUtil.kt" }
                .content

        val prefix = if (library == SerializationLibrary.JACKSON_2) "com.fasterxml.jackson" else "tools.jackson"
        val otherPrefix = if (library == SerializationLibrary.JACKSON_2) "tools.jackson" else "com.fasterxml.jackson"
        val mapper = if (library == SerializationLibrary.JACKSON_2) "databind.ObjectMapper" else "databind.json.JsonMapper"
        assertThat(client)
            .contains("import $prefix.databind.JsonNode")
            .contains("import $prefix.$mapper")
            .contains("import $prefix.module.kotlin.jacksonTypeRef")
            .doesNotContain("import $otherPrefix.databind")
            .doesNotContain("import $otherPrefix.module.kotlin")
        assertThat(httpUtil)
            .contains("import $prefix.core.type.TypeReference")
            .contains("import $prefix.$mapper")
            .doesNotContain("import $otherPrefix")
    }

    @ParameterizedTest
    @EnumSource(SerializationLibrary::class, names = ["JACKSON", "JACKSON_2", "JACKSON_3"])
    fun `Interface generators use the selected Jackson response types`(library: SerializationLibrary) {
        MutableSettings.updateSettings(serializationLibrary = library)
        val sourceApi = sourceApi()
        val openFeignClient =
            OpenFeignInterfaceGenerator(packages, sourceApi)
                .generate(emptySet())
                .clients
                .toSingleFile()
        val springController =
            SpringControllerInterfaceGenerator(
                packages,
                sourceApi,
                JavaxValidationAnnotations,
            ).generate().files.joinToString("\n")

        val prefix = if (library == SerializationLibrary.JACKSON_2) "com.fasterxml.jackson" else "tools.jackson"
        val otherPrefix = if (library == SerializationLibrary.JACKSON_2) "tools.jackson" else "com.fasterxml.jackson"
        assertThat(openFeignClient)
            .contains("import $prefix.databind.JsonNode")
            .doesNotContain("import $otherPrefix.databind.JsonNode")
        assertThat(springController)
            .contains("import $prefix.databind.JsonNode")
            .doesNotContain("import $otherPrefix.databind.JsonNode")
    }
}
