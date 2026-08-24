package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.cli.ClientCodeGenTargetType
import com.cjbooms.fabrikt.cli.CodeGenTypeOverride
import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.ExternalReferencesResolutionMode
import com.cjbooms.fabrikt.cli.ModelCodeGenOptionType
import com.cjbooms.fabrikt.cli.OutputOptionType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.client.OkHttpClientGenerator
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
        "multipartUpload",
    )

    @Suppress("unused")
    private fun groupedClientTestCases(): Stream<String> = Stream.concat(fullApiTestCases(), Stream.of("tagGrouping"))

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
    @MethodSource("groupedClientTestCases")
    fun `correct api simple client is generated from a full API definition`(testCaseName: String) {
        val packages = Packages("examples.$testCaseName")
        val apiLocation = javaClass.getResource("/examples/$testCaseName/api.yaml")!!
        val sourceApi = SourceApi(apiLocation.readText(), baseUri = apiLocation.toURI())

        val expectedModel = "/examples/$testCaseName/models/ClientModels.kt"
        val expectedClient = expectedClientPath(testCaseName, "ApiClient.kt")

        val models = ModelGenerator(
            packages,
            sourceApi
        ).generate().toSingleFile()
        val simpleClientCode = OkHttpClientGenerator(
            packages,
            sourceApi,
            Paths.get("src/main/kotlin")
        )
            .generate(optionsFor(testCaseName))
            .clients
            .toSingleFile()

        if (testCaseName != "tagGrouping") {
            assertThatGenerated(models).isEqualTo(expectedModel)
        }
        assertThatGenerated(simpleClientCode).isEqualTo(expectedClient)
    }

    @ParameterizedTest
    @MethodSource("groupedClientTestCases")
    fun `correct api fault-tolerant service client is generated when the resilience4j option is set`(testCaseName: String) {
        val packages = Packages("examples.$testCaseName")
        val apiLocation = javaClass.getResource("/examples/$testCaseName/api.yaml")!!
        val sourceApi = SourceApi(apiLocation.readText(), baseUri = apiLocation.toURI())

        val expectedLibUtil = "/examples/$testCaseName/client/HttpResilience4jUtil.kt"
        val expectedClientCode = expectedClientPath(testCaseName, "ApiService.kt")
        val options = setOf(ClientCodeGenOptionType.RESILIENCE4J) + optionsFor(testCaseName)

        val generator =
            OkHttpEnhancedClientGenerator(packages, sourceApi)
        val enhancedLibUtil = generator.generateLibrary(options)
            .filterIsInstance<SimpleFile>()
            .contentOf("HttpResilience4jUtil.kt")
        val enhancedClientCode = generator.generateDynamicClientCode(options)

        if (testCaseName != "tagGrouping") {
            assertThatGenerated(enhancedLibUtil).isEqualTo(expectedLibUtil)
        }
        assertThatGenerated(enhancedClientCode.toSingleFile()).isEqualTo(expectedClientCode)
    }

    @ParameterizedTest
    @MethodSource("fullApiTestCases")
    fun `the enhanced client is not generated when no specific options are provided`(testCaseName: String) {
        val packages = Packages("examples.$testCaseName")
        val apiLocation = javaClass.getResource("/examples/$testCaseName/api.yaml")!!
        val sourceApi = SourceApi(apiLocation.readText(), baseUri = apiLocation.toURI())

        val enhancedClientCode = OkHttpEnhancedClientGenerator(
            packages,
            sourceApi
        )
            .generateDynamicClientCode(emptySet())

        assertThat(enhancedClientCode).isEqualTo(emptySet<ClientType>())
    }

    @ParameterizedTest
    @MethodSource("fullApiTestCases")
    fun `correct client library files are generated`(testCaseName: String) {
        val packages = Packages("examples.$testCaseName")
        val apiLocation = javaClass.getResource("/examples/$testCaseName/api.yaml")!!
        val sourceApi = SourceApi(apiLocation.readText(), baseUri = apiLocation.toURI())

        val expectedHttpUtils = "/examples/$testCaseName/client/HttpUtil.kt"
        val expectedApiModels = "/examples/$testCaseName/client/ApiModels.kt"
        val expectedOAuth = "/examples/$testCaseName/client/OAuth.kt"

        val generatedLibrary = OkHttpSimpleClientGenerator(
            packages,
            sourceApi
        ).generateLibrary(emptySet()).filterIsInstance<SimpleFile>()

        assertThatGenerated(generatedLibrary.contentOf("HttpUtil.kt")).isEqualTo(expectedHttpUtils)
        assertThatGenerated(generatedLibrary.contentOf("ApiModels.kt")).isEqualTo(expectedApiModels)
        assertThatGenerated(generatedLibrary.contentOf("OAuth.kt")).isEqualTo(expectedOAuth)
    }

    private fun Collection<SimpleFile>.contentOf(fileName: String): String =
        first { it.path.fileName.toString() == fileName }.content

    private fun optionsFor(testCaseName: String): Set<ClientCodeGenOptionType> =
        if (testCaseName == "tagGrouping") setOf(ClientCodeGenOptionType.GROUP_BY_TAG) else emptySet()

    private fun expectedClientPath(testCaseName: String, fileName: String): String =
        if (testCaseName == "tagGrouping") {
            "/examples/$testCaseName/client/grouped/$fileName"
        } else {
            "/examples/$testCaseName/client/$fileName"
        }

    @Test
    fun `correct api client, models and library files are generated with external reference solution mode AGGRESSIVE`() {
        val packages = Packages("examples.externalReferences.aggressive")
        val apiLocation = javaClass.getResource("/examples/externalReferences/aggressive/api.yaml")!!
        val sourceApi = SourceApi(apiLocation.readText(), baseUri = apiLocation.toURI())

        val expectedModel = "/examples/externalReferences/aggressive/models/ClientModels.kt"
        val expectedClient = "/examples/externalReferences/aggressive/client/ApiClient.kt"
        val expectedClientCode = "/examples/externalReferences/aggressive/client/ApiService.kt"
        val expectedHttpUtils = "/examples/externalReferences/aggressive/client/HttpUtil.kt"
        val expectedApiModels = "/examples/externalReferences/aggressive/client/ApiModels.kt"
        val expectedOAuth = "/examples/externalReferences/aggressive/client/OAuth.kt"
        val expectedLibUtil = "/examples/externalReferences/aggressive/client/HttpResilience4jUtil.kt"
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
        val simpleClientGenerator = OkHttpSimpleClientGenerator(packages, sourceApi)
        val simpleClientCode = simpleClientGenerator
            .generateDynamicClientCode()
            .toSingleFile()
        val enhancedClientCode = generator.generateDynamicClientCode(setOf(ClientCodeGenOptionType.RESILIENCE4J))
            .toSingleFile()
        val simpleClientLibrary = simpleClientGenerator.generateLibrary(emptySet()).filterIsInstance<SimpleFile>()
        val enhancedLibUtil = generator.generateLibrary(setOf(ClientCodeGenOptionType.RESILIENCE4J))
            .filterIsInstance<SimpleFile>()
            .contentOf("HttpResilience4jUtil.kt")

        assertThatGenerated(models).isEqualTo(expectedModel)
        assertThatGenerated(simpleClientCode).isEqualTo(expectedClient)
        assertThatGenerated(enhancedClientCode).isEqualTo(expectedClientCode)
        assertThatGenerated(simpleClientLibrary.contentOf("HttpUtil.kt")).isEqualTo(expectedHttpUtils)
        assertThatGenerated(simpleClientLibrary.contentOf("ApiModels.kt")).isEqualTo(expectedApiModels)
        assertThatGenerated(simpleClientLibrary.contentOf("OAuth.kt")).isEqualTo(expectedOAuth)
        assertThatGenerated(enhancedLibUtil).isEqualTo(expectedLibUtil)
    }

    @Test
    fun `correct api client and library files are generated when the non-null response payloads option is set`() {
        val packages = Packages("examples.okHttpClientNonNullResponsePayloads")
        val apiLocation = javaClass.getResource("/examples/okHttpClientNonNullResponsePayloads/api.yaml")!!
        val sourceApi = SourceApi(apiLocation.readText(), baseUri = apiLocation.toURI())
        val options = setOf(ClientCodeGenOptionType.OKHTTP_NON_NULL_RESPONSE_PAYLOADS)

        val expectedClient = "/examples/okHttpClientNonNullResponsePayloads/client/ApiClient.kt"
        val expectedHttpUtils = "/examples/okHttpClientNonNullResponsePayloads/client/HttpUtil.kt"
        val expectedApiModels = "/examples/okHttpClientNonNullResponsePayloads/client/ApiModels.kt"

        val simpleClientCode = OkHttpClientGenerator(
            packages,
            sourceApi,
            Paths.get("src/main/kotlin")
        )
            .generate(options)
            .clients
            .toSingleFile()
        val generatedLibrary = OkHttpSimpleClientGenerator(
            packages,
            sourceApi
        ).generateLibrary(options).filterIsInstance<SimpleFile>()

        assertThatGenerated(simpleClientCode).isEqualTo(expectedClient)
        assertThatGenerated(generatedLibrary.contentOf("HttpUtil.kt")).isEqualTo(expectedHttpUtils)
        assertThatGenerated(generatedLibrary.contentOf("ApiModels.kt")).isEqualTo(expectedApiModels)
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
