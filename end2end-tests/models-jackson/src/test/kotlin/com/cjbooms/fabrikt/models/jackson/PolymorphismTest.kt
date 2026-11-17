package com.cjbooms.fabrikt.models.jackson

import com.cjbooms.fabrikt.models.jackson.Helpers.mapper
import com.example.models.PolymorphicSuperType
import com.example.models.PolymorphicTypeOne
import com.example.models.PolymorphicTypeTwo
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.node.ObjectNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PolymorphismTest {
    private val objectMapper = mapper()
    private val writer = objectMapper.writerWithDefaultPrettyPrinter()

    @Test
    fun `PolymorphicTypeOne retains declared fields and untyped additional properties`() {
        val mapper = objectMapper.copy().setSerializationInclusion(JsonInclude.Include.ALWAYS)
        val json = mapper.readTree(javaClass.getResource("/polymorphic/polymorphic_type_one.json")!!.readText())
            .deepCopy<ObjectNode>()
            .put("extra", "retained")
            .putNull("nullable_extra")
        val result = mapper.treeToValue(json, PolymorphicSuperType::class.java) as PolymorphicTypeOne
        assertThat(result.childOneName).isEqualTo("child on name")
        assertThat(result.properties).containsEntry("extra", "retained").containsEntry("nullable_extra", null)
            .doesNotContainKeys("first_name", "last_name", "child_one_name", "generation")
        assertThat(mapper.readTree(mapper.writeValueAsString(result.copy()))).isEqualTo(json)
    }

    @Test
    fun `PolymorphicTypeTwo retains declared fields and typed additional properties`() {
        val mapper = objectMapper.copy().setSerializationInclusion(JsonInclude.Include.ALWAYS)
        val json = mapper.readTree(javaClass.getResource("/polymorphic/polymorphic_type_two.json")!!.readText())
            .deepCopy<ObjectNode>()
            .put("extra", "retained")
            .putNull("nullable_extra")
        val result = mapper.treeToValue(json, PolymorphicSuperType::class.java) as PolymorphicTypeTwo
        assertThat(result.childTwoAge).isEqualTo(1)
        assertThat(result.properties).containsEntry("extra", "retained").containsEntry("nullable_extra", null)
            .doesNotContainKeys("first_name", "last_name", "child_two_age", "generation")
        assertThat(mapper.readTree(mapper.writeValueAsString(result.copy()))).isEqualTo(json)
    }

    @Test
    fun `must serialize PolymorphicTypeOne`() {
        val obj = PolymorphicTypeOne(firstName = "first name", lastName = "last name", childOneName = "child on name")
        val result = writer.writeValueAsString(obj)

        val expected = javaClass.getResource("/polymorphic/polymorphic_type_one.json")!!.readText()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `must deserialize PolymorphicTypeOne`() {
        val jsonString = javaClass.getResource("/polymorphic/polymorphic_type_one.json")!!.readText()

        val result = objectMapper.readValue(jsonString, PolymorphicSuperType::class.java)
        assertThat(result).isEqualTo(PolymorphicTypeOne(firstName = "first name", lastName = "last name", childOneName = "child on name"))
    }

    @Test
    fun `must serialize PolymorphicTypeTwo`() {
        val obj = PolymorphicTypeTwo(firstName = "first name", lastName = "last name", childTwoAge = 1)
        val result = writer.writeValueAsString(obj)

        val expected = javaClass.getResource("/polymorphic/polymorphic_type_two.json")!!.readText()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `must deserialize PolymorphicTypeTwo`() {
        val jsonString = javaClass.getResource("/polymorphic/polymorphic_type_two.json")!!.readText()

        val result = objectMapper.readValue(jsonString, PolymorphicSuperType::class.java)
        assertThat(result).isEqualTo(PolymorphicTypeTwo(firstName = "first name", lastName = "last name", childTwoAge = 1))
    }
}
