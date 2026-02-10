package com.cjbooms.fabrikt.models.jackson

import com.cjbooms.fabrikt.models.jackson.Helpers.mapper
import com.example.oneof.models.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Test for nested discriminated oneOf with Jackson serialization/deserialization.
 * Tests the scenario from GitHub issue #461 where StateB is a nested oneOf within State.
 */
class NestedOneOfTest {
    private val objectMapper = mapper()
    private val writer = objectMapper.writerWithDefaultPrettyPrinter()

    @Test
    fun `must serialize StateA`() {
        val obj = StateA(status = Status.A)
        val result = writer.writeValueAsString(obj)
        
        val expected = javaClass.getResource("/nested_oneof/state_a.json")!!.readText()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `must deserialize StateA`() {
        val jsonString = javaClass.getResource("/nested_oneof/state_a.json")!!.readText()
        
        val result = objectMapper.readValue(jsonString, State::class.java)
        assertThat(result).isEqualTo(StateA(status = Status.A))
    }

    @Test
    fun `must serialize StateB1 - nested oneOf`() {
        val obj = StateB1(mode = "mode1", status = Status.B1)
        val result = writer.writeValueAsString(obj)
        
        val expected = javaClass.getResource("/nested_oneof/state_b1.json")!!.readText()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `must deserialize StateB1 - nested oneOf`() {
        val jsonString = javaClass.getResource("/nested_oneof/state_b1.json")!!.readText()
        
        val result = objectMapper.readValue(jsonString, State::class.java)
        assertThat(result).isEqualTo(StateB1(mode = "mode1", status = Status.B1))
        assertThat(result).isInstanceOf(StateB::class.java)
    }

    @Test
    fun `must deserialize StateB1 as StateB - nested oneOf interface`() {
        val jsonString = javaClass.getResource("/nested_oneof/state_b1.json")!!.readText()
        
        val result = objectMapper.readValue(jsonString, StateB::class.java)
        assertThat(result).isEqualTo(StateB1(mode = "mode1", status = Status.B1))
    }

    @Test
    fun `must serialize StateB2 - nested oneOf`() {
        val obj = StateB2(mode = "mode3", status = Status.B2)
        val result = writer.writeValueAsString(obj)
        
        val expected = javaClass.getResource("/nested_oneof/state_b2.json")!!.readText()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `must deserialize StateB2 - nested oneOf`() {
        val jsonString = javaClass.getResource("/nested_oneof/state_b2.json")!!.readText()
        
        val result = objectMapper.readValue(jsonString, State::class.java)
        assertThat(result).isEqualTo(StateB2(mode = "mode3", status = Status.B2))
        assertThat(result).isInstanceOf(StateB::class.java)
    }

    @Test
    fun `must serialize SomeObj with StateB1 - container with nested oneOf`() {
        val obj = SomeObj(state = StateB1(mode = "mode1", status = Status.B1))
        val result = writer.writeValueAsString(obj)
        
        val expected = javaClass.getResource("/nested_oneof/some_obj_with_state_b1.json")!!.readText()
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `must deserialize SomeObj with StateB1 - container with nested oneOf`() {
        val jsonString = javaClass.getResource("/nested_oneof/some_obj_with_state_b1.json")!!.readText()
        
        val result = objectMapper.readValue(jsonString, SomeObj::class.java)
        assertThat(result.state).isEqualTo(StateB1(mode = "mode1", status = Status.B1))
        assertThat(result.state).isInstanceOf(StateB::class.java)
    }

    @Test
    fun `auto-flatten - must deserialize YTest1 from Test interface`() {
        // Key test: Test auto-flattened to include YTest1 directly
        // JSON uses discriminator value "YTest1" which maps to concrete type
        val json = """{"type": "YTest1", "alt": "test"}"""
        
        val result = objectMapper.readValue(json, com.example.oneof.models.Test::class.java)
        assertThat(result).isInstanceOf(YTest1::class.java)
        assertThat(result).isInstanceOf(YTest::class.java)
    }
    
    @Test
    fun `auto-flatten - SomeRequest has List of Test not Any`() {
        val json = """{"id": 123, "events": [{"type": "YTest2", "alt": "test"}]}"""
        
        val result = objectMapper.readValue(json, SomeRequest::class.java)
        assertThat(result.events).isNotNull
        assertThat(result.events).hasSize(1)
        assertThat(result.events!![0]).isInstanceOf(YTest2::class.java)
        assertThat(result.events!![0]).isInstanceOf(YTest::class.java)
    }
}
