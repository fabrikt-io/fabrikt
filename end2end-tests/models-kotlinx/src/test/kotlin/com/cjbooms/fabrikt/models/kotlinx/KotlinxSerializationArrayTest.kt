package com.cjbooms.fabrikt.models.kotlinx

import com.example.models.ArrayHolder
import com.example.models.BasicObject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KotlinxSerializationArrayTest {

    private val json = Json { prettyPrint = true }

    @Test
    fun `must serialize uniqueItems = true array`() {
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
        val result = json.encodeToString(ArrayHolder.serializer(), wrapper)

        val expected = javaClass.getResource("/arrays/uniqueItems_true.json")!!.readText()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `must deserialize uniqueItems=true object array`() {
        val jsonString = javaClass.getResource("/arrays/uniqueItems_true.json")!!.readText()
        val result = json.decodeFromString(ArrayHolder.serializer(), jsonString)

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
    fun `deserialized then serialized uniqueItems=true object array should be the same`() {
        val jsonString = javaClass.getResource("/arrays/uniqueItems_true.json")!!.readText()
        val parsed = json.parseToJsonElement(jsonString)
        val deserialised = json.decodeFromJsonElement(ArrayHolder.serializer(), parsed)
        val reserialized = json.encodeToJsonElement(deserialised)

        assertThat(reserialized).isEqualTo(parsed)

    }

    @Test
    fun `must deserialize uniqueItems=true int array`() {
        val jsonString = javaClass.getResource("/arrays/uniqueItems_true_ints.json")!!.readText()
        val result = json.decodeFromString(ArrayHolder.serializer(), jsonString)

        val expected = ArrayHolder(
            uniqueInts = linkedSetOf(6,5,4,3,2,1)
        )
        assertThat(result).isEqualTo(expected)
        assertThat(result.uniqueObjects?.toList()).isEqualTo(expected.uniqueObjects?.toList())
    }

    @Test
    fun `must deserialize uniqueItems=true array that includes duplicates`() {
        val jsonString = javaClass.getResource("/arrays/uniqueItems_true_with_duplicates.json")!!.readText()
        val result = json.decodeFromString(ArrayHolder.serializer(), jsonString)

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
        val result = json.encodeToString(ArrayHolder.serializer(), wrapper)

        val expected = javaClass.getResource("/arrays/uniqueItems_false.json")!!.readText()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `must deserialize non unique objects`() {
        val jsonString = javaClass.getResource("/arrays/uniqueItems_false.json")!!.readText()
        val result = json.decodeFromString(ArrayHolder.serializer(), jsonString)

        val expected = ArrayHolder(
            nonUniqueObjects = listOf(
                BasicObject(one = "first"),
                BasicObject(one = "second")
            )
        )
        assertThat(result).isEqualTo(expected)
    }
}

