package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.validation.Valid
import javax.validation.constraints.NotNull

class ArrayModelSerializationTest {

    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `ContainsUniqueArrayRef serializes and deserializes correctly`() {

        data class ContainsUniqueArrayRef(
            @param:JsonProperty("weight_on_mars")
            @get:JsonProperty("weight_on_mars")
            @get:NotNull
            @get:Valid
            val weightOnMars: Set<Int>,
        )

        val originalJson = """{"weight_on_mars":[1,2,3,5,6,4]}"""

        val deserializedObject = objectMapper.readValue(originalJson, ContainsUniqueArrayRef::class.java)

        val serializedJson = objectMapper.writeValueAsString(deserializedObject)

        val originalJsonNode = objectMapper.readTree(originalJson)
        val serializedJsonNode = objectMapper.readTree(serializedJson)

        // FAILS because the order of elements in a Set is not guaranteed (serializedJsonNode corresponds to: {"weight_on_mars":[1,2,3,4,5,6]})
        assertThat(serializedJsonNode).isEqualTo(originalJsonNode)
    }
}

