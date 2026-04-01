package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.cli.ClientCodeGenTargetType
import com.cjbooms.fabrikt.cli.CodeGenTypeOverride
import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.ExternalReferencesResolutionMode
import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType
import com.cjbooms.fabrikt.cli.OutputOptionType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.client.OkHttpEnhancedClientGenerator
import com.cjbooms.fabrikt.generators.client.OkHttpSimpleClientGenerator
import com.cjbooms.fabrikt.generators.model.ModelGenerator
import com.cjbooms.fabrikt.model.ClientType
import com.cjbooms.fabrikt.model.Clients
import com.cjbooms.fabrikt.model.SimpleFile
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.TestFileUtils.toSingleFile
import com.cjbooms.fabrikt.util.Linter
import com.cjbooms.fabrikt.util.GeneratedCodeAsserter.Companion.assertThatGenerated
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Paths
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OkHttpClientGeneratorTest {

    @Suppress("unused")
    private fun fullApiTestCases(): Stream<String> = Stream.of(
        "okHttpClient",
        "multiMediaType",
        "okHttpClientPostWithoutRequestBody",
        "pathLevelParameters",
        "parameterNameClash",
        "byteArrayStream",
    )

    @BeforeEach
    fun init() {
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.CLIENT),
            clientTarget = ClientCodeGenTargetType.OK_HTTP,
            modelOptions = setOf(ModelCodeGenOptionType.X_EXTENSIBLE_ENUMS, ModelCodeGenOptionType.DISABLE_SEALED_INTERFACES_FOR_ONE_OF),
            typeOverrides = setOf(CodeGenTypeOverride.BYTEARRAY_AS_INPUTSTREAM)
        )
        ModelNameRegistry.clear()
    }

    @ParameterizedTest
    @MethodSource("fullApiTestCases")
    fun `correct api simple client is generated from a full API definition`(testCaseName: String) {
        val packages = Packages("examples.$testCaseName")
        val apiLocation = javaClass.getResource("/examples/$testCaseName/api.yaml")!!
        val sourceApi = SourceApi(apiLocation.readText(), baseDir = Paths.get(apiLocation.toURI()))

        val expectedModel = "/examples/$testCaseName/models/ClientModels.kt"
        val expectedClient = "/examples/$testCaseName/client/ApiClient.kt"

        val models = ModelGenerator(
            packages,
            sourceApi
        ).generate().toSingleFile()
        val simpleClientCode = OkHttpSimpleClientGenerator(
            packages,
            sourceApi
        )
            .generateDynamicClientCode()
            .toSingleFile()

        assertThatGenerated(models).isEqualTo(expectedModel)
        assertThatGenerated(simpleClientCode).isEqualTo(expectedClient)
    }

    @ParameterizedTest
    @MethodSource("fullApiTestCases")
    fun `correct api fault-tolerant service client is generated when the resilience4j option is set`(testCaseName: String) {
        val packages = Packages("examples.$testCaseName")
        val apiLocation = javaClass.getResource("/examples/$testCaseName/api.yaml")!!
        val sourceApi = SourceApi(apiLocation.readText(), baseDir = Paths.get(apiLocation.toURI()))

        val expectedLibUtil = "/examples/$testCaseName/client/HttpResilience4jUtil.kt"
        val expectedClientCode = "/examples/$testCaseName/client/ApiService.kt"

        val generator =
            OkHttpEnhancedClientGenerator(packages, sourceApi)
        val enhancedLibUtil = generator.generateLibrary(setOf(ClientCodeGenOptionType.RESILIENCE4J))
            .filterIsInstance<SimpleFile>()
            .first { it.path.fileName.toString() == "HttpResilience4jUtil.kt" }
        val enhancedClientCode = generator.generateDynamicClientCode(setOf(ClientCodeGenOptionType.RESILIENCE4J))

        assertThatGenerated(enhancedLibUtil.content).isEqualTo(expectedLibUtil)
        assertThatGenerated(enhancedClientCode.toSingleFile()).isEqualTo(expectedClientCode)
    }

    @ParameterizedTest
    @MethodSource("fullApiTestCases")
    fun `the enhanced client is not generated when no specific options are provided`(testCaseName: String) {
        val packages = Packages("examples.$testCaseName")
        val apiLocation = javaClass.getResource("/examples/$testCaseName/api.yaml")!!
        val sourceApi = SourceApi(apiLocation.readText(), baseDir = Paths.get(apiLocation.toURI()))

        val enhancedClientCode = OkHttpEnhancedClientGenerator(
            packages,
            sourceApi
        )
            .generateDynamicClientCode(emptySet())

        assertThat(enhancedClientCode).isEqualTo(emptySet<ClientType>())
    }

    @ParameterizedTest
    @MethodSource("fullApiTestCases")
    fun `correct http utility libraries are generated`(testCaseName: String) {
        val packages = Packages("examples.$testCaseName")
        val apiLocation = javaClass.getResource("/examples/$testCaseName/api.yaml")!!
        val sourceApi = SourceApi(apiLocation.readText(), baseDir = Paths.get(apiLocation.toURI()))

        val expectedHttpUtils = "/examples/$testCaseName/client/HttpUtil.kt"

        val generatedHttpUtils = OkHttpSimpleClientGenerator(
            packages,
            sourceApi
        ).generateLibrary().filterIsInstance<SimpleFile>()
            .first { it.path.fileName.toString() == "HttpUtil.kt" }

        assertThatGenerated(generatedHttpUtils.content).isEqualTo(expectedHttpUtils)
    }

    @Test
    fun `correct api client and models are generated with external reference solution mode AGGRESSIVE`() {
        val packages = Packages("examples.externalReferences.aggressive")
        val apiLocation = javaClass.getResource("/examples/externalReferences/aggressive/api.yaml")!!
        val sourceApi = SourceApi(apiLocation.readText(), baseDir = Paths.get(apiLocation.toURI()))

        val expectedModel = "/examples/externalReferences/aggressive/models/ClientModels.kt"
        val expectedClient = "/examples/externalReferences/aggressive/client/ApiClient.kt"
        val expectedClientCode = "/examples/externalReferences/aggressive/client/ApiService.kt"
        MutableSettings.updateSettings(
            modelOptions = setOf(ModelCodeGenOptionType.DISABLE_SEALED_INTERFACES_FOR_ONE_OF),
            externalRefResolutionMode = ExternalReferencesResolutionMode.AGGRESSIVE,
        )

        val models = ModelGenerator(
            packages,
            sourceApi,
        ).generate().toSingleFile()
        val generator =
            OkHttpEnhancedClientGenerator(packages, sourceApi)
        val simpleClientCode = OkHttpSimpleClientGenerator(
            packages,
            sourceApi
        )
            .generateDynamicClientCode()
            .toSingleFile()
        val enhancedClientCode = generator.generateDynamicClientCode(setOf(ClientCodeGenOptionType.RESILIENCE4J))
            .toSingleFile()

        assertThatGenerated(models).isEqualTo(expectedModel)
        assertThatGenerated(simpleClientCode).isEqualTo(expectedClient)
        assertThatGenerated(enhancedClientCode).isEqualTo(expectedClientCode)
    }

    @Test
    fun `adds disclaimer as comment to files if enabled`() {
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.CLIENT),
            clientTarget = ClientCodeGenTargetType.OK_HTTP,
            outputOptions = setOf(OutputOptionType.ADD_FILE_DISCLAIMER)
        )
        val api = SourceApi(readTextResource("/examples/fileComment/api.yaml"))
        val generator = OkHttpSimpleClientGenerator(
            Packages("examples.fileComment"),
            api
        )

        val expectedClient = readTextResource("/examples/fileComment/client/okhttp/PetsClient.kt")

        val clientTypes = generator.generateDynamicClientCode()

        val content = Clients(clientTypes).files.first().toString().let { Linter.lintString(it) }

        assertThat(content).isEqualTo(expectedClient)
    }
}
