package com.cjbooms.fabrikt.models.kotlinx

import com.example.models.Car
import com.example.models.ChildActionA
import com.example.models.ChildActionB
import com.example.models.ChildActionsAll
import com.example.models.ParentAction
import com.example.models.Truck
import com.example.models.Vehicle
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Covers allOf-based polymorphism where the parent schema carries the discriminator
 * (https://github.com/fabrikt-io/fabrikt/issues/627). The child classes must not redeclare the
 * discriminator property, since kotlinx serialization emits it as the class discriminator.
 */
class KotlinxSerializationAllOfPolymorphicTest {

    @Test
    fun `must serialize ChildActionA with discriminator when serialized as ParentAction`() {
        val action: ParentAction = ChildActionA(fieldA = "hello")
        val json = Json.encodeToString(action)

        assertThat(json).isEqualTo("""{"actionType":"CHILD_A","fieldA":"hello"}""")
    }

    @Test
    fun `must round-trip a property inherited from the supertype`() {
        val action: ParentAction = ChildActionA(createdBy = "someone", fieldA = "hello")
        val json = Json.encodeToString(action)

        assertThat(json).isEqualTo("""{"actionType":"CHILD_A","createdBy":"someone","fieldA":"hello"}""")
        assertThat(Json.decodeFromString<ParentAction>(json))
            .isEqualTo(ChildActionA(createdBy = "someone", fieldA = "hello"))
    }

    @Test
    fun `must serialize ChildActionB with discriminator when serialized as ParentAction`() {
        val action: ParentAction = ChildActionB(fieldB = 42)
        val json = Json.encodeToString(action)

        assertThat(json).isEqualTo("""{"actionType":"CHILD_B","fieldB":42}""")
    }

    @Test
    fun `must deserialize ParentAction into ChildActionA`() {
        val json = """{"actionType":"CHILD_A","fieldA":"hello"}"""
        val action: ParentAction = Json.decodeFromString(json)

        assertThat(action).isEqualTo(ChildActionA(fieldA = "hello"))
    }

    @Test
    fun `must deserialize ParentAction into ChildActionB`() {
        val json = """{"actionType":"CHILD_B","fieldB":42}"""
        val action: ParentAction = Json.decodeFromString(json)

        assertThat(action).isEqualTo(ChildActionB(fieldB = 42))
    }

    @Test
    fun `must round-trip the children through the oneOf sealed interface`() {
        val actions: List<ChildActionsAll> = listOf(ChildActionA(fieldA = "hello"), ChildActionB(fieldB = 42))
        val json = Json.encodeToString(actions)

        assertThat(json)
            .isEqualTo("""[{"actionType":"CHILD_A","fieldA":"hello"},{"actionType":"CHILD_B","fieldB":42}]""")
        assertThat(Json.decodeFromString<List<ChildActionsAll>>(json)).isEqualTo(actions)
    }

    @Test
    fun `must default the discriminator value to the schema name when the mapping is omitted`() {
        val vehicle: Vehicle = Car(wheels = 4, trunkSize = "large")
        val json = Json.encodeToString(vehicle)

        assertThat(json).isEqualTo("""{"vehicleType":"Car","wheels":4,"trunkSize":"large"}""")
        assertThat(Json.decodeFromString<Vehicle>(json)).isEqualTo(vehicle)
    }

    @Test
    fun `must round-trip a required inherited property`() {
        val json = """{"vehicleType":"Truck","wheels":6,"payload":3500}"""
        val vehicle: Vehicle = Json.decodeFromString(json)

        assertThat(vehicle).isEqualTo(Truck(wheels = 6, payload = 3500))
        assertThat(Json.encodeToString(vehicle)).isEqualTo(json)
    }
}
