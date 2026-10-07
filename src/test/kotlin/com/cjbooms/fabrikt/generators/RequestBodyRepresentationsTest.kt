package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.cli.ClientCodeGenTargetType
import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.client.OkHttpClientGenerator
import com.cjbooms.fabrikt.generators.client.OpenFeignInterfaceGenerator
import com.cjbooms.fabrikt.generators.client.SpringHttpInterfaceGenerator
import com.cjbooms.fabrikt.model.KotlinTypeInfo
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.cjbooms.fabrikt.util.ResourceHelper.readTextResource
import com.cjbooms.fabrikt.util.effectiveRequestSchema
import com.cjbooms.fabrikt.util.requestOperations
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.nio.file.Paths

class RequestBodyRepresentationsTest {
    private val mapper = jacksonObjectMapper()

    @BeforeEach
    fun setup() {
        MutableSettings.updateSettings(genTypes = setOf(CodeGenerationType.HTTP_MODELS))
        ModelNameRegistry.clear()
    }

    @Test
    fun `all media types alias the existing component model`() {
        val api = SourceApi(readTextResource("/examples/multipleRequestMediaTypes/api.yaml"))
        val operation = api.requestOperations(api.openApi3.paths.getValue("/alias")).single().second
        assertThat(operation.requestBody.contentMediaTypes).hasSize(3)
        assertThat(
            operation.requestBody.contentMediaTypes.values
                .map { KotlinTypeInfo.from(it.schema).generatedModelClassName },
        ).containsOnly("RequestsDetailsRequest")
        assertThat(api.allSchemas.map { it.name }).doesNotContain("RequestBody", "post_alias_request")
    }

    @Test
    fun `each representation keeps its own model when content order is reversed`() {
        val document = mapper.readTree(readTextResource("/examples/multipleRequestMediaTypes/api.yaml"))
        val content = document.at("/paths/~1distinct/post/requestBody/content") as ObjectNode
        val entries =
            content
                .fields()
                .asSequence()
                .toList()
                .reversed()
        content.removeAll()
        entries.forEach { (key, value) -> content.set<com.fasterxml.jackson.databind.JsonNode>(key, value) }
        val api = SourceApi(document.toString())
        val variants = api.requestOperations(api.openApi3.paths.getValue("/distinct"))
        assertThat(
            variants.map {
                it.second.requestBody.contentMediaTypes.keys
                    .single()
            },
        ).containsExactly("text/json", "application/json")
        assertThat(
            variants.map {
                KotlinTypeInfo
                    .from(
                        it.second.requestBody.contentMediaTypes.values
                            .single()
                            .schema,
                    ).generatedModelClassName
            },
        ).containsExactly("CountRequest", "RequestsDetailsRequest")
    }

    @Test
    fun `wrapper metadata is not discarded and variants do not collide with an operation id`() {
        val document = mapper.readTree(readTextResource("/examples/multipleRequestMediaTypes/api.yaml"))
        (document.at("/paths/~1alias/post/requestBody/content/text~1json/schema") as ObjectNode).put("nullable", true)
        (document.at("/paths/~1inline/post") as ObjectNode).put("operationId", "createDetailsTextJson")
        val api = SourceApi(document.toString())
        val aliases = api.requestOperations(api.openApi3.paths.getValue("/alias"))
        assertThat(aliases).hasSize(2)
        val nullableWrapper =
            aliases[1]
                .second.requestBody.contentMediaTypes.values
                .single()
                .schema
        assertThat(nullableWrapper.effectiveRequestSchema()).isEqualTo(nullableWrapper)
        assertThat(nullableWrapper.isNullable).isTrue()
        val variants = api.requestOperations(api.openApi3.paths.getValue("/distinct"))
        assertThat(GeneratorUtils.functionName(variants[1].second, "/distinct", "post"))
            .isEqualTo("createDetailsTextJsonExtra")
    }

    @Test
    fun `fallback controller names avoid collisions with an existing operation`() {
        val document = mapper.readTree(readTextResource("/examples/multipleRequestMediaTypes/api.yaml"))
        (document.at("/paths/~1alias/post/requestBody/content/text~1json/schema") as ObjectNode).put("nullable", true)
        (document.at("/paths/~1inline/post") as ObjectNode).put("operationId", "postTextJson")
        val api = SourceApi(document.toString())
        val variants = api.requestOperations(api.openApi3.paths.getValue("/alias"))
        assertThat(
            com.cjbooms.fabrikt.generators.controller.ControllerGeneratorUtils
                .methodName(variants[1].second, "post", false),
        ).isEqualTo("postTextJsonExtra")
    }

    @ParameterizedTest
    @EnumSource(ClientCodeGenTargetType::class, names = ["OK_HTTP", "OPEN_FEIGN", "SPRING_HTTP_INTERFACE"])
    fun `typed response functions retain each request representation`(target: ClientCodeGenTargetType) {
        val document = mapper.readTree(readTextResource("/examples/multipleRequestMediaTypes/api.yaml"))
        val responses = document.at("/paths/~1distinct/post/responses") as ObjectNode
        val response = responses.removeAll().putObject("200").put("description", "Success")
        response.set<com.fasterxml.jackson.databind.JsonNode>(
            "content",
            mapper.readTree(
                """
                {
                  "application/json": {"schema": {"${'$'}ref": "#/components/schemas/Requests.DetailsRequest"}},
                  "application/problem+json": {"schema": {"${'$'}ref": "#/components/schemas/CountRequest"}}
                }
                """.trimIndent(),
            ),
        )
        val options = setOf(ClientCodeGenOptionType.RESPONSE_MEDIA_TYPE_FUNCTIONS, ClientCodeGenOptionType.RESILIENCE4J)
        MutableSettings.updateSettings(genTypes = setOf(CodeGenerationType.CLIENT), clientTarget = target, clientOptions = options)
        val api = SourceApi(document.toString())
        val packages = Packages("examples.multipleRequestMediaTypes")
        val generator =
            when (target) {
                ClientCodeGenTargetType.OK_HTTP -> OkHttpClientGenerator(packages, api, Paths.get("src/main/kotlin"))
                ClientCodeGenTargetType.OPEN_FEIGN -> OpenFeignInterfaceGenerator(packages, api)
                ClientCodeGenTargetType.SPRING_HTTP_INTERFACE -> SpringHttpInterfaceGenerator(packages, api)
                else -> error("Unsupported target")
            }
        val expectedRequestTypes = mapOf("createDetails" to "RequestsDetailsRequest", "createDetailsTextJson" to "CountRequest")
        val expectedResponseTypes = mapOf("Json" to "RequestsDetailsRequest", "ProblemJson" to "CountRequest")
        val clients = generator.generate(options).clients.filter { it.spec.name in setOf("DistinctClient", "DistinctService") }
        assertThat(clients).isNotEmpty()
        clients.forEach { client ->
            val functions = client.spec.funSpecs.associateBy { it.name }
            expectedRequestTypes.forEach { (baseName, requestType) ->
                expectedResponseTypes.forEach { (suffix, responseType) ->
                    val function = functions.getValue(baseName + suffix)
                    assertThat(function.parameters.map { it.type.toString().removeSuffix("?") })
                        .contains("${packages.models}.$requestType")
                    assertThat(function.returnType.toString()).contains("${packages.models}.$responseType")
                }
            }
        }
    }
}
