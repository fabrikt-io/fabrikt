package com.cjbooms.fabrikt.models.jackson

import com.cjbooms.fabrikt.models.jackson.Helpers.mapper
import com.example.models.*
import com.fasterxml.jackson.databind.JsonNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ArrayTest {
    private val objectMapper = mapper()
    private val writer = objectMapper.writerWithDefaultPrettyPrinter()

    @Test
    fun `must serialize uniqueItems = true object array`() {
        val wrapper = ArrayHolder(
            uniqueObjects = linkedSetOf(
                BasicObject(one = "first"),
                BasicObject(one = "second"),
                BasicObject(one = "third"),
                BasicObject(one = "fourth"),
                BasicObject(one = "fifth"),
                BasicObject(one = "sixth")
            )
        )
        val result = writer.writeValueAsString(wrapper)

        val expected = javaClass.getResource("/arrays/uniqueItems_true.json")!!.readText()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `must serialize uniqueItems = true int array`() {
        val wrapper = ArrayHolder(
            uniqueInts = linkedSetOf(6,5,4,3,2,1)
        )
        val result = writer.writeValueAsString(wrapper)

        val expected = javaClass.getResource("/arrays/uniqueItems_true_ints.json")!!.readText()
        assertThat(result).isEqualTo(expected)
    }


    @Test
    fun `must deserialize uniqueItems=true object array`() {
        val result = readArrayHolder("uniqueItems_true")

        val expected = ArrayHolder(
            uniqueObjects = linkedSetOf(
                BasicObject(one = "first"),
                BasicObject(one = "second"),
                BasicObject(one = "third"),
                BasicObject(one = "fourth"),
                BasicObject(one = "fifth"),
                BasicObject(one = "sixth")
            )
        )
        assertThat(result).isEqualTo(expected)
        assertThat(result.uniqueObjects?.toList()).isEqualTo(expected.uniqueObjects?.toList())
    }

    @Test
    fun `must deserialize uniqueItems=true int array in same order`() {
        val result = readArrayHolder("uniqueItems_true_ints")

        val expected = ArrayHolder(
            uniqueInts = linkedSetOf(6,5,4,3,2,1)
        )
        assertThat(result).isEqualTo(expected)
        assertThat(result.uniqueInts?.toList()).isEqualTo(expected.uniqueInts?.toList())
    }

    @Test
    fun `deserialized then serialized uniqueItems=true object array should be the same`() {
        val jsonString = javaClass.getResource("/arrays/uniqueItems_true.json")!!.readText()

        val parsed = objectMapper.readTree(jsonString)!!

        val deserialized = objectMapper.convertValue(parsed, ArrayHolder::class.java)!!
        val reserialized: JsonNode = objectMapper.valueToTree(deserialized)

        assertThat(reserialized).isEqualTo(parsed)

    }


    @Test
    fun `must deserialize uniqueItems=true object array that includes duplicates`() {
        val result = readArrayHolder("uniqueItems_true_with_duplicates")

        val expected = ArrayHolder(
            uniqueObjects = linkedSetOf(
                BasicObject(one = "first"),
                BasicObject(one = "second"),
                BasicObject(one = "third")
            )
        )
        assertThat(result).isEqualTo(expected)
        assertThat(result.uniqueObjects?.toList()).isEqualTo(expected.uniqueObjects?.toList())
    }


    @Test
    fun `must serialize non unique objects`() {
        val wrapper = ArrayHolder(
            nonUniqueObjects = listOf(
                BasicObject(one = "first"),
                BasicObject(one = "second")
            )
        )
        val result = writer.writeValueAsString(wrapper)

        val expected = javaClass.getResource("/arrays/uniqueItems_false.json")!!.readText()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `must deserialize non unique objects`() {
        val result = readArrayHolder("uniqueItems_false")

        val expected = ArrayHolder(
            nonUniqueObjects = listOf(
                BasicObject(one = "first"),
                BasicObject(one = "second")
            )
        )
        assertThat(result).isEqualTo(expected)
    }

    private fun readArrayHolder(name: String): ArrayHolder {
        val jsonString = javaClass.getResource("/arrays/$name.json")!!.readText()

        return objectMapper.readValue(jsonString, ArrayHolder::class.java)
    }
}

