package com.cjbooms.fabrikt.generators.client

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.GeneratorUtils.functionName
import com.cjbooms.fabrikt.generators.GeneratorUtils.toClassName
import com.cjbooms.fabrikt.generators.GeneratorUtils.toKCodeName
import com.cjbooms.fabrikt.generators.TypeFactory
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.ADDITIONAL_HEADERS_PARAMETER_NAME
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.addIncomingParameters
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.deriveClientParameters
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.enhancedClientName
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.multipartParameterToSpecBuilder
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.simpleClientName
import com.cjbooms.fabrikt.generators.client.ClientGeneratorUtils.toClientReturnType
import com.cjbooms.fabrikt.generators.model.JacksonMetadata.TYPE_REFERENCE_IMPORT
import com.cjbooms.fabrikt.model.*
import com.cjbooms.fabrikt.util.KaizenParserExtensions.groupByPathSegment
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.javaparser.utils.CodeGenerationUtils
import com.reprezen.kaizen.oasparser.model3.Operation
import com.squareup.kotlinpoet.*
import java.nio.file.Path

class OkHttpEnhancedClientGenerator(
    private val packages: Packages,
    private val api: SourceApi,
    private val srcPath: Path = Destinations.MAIN_KT_SOURCE
) {

    fun generateDynamicClientCode(options: Set<ClientCodeGenOptionType>): Collection<ClientType> =
        options.ifResilience4jIsEnabled {
            generateResilience4jClientCode()
        }

    private fun generateResilience4jClientCode(): Collection<ClientType> {
        return api.openApi3.groupByPathSegment().map { (resourceName, paths) ->
            val funSpecs: List<FunSpec> = paths.flatMap { (resource, path) ->
                path.operations.map { (verb, operation) ->
                    val parameters = deriveClientParameters(path, operation, packages.base)
                    FunSpec
                        .builder(functionName(operation, resource, verb))
                        .addModifiers(KModifier.PUBLIC)
                        .addAnnotation(
                            AnnotationSpec.builder(Throws::class)
                                .addMember("%T::class", "ApiException".toClassName(packages.client)).build()
                        )
                        .addIncomingParameters(
                            parameters,
                            multipartParameterToSpecBuilder = multipartParameterToSpecBuilder()
                        )
                        .addParameter(
                            ParameterSpec.builder(
                                ADDITIONAL_HEADERS_PARAMETER_NAME,
                                TypeFactory.createMapOfStringToNonNullType(String::class.asTypeName())
                            )
                                .defaultValue("emptyMap()")
                                .build()
                        )
                        .addCode(
                            Resilience4jClientOperationStatement(
                                resource,
                                verb,
                                operation,
                                parameters,
                            ).toStatement()
                        )
                        .returns(operation.toClientReturnType(packages))
                        .build()
                }
            }

            generateCircuitBreakerClientCode(resourceName, funSpecs)
        }.toSet()
    }

    private fun generateCircuitBreakerClientCode(resourceName: String, funSpecs: List<FunSpec>): ClientType {
        val apiClientClassName = simpleClientName(resourceName).toClassName(packages.client)
        val circuitBreakerRegistryClassName =
            "CircuitBreakerRegistry".toClassName("io.github.resilience4j.circuitbreaker")

        val configurableCircuitBreakerNameProperty =
            PropertySpec.builder("circuitBreakerName", String::class.asTypeName())
                .mutable()
                .initializer("%S", apiClientClassName.simpleName.toKCodeName())
                .build()

        val clientProperty = PropertySpec.builder("apiClient", apiClientClassName, KModifier.PRIVATE)
            .initializer("%T(objectMapper, baseUrl, okHttpClient)", apiClientClassName)
            .build()

        val circuitBreakerRegistryProperty =
            PropertySpec.builder("circuitBreakerRegistry", circuitBreakerRegistryClassName, KModifier.PRIVATE)
                .initializer("circuitBreakerRegistry")
                .build()

        val constructor = FunSpec.constructorBuilder()
            .addParameters(
                listOf(
                    ParameterSpec.builder(
                        circuitBreakerRegistryProperty.name,
                        circuitBreakerRegistryProperty.type
                    ).build(),
                    ParameterSpec.builder("objectMapper", ObjectMapper::class.asTypeName()).build(),
                    ParameterSpec.builder("baseUrl", String::class.asTypeName()).build(),
                    ParameterSpec.builder("okHttpClient", "OkHttpClient".toClassName("okhttp3")).build()
                )
            ).build()

        val clientType = TypeSpec.classBuilder(enhancedClientName(resourceName))
            .addKdoc(addClientKDoc())
            .primaryConstructor(constructor)
            .addProperty(circuitBreakerRegistryProperty)
            .addAnnotation(AnnotationSpec.builder(Suppress::class).addMember("%S", "unused").build())
            .addProperty(configurableCircuitBreakerNameProperty)
            .addProperty(clientProperty)
            .addFunctions(funSpecs)
            .build()

        return ClientType(clientType, packages.base, setOf(TYPE_REFERENCE_IMPORT))
    }

    fun generateLibrary(options: Set<ClientCodeGenOptionType>): Collection<GeneratedFile> {
        val clientDir = srcPath
            .resolve(CodeGenerationUtils.packageToPath(packages.base))
            .resolve("client")
        return listOfNotNull(
            applyTemplateIfResilience4jIsEnabled(options) {
                HandlebarsTemplates.applyTemplate(
                    HandlebarsTemplates.clientHttpResilience4jUtils,
                    packages,
                    clientDir,
                    "HttpResilience4jUtil.kt"
                )
            }
        )
    }

    private fun addClientKDoc(): CodeBlock =
        CodeBlock.builder()
            .add("The circuit breaker registry should have the proper configuration to correctly action on circuit breaker ")
            .add("transitions based on the client exceptions [ApiClientException], [ApiServerException] and [IOException].\n")
            .add("\n@see ApiClientException")
            .add("\n@see ApiServerException")
            .build()

    private fun applyTemplateIfResilience4jIsEnabled(
        options: Set<ClientCodeGenOptionType>,
        applyTemplate: () -> SimpleFile
    ): SimpleFile? =
        options.find { it == ClientCodeGenOptionType.RESILIENCE4J }?.let { applyTemplate() }

    private fun Set<ClientCodeGenOptionType>.ifResilience4jIsEnabled(
        block: (Set<ClientCodeGenOptionType>) -> Collection<ClientType>
    ): Collection<ClientType> =
        if (this.any { it == ClientCodeGenOptionType.RESILIENCE4J }) block(this) else emptySet()
}

class Resilience4jClientOperationStatement(
    private val resource: String,
    private val verb: String,
    private val operation: Operation,
    private val parameters: List<IncomingParameter>,
) {
    fun toStatement(): CodeBlock =
        CodeBlock.builder()
            .addCircuitBreakerStatement(parameters)
            .build()

    private fun CodeBlock.Builder.addCircuitBreakerStatement(parameters: List<IncomingParameter>): CodeBlock.Builder {
        this.add("return \n withCircuitBreaker(circuitBreakerRegistry, circuitBreakerName) {\n")
        this.addClientCallStatement(parameters)
        this.add("\n}\n")
        return this
    }

    private fun CodeBlock.Builder.addClientCallStatement(parameters: List<IncomingParameter>): CodeBlock.Builder {
        this.add(
            "apiClient.%N(%L)",
            functionName(operation, resource, verb),
            (parameters.map { it.name } + ADDITIONAL_HEADERS_PARAMETER_NAME).joinToString(","),
        )
        return this
    }
}
