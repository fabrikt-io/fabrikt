package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.CodeGenTypeOverride
import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.ControllerCodeGenOptionType
import com.cjbooms.fabrikt.cli.OutputOptionType
import com.cjbooms.fabrikt.cli.ValidationLibrary
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.controller.KtorControllerInterfaceGenerator
import com.cjbooms.fabrikt.generators.controller.SpringControllerInterfaceGenerator
import com.cjbooms.fabrikt.generators.controller.SpringControllers
import com.cjbooms.fabrikt.generators.controller.metadata.SpringImports
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
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Path
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SpringControllerGeneratorTest {
    private val basePackage = "ie.zalando"
    private lateinit var generated: Collection<FileSpec>

    @Suppress("unused")
    private fun testCases(): Stream<String> = Stream.of(
        "arrays",
        "githubApi",
        "singleAllOf",
        "pathLevelParameters",
        "parameterNameClash",
        "jakartaValidationAnnotations",
        "modelSuffix",
        "unsupportedInlinedDefinitions",
        "httpStatusCodeRangeDefinition",
        "multipartFormData",
    )

    private fun setupGithubApiTestEnv(annotations: ValidationAnnotations = JavaxValidationAnnotations) {
        val api = SourceApi(readTextResource("/examples/githubApi/api.yaml"))
        generated = SpringControllerInterfaceGenerator(Packages(basePackage), api, annotations).generate().files
    }

    @BeforeEach
    fun init() {
        MutableSettings.updateSettings(genTypes = setOf(CodeGenerationType.CONTROLLERS))
        ModelNameRegistry.clear()
    }

    // @Test
    // fun `debug single test`() = `correct models are generated for different OpenApi Specifications`("singleAllOf")

    @ParameterizedTest
    @MethodSource("testCases")
    fun `correct models are generated for different OpenApi Specifications`(testCaseName: String) {
        val basePackage = "examples.$testCaseName"
        val api = SourceApi(readTextResource("/examples/$testCaseName/api.yaml"))
        val expectedControllers = "/examples/$testCaseName/controllers/spring/Controllers.kt"

        if (testCaseName == "modelSuffix") {
            MutableSettings.updateSettings(modelSuffix = "Dto")
        }

        val controllers = SpringControllerInterfaceGenerator(
            Packages(basePackage),
            api,
            JavaxValidationAnnotations,
        ).generate().toSingleFile()

        assertThatGenerated(controllers).isEqualTo(expectedControllers)
    }

    @Test
    fun `correct models are generated for ControllerCodeGenOptionType_AUTHENTICATION`() {
        val basePackage = "examples.authentication"
        val api = SourceApi(readTextResource("/examples/authentication/api.yaml"))
        val expectedControllers = "/examples/authentication/controllers/spring/Controllers.kt"

        val controllers = SpringControllerInterfaceGenerator(
            Packages(basePackage),
            api,
            JavaxValidationAnnotations,
            setOf(ControllerCodeGenOptionType.AUTHENTICATION),
        ).generate().toSingleFile()

        assertThatGenerated(controllers).isEqualTo(expectedControllers)
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
    fun `ensure controller has correct annotations`() {
        setupGithubApiTestEnv()
        val controllerAnnotations =
            generated.flatMap { it.members.flatMap { ts -> (ts as TypeSpec).annotations.map { t -> t.typeName.toString() } } }
                .distinct()

        assertThat(controllerAnnotations).containsOnly(
            "org.springframework.stereotype.Controller",
            "org.springframework.validation.`annotation`.Validated",
            "org.springframework.web.bind.`annotation`.RequestMapping",
        )
    }

    @ParameterizedTest
    @EnumSource(ValidationLibrary::class)
    fun `ensure controller method parameters have correct validation annotations`(library: ValidationLibrary) {
        setupGithubApiTestEnv(library.annotations)
        if (library == ValidationLibrary.NO_VALIDATION) return
        val desiredPackagePrefix = when (library) {
            ValidationLibrary.JAVAX_VALIDATION -> "javax.validation."
            ValidationLibrary.JAKARTA_VALIDATION -> "jakarta.validation."
            else -> throw IllegalArgumentException("Unknown library: $library")
        }
        val parameterValidationAnnotations = generated
            .flatMap { it.members }
            .filterIsInstance<TypeSpec>()
            .flatMap { it.funSpecs }
            .flatMap { it.parameters }
            .flatMap { it.annotations }
            .map { it.typeName.toString() }
            .filter { ".validation." in it }
            .distinct()

        assertThat(parameterValidationAnnotations).isNotEmpty
        assertThat(parameterValidationAnnotations).allMatch { it.startsWith(desiredPackagePrefix) }
    }

    @Test
    fun `ensure that subresource specific controllers are created`() {
        val api = SourceApi(readTextResource("/examples/githubApi/api.yaml"))
        val controllers =
            SpringControllerInterfaceGenerator(Packages(basePackage), api, JavaxValidationAnnotations).generate()

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
    fun `ensure controller methods has the correct modifiers`() {
        val api = SourceApi(readTextResource("/examples/githubApi/api.yaml"))
        val controllers = SpringControllerInterfaceGenerator(
            Packages(basePackage),
            api,
            JavaxValidationAnnotations,
            setOf(ControllerCodeGenOptionType.SUSPEND_MODIFIER),
        ).generate()

        assertThat(controllers.files).size().isEqualTo(6)
        assertThat(
            controllers.files
                .flatMap { file -> file.members }
                .flatMap { (it as TypeSpec).funSpecs.map(FunSpec::modifiers) }
                .all { it.contains(KModifier.SUSPEND) },
        ).isTrue()
    }

    private fun SpringControllers.toSingleFile(): String {
        val destPackage = if (controllers.isNotEmpty()) controllers.first().destinationPackage else ""
        val singleFileBuilder = FileSpec.builder(destPackage, "dummyFilename")
        controllers.forEach {
            val builder = singleFileBuilder
                .addImport(SpringImports.Static.REQUEST_METHOD.first, SpringImports.Static.REQUEST_METHOD.second)
                .addImport(
                    SpringImports.Static.RESPONSE_STATUS.first,
                    SpringImports.Static.RESPONSE_STATUS.second,
                )
                .addType(it.spec)
            builder.build()
        }
        return Linter.lintString(singleFileBuilder.build().toString())
    }

    @Test
    fun `controller parameters should have spring DateTimeFormat annotations`() {
        val api = SourceApi(readTextResource("/examples/springFormatDateAndDateTime/api.yaml"))
        val controllers =
            SpringControllerInterfaceGenerator(Packages(basePackage), api, JavaxValidationAnnotations).generate()
                .toSingleFile()
        val expectedControllers = "/examples/springFormatDateAndDateTime/controllers/Controllers.kt"

        assertThatGenerated(controllers.trim()).isEqualTo(expectedControllers)
    }

    @Test
    fun `ensure generates ByteArray body parameter and response for string with format binary`() {
        val api = SourceApi(readTextResource("/examples/binary/api.yaml"))
        val controllers =
            SpringControllerInterfaceGenerator(Packages(basePackage), api, JavaxValidationAnnotations).generate()
                .toSingleFile()
        val expectedControllers = "/examples/binary/controllers/spring/Controllers.kt"

        assertThatGenerated(controllers.trim()).isEqualTo(expectedControllers)
    }

    @Test
    fun `ensure generates ByteArrayStream body parameter and response for string with format binary`() {
        MutableSettings.addOption(CodeGenTypeOverride.BYTEARRAY_AS_INPUTSTREAM)
        val api = SourceApi(readTextResource("/examples/byteArrayStream/api.yaml"))
        val controllers = SpringControllerInterfaceGenerator(Packages(basePackage), api, JavaxValidationAnnotations).generate().toSingleFile()
        val expectedControllers = "/examples/byteArrayStream/controllers/spring/Controllers.kt"

        assertThatGenerated(controllers.trim()).isEqualTo(expectedControllers)
    }
    
    fun `controller functions are wrapped by CompletionStage`() {
        val basePackage = "examples.completionStage"
        val api = SourceApi(readTextResource("/examples/githubApi/api.yaml"))
        val expectedControllers = "/examples/githubApi/controllers/spring-completion-stage/Controllers.kt"

        val controllers = SpringControllerInterfaceGenerator(
            Packages(basePackage),
            api,
            JavaxValidationAnnotations,
            setOf(ControllerCodeGenOptionType.COMPLETION_STAGE),
        ).generate().toSingleFile()

        assertThatGenerated(controllers).isEqualTo(expectedControllers)
    }

    @Test
    fun `controller functions with x-async-support=false extension are NOT wrapped by CompletionStage`() {
        val basePackage = "examples.completionStage"
        val api = SourceApi(readTextResource("/examples/githubApi/api.yaml"))
        val expectedControllers = "/examples/githubApi/controllers/spring-completion-stage/Controllers.kt"

        val controllers = SpringControllerInterfaceGenerator(
            Packages(basePackage),
            api,
            JavaxValidationAnnotations,
            setOf(ControllerCodeGenOptionType.COMPLETION_STAGE),
        ).generate().toSingleFile()

        assertThatGenerated(controllers).isEqualTo(expectedControllers)
    }

    @Test
    fun `adds disclaimer as comment to files if enabled`() {
        MutableSettings.updateSettings(
            outputOptions = setOf(OutputOptionType.ADD_FILE_DISCLAIMER)
        )
        val api = SourceApi(readTextResource("/examples/fileComment/api.yaml"))
        val generator = SpringControllerInterfaceGenerator(
            Packages("examples.fileComment"),
            api,
            NoValidationAnnotations,
        )

        val expectedFiles = readFolder(Path.of("src/test/resources/examples/fileComment/controllers/spring"))

        val controllers = generator.generate()
        val lib = generator.generateLibrary()

        val files = controllers.files + lib.toFileSpec()

        files.forEach { file ->
            val key = "${file.name}.kt"
            val content = file.toString()
            assertThat(content).isEqualTo(expectedFiles[key])
        }
    }
}
