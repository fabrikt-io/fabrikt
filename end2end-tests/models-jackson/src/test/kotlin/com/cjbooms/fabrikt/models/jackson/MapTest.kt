package com.cjbooms.fabrikt.models.jackson

import com.cjbooms.fabrikt.models.jackson.Helpers.mapper
import com.example.models.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class MapTest {
    private val objectMapper = mapper()
    private val writer = objectMapper.writerWithDefaultPrettyPrinter()

    @Test
    fun `subtypes share the parent's additional properties map and closed subtypes stay strict`() {
        val mapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
        val json = """{"id":"1","kind":"child","label":"Alice","extra":null,"nested":{"items":[1,true]}}"""
        val child = mapper.readValue(json, com.example.inheritedadditionalproperties.models.OrderX::class.java)
            as com.example.inheritedadditionalproperties.models.OrderChild
        assertThat(child.properties).containsKey("extra").containsKey("nested").doesNotContainKey("label")
        assertThat(mapper.readTree(mapper.writeValueAsString(child.copy()))).isEqualTo(mapper.readTree(json))

        val typedJson = """{"id":"2","kind":"typed","label":"Bob","extra":"retained"}"""
        val typed = mapper.readValue(typedJson, com.example.inheritedadditionalproperties.models.OrderX::class.java)
            as com.example.inheritedadditionalproperties.models.OrderTypedChild
        assertThat(typed.properties).containsEntry("extra", "retained")
        assertThat(mapper.readTree(mapper.writeValueAsString(typed.copy()))).isEqualTo(mapper.readTree(typedJson))
        assertThatThrownBy {
            mapper.readValue(
                """{"id":"3","kind":"sibling","count":3,"extra":1}""",
                com.example.inheritedadditionalproperties.models.OrderX::class.java,
            )
        }.isInstanceOf(com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException::class.java)
    }

    @Test
    fun `typed parent map widens across allOf hierarchy when descendant declares different type`() {
        val mapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
        val intJson = """{"id":"1","kind":"intChild","count":5,"extraInt":42,"extraString":"hello"}"""
        val intChild = mapper.readValue(intJson, com.example.inheritedadditionalproperties.models.WidenedParent::class.java)
            as com.example.inheritedadditionalproperties.models.WidenedIntChild
        assertThat(intChild.properties).containsEntry("extraInt", 42).containsEntry("extraString", "hello")
        assertThat(mapper.readTree(mapper.writeValueAsString(intChild.copy()))).isEqualTo(mapper.readTree(intJson))

        val plainJson = """{"id":"2","kind":"plainSibling","name":"Bob","extra":true}"""
        val plainSibling = mapper.readValue(plainJson, com.example.inheritedadditionalproperties.models.WidenedParent::class.java)
            as com.example.inheritedadditionalproperties.models.WidenedPlainChild
        assertThat(plainSibling.properties).containsEntry("extra", true)
        assertThat(mapper.readTree(mapper.writeValueAsString(plainSibling.copy()))).isEqualTo(mapper.readTree(plainJson))
    }

    @Test
    fun `typed parent map stays typed when descendants do not disagree`() {
        val mapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
        val plainJson = """{"id":"1","kind":"plainChild","name":"Alice","extraString":"hello"}"""
        val plainChild = mapper.readValue(plainJson, com.example.inheritedadditionalproperties.models.TypedParent::class.java)
            as com.example.inheritedadditionalproperties.models.TypedPlainChild
        assertThat(plainChild.properties).containsEntry("extraString", "hello")
        assertThat(mapper.readTree(mapper.writeValueAsString(plainChild.copy()))).isEqualTo(mapper.readTree(plainJson))

        val stringJson = """{"id":"2","kind":"stringChild","label":"Bob","otherString":"world"}"""
        val stringChild = mapper.readValue(stringJson, com.example.inheritedadditionalproperties.models.TypedParent::class.java)
            as com.example.inheritedadditionalproperties.models.TypedStringChild
        assertThat(stringChild.properties).containsEntry("otherString", "world")
        assertThat(mapper.readTree(mapper.writeValueAsString(stringChild.copy()))).isEqualTo(mapper.readTree(stringJson))
    }

    @Test
    fun `intermediate without discriminator shares root map`() {
        val mapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
        val leafJson = """{"id":"1","kind":"leaf","leafProp":"lp","intermediateProp":"ip","extra":"val"}"""
        val leaf = mapper.readValue(leafJson, com.example.inheritedadditionalproperties.models.RootOrder::class.java)
            as com.example.inheritedadditionalproperties.models.LeafOrder
        assertThat(leaf.properties).containsEntry("extra", "val")
        assertThat(mapper.readTree(mapper.writeValueAsString(leaf.copy()))).isEqualTo(mapper.readTree(leafJson))
    }

    @Test
    fun `unconfigured parent leaves map on child that declares it`() {
        val mapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
        val explicitJson = """{"id":"1","kind":"explicit","label":"exp","extra":123}"""
        val explicit = mapper.readValue(explicitJson, com.example.inheritedadditionalproperties.models.UnconfiguredParent::class.java)
            as com.example.inheritedadditionalproperties.models.ExplicitChild
        assertThat(explicit.properties).containsEntry("extra", 123)
        assertThat(mapper.readTree(mapper.writeValueAsString(explicit.copy()))).isEqualTo(mapper.readTree(explicitJson))

        assertThatThrownBy {
            mapper.readValue(
                """{"id":"2","kind":"quiet","name":"q","extra":123}""",
                com.example.inheritedadditionalproperties.models.UnconfiguredParent::class.java,
            )
        }.isInstanceOf(com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException::class.java)
    }

    @Test
    fun `must serialize string map`() {
        val wrapper = MapHolder(stringMap = mapOf("Key 1" to "Value 1", "Key 2" to "Value 2"))
        val result = writer.writeValueAsString(wrapper)

        val expected = javaClass.getResource("/maps/string_map.json")!!.readText()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `must deserialize string map`() {
        val result = readMapHolder("string_map")

        val expected = MapHolder(stringMap = mapOf("Key 1" to "Value 1", "Key 2" to "Value 2"))
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `must serialize typed object map`() {
        val wrapper = MapHolder(typedObjectMap = mapOf("Key 1" to TypedObjectMapValue(1, "something")))
        val result = writer.writeValueAsString(wrapper)

        val expected = javaClass.getResource("/maps/typed_object_map.json")!!.readText()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `must deserialize typed object map`() {
        val result = readMapHolder("typed_object_map")

        val expected = MapHolder(typedObjectMap = mapOf("Key 1" to TypedObjectMapValue(1, "something")))
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `must serialize object map`() {
        val wrapper = MapHolder(objectMap = mapOf("Key 1" to mapOf("Key 1.1" to "something")))
        val result = writer.writeValueAsString(wrapper)

        val expected = javaClass.getResource("/maps/object_map.json")!!.readText()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `must deserialize object map`() {
        val result = readMapHolder("object_map")

        val expected = MapHolder(objectMap = mapOf("Key 1" to mapOf("Key 1.1" to "something")))
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `must serialize inlined string map`() {
        val wrapper = MapHolder(inlinedStringMap = mapOf("Key 1" to "something"))
        val result = writer.writeValueAsString(wrapper)

        val expected = javaClass.getResource("/maps/inlined_string_map.json")!!.readText()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `must deserialize inlined string map`() {
        val result = readMapHolder("inlined_string_map")

        val expected = MapHolder(inlinedStringMap = mapOf("Key 1" to "something"))
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `must serialize inlined object map`() {
        val wrapper = MapHolder(inlinedObjectMap = mapOf("Key 1" to mapOf("Key 1.1" to 1)))
        val result = writer.writeValueAsString(wrapper)

        val expected = javaClass.getResource("/maps/inlined_object_map.json")!!.readText()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `must deserialize inlined object map`() {
        val result = readMapHolder("inlined_object_map")

        val expected = MapHolder(inlinedObjectMap = mapOf("Key 1" to mapOf("Key 1.1" to 1)))
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `must serialize inlined unknown map`() {
        val wrapper = MapHolder(inlinedUnknownMap = mapOf("Key 1" to 1))
        val result = writer.writeValueAsString(wrapper)

        val expected = javaClass.getResource("/maps/inlined_unknown_map.json")!!.readText()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `must deserialize inlined unknown map`() {
        val result = readMapHolder("inlined_unknown_map")

        val expected = MapHolder(inlinedUnknownMap = mapOf("Key 1" to 1))
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `must serialize complex object with typed map`() {
        val wrapper = MapHolder(complexObjectWithTypedMap = ComplexObjectWithTypedMap("text", 1))
        val result = writer.writeValueAsString(wrapper)

        val expected = javaClass.getResource("/maps/complex_object_with_types_map.json")!!.readText()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `must deserialize complex object with typed map`() {
        val result = readMapHolder("complex_object_with_types_map")

        val expected = MapHolder(complexObjectWithTypedMap = ComplexObjectWithTypedMap("text", 1))
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `must serialize inlined object with untyped map`() {
        val wrapper = MapHolder(inlinedComplexObjectWithUntypedMap = MapHolderInlinedComplexObjectWithUntypedMap("key", 1))
        val result = writer.writeValueAsString(wrapper)

        val expected = javaClass.getResource("/maps/inlined_object_with_untyped_map.json")!!.readText()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `must deserialize inlined object with untyped map`() {
        val result = readMapHolder("inlined_object_with_untyped_map")

        val expected = MapHolder(inlinedComplexObjectWithUntypedMap = MapHolderInlinedComplexObjectWithUntypedMap("key", 1))
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `must serialize inlined object with typed map`() {
        val wrapper = MapHolder(inlinedComplexObjectWithTypedMap = MapHolderInlinedComplexObjectWithTypedMap("key", 1))
        val result = writer.writeValueAsString(wrapper)

        val expected = javaClass.getResource("/maps/inlined_object_with_typed_map.json")!!.readText()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `must deserialize inlined object with typed map`() {
        val result = readMapHolder("inlined_object_with_typed_map")

        val expected = MapHolder(inlinedComplexObjectWithTypedMap = MapHolderInlinedComplexObjectWithTypedMap("key", 1))
        assertThat(result).isEqualTo(expected)
    }

    private fun readMapHolder(name: String): MapHolder {
        val jsonString = javaClass.getResource("/maps/$name.json")!!.readText()

        return objectMapper.readValue(jsonString, MapHolder::class.java)
    }
}