package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.JacksonNullabilityMode
import com.cjbooms.fabrikt.cli.SerializationLibrary
import com.cjbooms.fabrikt.cli.SerializationLibraryOptionConverter
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.client.OkHttpEnhancedClientGenerator
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
    fun `Jackson 2 remains the default serialization library`() {
        assertThat(SerializationLibrary.default).isEqualTo(SerializationLibrary.JACKSON)
    }

    @Test
    fun `Jackson 3 can be selected using the CLI value`() {
        assertThat(SerializationLibraryOptionConverter().convert("jackson_3"))
            .isEqualTo(SerializationLibrary.JACKSON_3)
    }

    @Test
    fun `Jackson 3 models keep using the shared Jackson annotations`() {
        val models = ModelGenerator(packages, sourceApi()).generate().toSingleFile()

        assertThat(models)
            .contains("import com.fasterxml.jackson.`annotation`.JsonProperty")
            .doesNotContain("import tools.jackson.annotation")
    }

    @Test
    fun `Jackson nullability modes also apply to Jackson 3`() {
        MutableSettings.updateSettings(
            serializationLibrary = SerializationLibrary.JACKSON_3,
            jacksonNullabilityMode = JacksonNullabilityMode.STRICT,
        )

        assertThat(MutableSettings.effectiveJacksonNullabilityMode)
            .isEqualTo(JacksonNullabilityMode.STRICT)
    }

    @Test
    fun `Jackson 3 OkHttp clients use Jackson 3 runtime types`() {
        val sourceApi = sourceApi()
        val generator = OkHttpSimpleClientGenerator(packages, sourceApi)
        val client = generator.generateDynamicClientCode().toSingleFile()
        val httpUtil =
            generator
                .generateLibrary(emptySet())
                .filterIsInstance<SimpleFile>()
                .first { it.path.fileName.toString() == "HttpUtil.kt" }
                .content
        val enhancedClient =
            OkHttpEnhancedClientGenerator(packages, sourceApi)
                .generateDynamicClientCode(setOf(ClientCodeGenOptionType.RESILIENCE4J))
                .toSingleFile()

        assertThat(client)
            .contains("import tools.jackson.databind.JsonNode")
            .contains("import tools.jackson.databind.json.JsonMapper")
            .contains("import tools.jackson.module.kotlin.jacksonTypeRef")
            .doesNotContain("import com.fasterxml.jackson.databind")
            .doesNotContain("import com.fasterxml.jackson.module.kotlin")
        assertThat(httpUtil)
            .contains("import tools.jackson.core.type.TypeReference")
            .contains("import tools.jackson.databind.json.JsonMapper")
            .doesNotContain("import com.fasterxml.jackson")
        assertThat(enhancedClient)
            .contains("import tools.jackson.databind.json.JsonMapper")
            .contains("import tools.jackson.module.kotlin.jacksonTypeRef")
    }

    @Test
    fun `Jackson 3 response types are used by interface generators`() {
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

        assertThat(openFeignClient)
            .contains("import tools.jackson.databind.JsonNode")
            .doesNotContain("import com.fasterxml.jackson.databind.JsonNode")
        assertThat(springController)
            .contains("import tools.jackson.databind.JsonNode")
            .doesNotContain("import com.fasterxml.jackson.databind.JsonNode")
    }
}
