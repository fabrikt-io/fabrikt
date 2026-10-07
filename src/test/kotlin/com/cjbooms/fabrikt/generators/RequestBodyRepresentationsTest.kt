package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.CodeGenerationType
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
}
