package com.cjbooms.fabrikt.models.kotlinx

import com.example.jsonelement.models.Example
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * End-to-end verification of the ANY_AS_JSONELEMENT type override (fabrikt issue #633)
 * with kotlinx.serialization. Untyped (any) schemas are generated as
 * `kotlinx.serialization.json.JsonElement`, allowing dynamic JSON to round-trip without
 * custom serializers.
 */
class KotlinxSerializationAnyAsJsonElementTest {

    @Test
    fun `serializes dynamic JSON of mixed shapes`() {
        val example = Example(
            anyValue = buildJsonObject {
                put("name", "fabrikt")
                put("stars", 1000)
            },
            optionalAnyValue = JsonPrimitive(42),
            anyList = listOf(
                JsonPrimitive("text"),
                JsonPrimitive(true),
                buildJsonArray { add(1) },
            ),
        )

        val json = Json.encodeToString(example)

        assertThat(json).isEqualTo(
            """{"anyValue":{"name":"fabrikt","stars":1000},"optionalAnyValue":42,"anyList":["text",true,[1]]}"""
        )
    }

    @Test
    fun `deserializes arbitrary JSON into JsonElement properties`() {
        val example = Json.decodeFromString<Example>(
            """{"anyValue":{"nested":{"deep":[1,2,3]}},"anyList":[{"id":7}]}"""
        )

        assertThat(example.anyValue).isInstanceOf(JsonObject::class.java)
        assertThat(
            example.anyValue.jsonObject["nested"]!!.jsonObject["deep"]!!.jsonArray.map { it.jsonPrimitive.int }
        ).containsExactly(1, 2, 3)
        assertThat(example.optionalAnyValue).isNull()
        assertThat(example.anyList!!.single().jsonObject["id"]!!.jsonPrimitive.int).isEqualTo(7)
    }

    @Test
    fun `deserializes explicit null into JsonNull for a required any property`() {
        val example = Json.decodeFromString<Example>("""{"anyValue":null}""")

        assertThat(example.anyValue).isEqualTo(JsonNull)
    }

    @Test
    fun `round trip preserves dynamic JSON structure`() {
        val original = Json.decodeFromString<Example>(
            """{"anyValue":{"a":[1,"two",null,{"b":false}]},"optionalAnyValue":"plain","anyList":[]}"""
        )

        val roundTripped = Json.decodeFromString<Example>(Json.encodeToString(original))

        assertThat(roundTripped).isEqualTo(original)
    }

    @Test
    fun `handles the other untyped shapes as JsonElement`() {
        val example = Json.decodeFromString<Example>(
            """
            {
              "anyValue": 1,
              "anyListNoItems": [1, "two", {"three": 3}],
              "nullableAny": null,
              "refAny": {"anything": ["goes"]},
              "oneOfAny": "either-a-string-or-an-int",
              "untypedObject": {"free": {"form": [1, 2]}}
            }
            """.trimIndent()
        )

        assertThat(example.anyListNoItems!!.map { it is JsonPrimitive })
            .containsExactly(true, true, false)
        assertThat(example.anyListNoItems!![2]).isInstanceOf(JsonObject::class.java)
        // explicit null decodes to Kotlin null on a nullable JsonElement?, unlike the required
        // non-nullable JsonElement above where it decodes to JsonNull
        assertThat(example.nullableAny).isNull()
        assertThat(example.refAny!!.jsonObject["anything"]!!.jsonArray.single().jsonPrimitive.content)
            .isEqualTo("goes")
        assertThat(example.oneOfAny!!.jsonPrimitive.content).isEqualTo("either-a-string-or-an-int")
        assertThat(example.untypedObject!!["free"]!!.jsonObject["form"]!!.jsonArray.map { it.jsonPrimitive.int })
            .containsExactly(1, 2)

        val roundTripped = Json.decodeFromString<Example>(Json.encodeToString(example))
        assertThat(roundTripped).isEqualTo(example)
    }
}
