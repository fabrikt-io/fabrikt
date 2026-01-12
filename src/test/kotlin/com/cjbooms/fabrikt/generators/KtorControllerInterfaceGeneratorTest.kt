package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.CodeGenTypeOverride
import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.ControllerCodeGenOptionType
import com.cjbooms.fabrikt.cli.ControllerCodeGenTargetType
import com.cjbooms.fabrikt.cli.OutputOptionType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.controller.KtorControllerInterfaceGenerator
import com.cjbooms.fabrikt.model.ControllerLibraryType
import com.cjbooms.fabrikt.model.Destinations.controllersPackage
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.model.toFileSpec
import com.cjbooms.fabrikt.util.GeneratedCodeAsserter.Companion.assertThatGenerated
import com.cjbooms.fabrikt.util.Linter
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.ResourceHelper.readFolder
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Path
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KtorControllerInterfaceGeneratorTest {
    private val basePackage = "ie.zalando"
    private lateinit var generated: Collection<FileSpec>

    @Suppress("unused")
    private fun testCases(): Stream<String> = Stream.of(
        "githubApi",
        "singleAllOf",
        "pathLevelParameters",
        "parameterNameClash",
        "requestBodyAsArray",
        "jakartaValidationAnnotations",
        "modelSuffix",
        "queryParameters",
        "pathParameters",
        "multipartFormData",
    )

    private fun setupGithubApiTestEnv() {
        val api = SourceApi(readTextResource("/examples/githubApi/api.yaml"))
        generated = KtorControllerInterfaceGenerator(
            Packages(basePackage),
            api
        ).generate().files
    }

    @BeforeEach
    fun init() {
        MutableSettings.updateSettings(
            genTypes = setOf(CodeGenerationType.CONTROLLERS),
            controllerTarget = ControllerCodeGenTargetType.KTOR,
        )
        ModelNameRegistry.clear()
    }

    // @Test
    // fun `debug single test`() = `correct controllers are generated for different OpenApi Specifications`("insert testcase here")

    @ParameterizedTest
    @MethodSource("testCases")
    fun `correct controllers are generated for different OpenApi Specifications`(testCaseName: String) {
        val basePackage = "examples.$testCaseName"
        val api = SourceApi(readTextResource("/examples/$testCaseName/api.yaml"))
        val expectedPath = "/examples/$testCaseName/controllers/ktor/Controllers.kt"

        if (testCaseName == "modelSuffix") {
            MutableSettings.updateSettings(modelSuffix = "Dto")
        }

        val ktorControllers = KtorControllerInterfaceGenerator(
            Packages(basePackage),
            api
        )

        val library = ktorControllers.generateLibrary()
        val controllers = ktorControllers.generate().toSingleFile(library)

        assertThatGenerated(controllers).isEqualTo(expectedPath)
    }

    @Test
    fun `correct controllers are generated for ControllerCodeGenOptionType_AUTHENTICATION`() {
        val basePackage = "examples.authentication"
        val api = SourceApi(readTextResource("/examples/authentication/api.yaml"))
        val expectedPath = "/examples/authentication/controllers/ktor/Controllers.kt"

        val generator = KtorControllerInterfaceGenerator(
            Packages(basePackage),
            api,
            setOf(ControllerCodeGenOptionType.AUTHENTICATION),
        )
        val library = generator.generateLibrary()
        val controllers = generator.generate().toSingleFile(library)

        assertThatGenerated(controllers).isEqualTo(expectedPath)
    }

    @Test
    fun `should contain correct number of controller classes`() {
        setupGithubApiTestEnv()
        assertThat(generated.size).isEqualTo(6)
    }

    @Test
    fun `the correct controllers are generated`() {
        setupGithubApiTestEnv()
        assertThat(generated.map { it.name })
            .containsOnly(
                "InternalEventsController",
                "ContributorsController",
                "OrganisationsController",
                "OrganisationsContributorsController",
                "RepositoriesController",
                "RepositoriesPullRequestsController",
            )
    }

    @Test
    fun `ensure package is correct`() {
        setupGithubApiTestEnv()
        assertThat(generated.map { it.packageName }.distinct())
            .containsOnly(controllersPackage(basePackage))
    }

    @Test
    fun `ensure that subresource specific controllers are created`() {
        val api = SourceApi(readTextResource("/examples/githubApi/api.yaml"))
        val controllers = KtorControllerInterfaceGenerator(
            Packages(basePackage),
            api
        ).generate()

        assertThat(controllers.files).size().isEqualTo(6)
        assertThat(controllers.files.map { it.name }).containsAll(
            listOf(
                "ContributorsController",
                "OrganisationsController",
                "OrganisationsContributorsController",
                "RepositoriesController",
                "RepositoriesPullRequestsController",
            ),
        )

        val linkedFunctionNames = controllers.files
            .filter { it.name == "OrganisationsContributorsController" }
            .flatMap { it.members }
            .flatMap { (it as TypeSpec).funSpecs.map { t -> t.name } }
        val ownedFunctionNames = controllers.files
            .filter { it.name == "RepositoriesPullRequestsController" }
            .flatMap { it.members }
            .flatMap { (it as TypeSpec).funSpecs.map { t -> t.name } }
        assertThat(linkedFunctionNames).containsExactlyInAnyOrder(
            "get",
            "getById",
            "putById",
            "deleteById",
        )
        assertThat(ownedFunctionNames).containsExactlyInAnyOrder(
            "get",
            "getById",
            "post",
            "putById",
        )
    }

    @Test
    fun `ensure controller methods has the suspend modifier`() {
        val api = SourceApi(readTextResource("/examples/githubApi/api.yaml"))
        val controllers = KtorControllerInterfaceGenerator(
            Packages(basePackage),
            api
        ).generate()

        assertThat(controllers.files).size().isEqualTo(6)
        assertThat(
            controllers.files
                .flatMap { file -> file.members }
                .flatMap { (it as TypeSpec).funSpecs.map(FunSpec::modifiers) }
                .all { it.contains(KModifier.SUSPEND) },
        ).isTrue()
    }

    @Test
    fun `adds disclaimer as comment to files if enabled`() {
        MutableSettings.updateSettings(
            outputOptions = setOf(OutputOptionType.ADD_FILE_DISCLAIMER)
        )
        val api = SourceApi(readTextResource("/examples/fileComment/api.yaml"))
        val generator = KtorControllerInterfaceGenerator(
            Packages("examples.fileComment"),
            api
        )

        val expectedFiles = readFolder(Path.of("src/test/resources/examples/fileComment/controllers/ktor"))

        val controllers = generator.generate()
        val lib = generator.generateLibrary()

        val files = controllers.files + lib.toFileSpec()

        files.forEach { file ->
            val key = "${file.name}.kt"
            val content = file.toString()
            assertThat(content).isEqualTo(expectedFiles[key])
        }
    }

    private fun KtorControllerInterfaceGenerator.KtorControllers.toSingleFile(extraSpecs: Collection<ControllerLibraryType>): String {
        val destPackage = if (controllers.isNotEmpty()) controllers.first().destinationPackage else ""
        val singleFileBuilder = FileSpec.builder(destPackage, "dummyFilename")

        controllers.forEach {
            singleFileBuilder.addType(it.spec)
        }

        extraSpecs.forEach {
            singleFileBuilder.addType(it.spec)
        }

        val singleFileString = singleFileBuilder.build().toString()

        return Linter.lintString(singleFileString)
    }

    @Test
    fun `ensure generates ByteArray body parameter and response for string with format binary`() {
        val api = SourceApi(readTextResource("/examples/binary/api.yaml"))
        val generator = KtorControllerInterfaceGenerator(
            Packages(basePackage),
            api
        )
        val controllers = generator.generate()
        val lib = generator.generateLibrary()

        val fileStr = controllers.toSingleFile(lib)

        assertThatGenerated(fileStr.trim()).isEqualTo("/examples/binary/controllers/ktor/Controllers.kt")
    }

    @Test
    fun `ensure generates ByteArrayStream body parameter and response for string with format binary`() {
        MutableSettings.addOption(CodeGenTypeOverride.BYTEARRAY_AS_INPUTSTREAM)
        val api = SourceApi(readTextResource("/examples/byteArrayStream/api.yaml"))
        val generator = KtorControllerInterfaceGenerator(
            Packages(basePackage),
            api
        )
        val controllers = generator.generate()
        val lib = generator.generateLibrary()

        val fileStr = controllers.toSingleFile(lib)
        val expectedControllers = "/examples/byteArrayStream/controllers/ktor/Controllers.kt"

        assertThatGenerated(fileStr).isEqualTo(expectedControllers)
    }
}
