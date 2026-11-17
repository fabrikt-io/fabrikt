package com.cjbooms.fabrikt.models.kotlinx

import com.example.additionalproperties.models.AdditionalPropertiesSerializer
import com.example.additionalproperties.models.Result
import com.example.additionalproperties.models.ResultWithImplicitAdditionalProperties
import com.example.additionalproperties.models.ResultWithoutAdditionalProperties
import com.example.additionalpropertiespolicy.models.ResultWithImplicitAdditionalProperties as PolicyImplicitResult
import com.example.allofparentorder.models.ExplicitChild
import com.example.allofparentorder.models.LeafOrder
import com.example.allofparentorder.models.OrderChild
import com.example.allofparentorder.models.OrderSibling
import com.example.allofparentorder.models.OrderTypedChild
import com.example.allofparentorder.models.OrderX
import com.example.allofparentorder.models.QuietSibling
import com.example.allofparentorder.models.RootOrder
import com.example.allofparentorder.models.UnconfiguredParent
import com.example.allofparentorder.models.WidenedIntChild
import com.example.allofparentorder.models.WidenedParent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.modules.plus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@OptIn(ExperimentalSerializationApi::class)
class KotlinxSerializationAdditionalPropertiesTest {
    private val json = Json

    @Test
    fun `captures extras on an open class and writes them back`() {
        val input = """{"message":"m","a":1,"b":{"c":[true,null]},"d":null}"""
        val result = json.decodeFromString<Result>(input)

        assertThat(result.message).isEqualTo("m")
        assertThat(result.properties.keys).containsExactlyInAnyOrder("a", "b", "d")
        assertThat(result.properties["d"]).isEqualTo(JsonNull)

        val encoded = json.encodeToString(result)
        assertThat(json.parseToJsonElement(encoded)).isEqualTo(json.parseToJsonElement(input))
    }

    @Test
    fun `complete serialization cycle for newly constructed model with extras`() {
        val original =
            Result(
                message = "initial",
                properties =
                    mutableMapOf(
                        "extra_bool" to JsonPrimitive(true),
                        "extra_num" to JsonPrimitive(123),
                    ),
            )
        val serialized = json.encodeToString(original)
        val deserialized = json.decodeFromString<Result>(serialized)

        assertThat(deserialized.message).isEqualTo("initial")
        assertThat(deserialized.properties.keys).containsExactlyInAnyOrder("extra_bool", "extra_num")
        assertThat(deserialized.properties["extra_bool"]).isEqualTo(JsonPrimitive(true))
        assertThat(deserialized.properties["extra_num"]).isEqualTo(JsonPrimitive(123))

        val reSerialized = json.encodeToString(deserialized)
        assertThat(json.parseToJsonElement(reSerialized)).isEqualTo(json.parseToJsonElement(serialized))
    }

    @Test
    fun `rejects unknown keys when additionalProperties is false`() {
        val input = """{"message":"m","x":1}"""
        assertThrows<SerializationException> {
            json.decodeFromString<ResultWithoutAdditionalProperties>(input)
        }
    }

    @Test
    fun `rejects unknown keys on an absent declaration by default and captures them under the policy`() {
        val input = """{"message":"m","x":1}"""
        assertThrows<SerializationException> {
            json.decodeFromString<ResultWithImplicitAdditionalProperties>(input)
        }

        val policyResult = json.decodeFromString<PolicyImplicitResult>(input)
        assertThat(policyResult.message).isEqualTo("m")
        assertThat(policyResult.properties.keys).isEqualTo(setOf("x"))
    }

    @Test
    fun `honours a naming strategy`() {
        val snakeJson = Json { namingStrategy = JsonNamingStrategy.SnakeCase }
        val input = """{"message":"m","other_key":1}"""
        val result = snakeJson.decodeFromString<Result>(input)

        assertThat(result.message).isEqualTo("m")
        assertThat(result.properties.keys).isEqualTo(setOf("other_key"))

        val encoded = snakeJson.encodeToString(result)
        assertThat(snakeJson.parseToJsonElement(encoded)).isEqualTo(snakeJson.parseToJsonElement(input))
    }

    @Test
    fun `captures extras on discriminated subtypes`() {
        val input =
            """[{"kind":"child","id":"1","label":"Alice","color":"red"},{"kind":"typed","id":"2","label":"Bob","z":null}]"""
        val orders = json.decodeFromString<List<OrderX>>(input)

        val child = orders[0] as OrderChild
        val typed = orders[1] as OrderTypedChild

        assertThat(child.properties.keys).isEqualTo(setOf("color"))
        assertThat(typed.properties.keys).isEqualTo(setOf("z"))
        assertThat(typed.properties["z"]).isEqualTo(JsonNull)

        assertThat(child.properties).doesNotContainKey("kind")
        assertThat(typed.properties).doesNotContainKey("kind")

        val encoded = json.encodeToString(orders)
        assertThat(json.parseToJsonElement(encoded)).isEqualTo(json.parseToJsonElement(input))
        assertThat(Regex("\"kind\"").findAll(encoded).count()).isEqualTo(2)
    }

    @Test
    fun `parent view shares the child map`() {
        val child = OrderChild(id = "1", label = "Alice", properties = mutableMapOf("color" to JsonPrimitive("red")))
        val order: OrderX = child
        order.properties["added"] = JsonPrimitive(1)

        val encoded = json.encodeToString<OrderX>(order)
        assertThat(encoded).contains("\"added\":1")

        val copied = child.copy(label = "Updated")
        assertThat(copied.properties.keys).contains("color", "added")
    }

    @Test
    fun `closed subtype rejects unknown keys`() {
        val valid = """{"kind":"sibling","id":"3","count":1}"""
        val order = json.decodeFromString<OrderX>(valid) as OrderSibling
        assertThat(order.properties).isEmpty()

        val invalid = """{"kind":"sibling","id":"3","count":1,"x":1}"""
        assertThrows<SerializationException> {
            json.decodeFromString<OrderX>(invalid)
        }

        val directValid = """{"id":"3","count":1}"""
        val directSibling = json.decodeFromString<OrderSibling>(directValid)
        assertThat(directSibling.properties).isEmpty()

        val directInvalid = """{"id":"3","count":1,"x":1}"""
        assertThrows<SerializationException> {
            json.decodeFromString<OrderSibling>(directInvalid)
        }
    }

    @Test
    fun `intermediate without discriminator shares root map`() {
        val leafJson = """{"id":"1","kind":"leaf","leafProp":"lp","intermediateProp":"ip","extra":"val"}"""
        val leaf = json.decodeFromString<RootOrder>(leafJson) as LeafOrder
        assertThat(leaf.properties).containsEntry("extra", JsonPrimitive("val"))
        assertThat(json.parseToJsonElement(json.encodeToString<RootOrder>(leaf)))
            .isEqualTo(json.parseToJsonElement(leafJson))
    }

    @Test
    fun `unconfigured parent leaves map on child that declares it`() {
        val explicitJson = """{"id":"1","kind":"explicit","label":"exp","extra":123}"""
        val explicit = json.decodeFromString<UnconfiguredParent>(explicitJson) as ExplicitChild
        assertThat(explicit.properties).containsEntry("extra", JsonPrimitive(123))

        val quietJson = """{"id":"2","kind":"quiet","name":"q","extra":123}"""
        assertThrows<SerializationException> {
            json.decodeFromString<UnconfiguredParent>(quietJson)
        }
    }

    @Test
    fun `global strategy override in SerializersModule intercepts all models`() {
        val customStrategy =
            object : AdditionalPropertiesSerializer.Strategy {
                override fun <T : Any> deserialize(
                    decoder: JsonDecoder,
                    generated: KSerializer<T>,
                    additionalProperties: (T) -> MutableMap<String, JsonElement>,
                    discriminator: String?,
                ): T {
                    val element = decoder.decodeJsonElement().jsonObject
                    val value = decoder.json.decodeFromJsonElement(generated, element)
                    additionalProperties(value)["INTERCEPTED"] = JsonPrimitive(true)
                    return value
                }

                override fun <T : Any> serialize(
                    encoder: JsonEncoder,
                    value: T,
                    generated: KSerializer<T>,
                    additionalProperties: (T) -> MutableMap<String, JsonElement>,
                ) {
                    val declared = encoder.json.encodeToJsonElement(generated, value).jsonObject
                    encoder.encodeJsonElement(JsonObject(declared + ("SERIALIZED_BY_STRATEGY" to JsonPrimitive(true))))
                }
            }

        val orderStrategy =
            object : com.example.allofparentorder.models.AdditionalPropertiesSerializer.Strategy {
                override fun <T : Any> deserialize(
                    decoder: JsonDecoder,
                    generated: KSerializer<T>,
                    additionalProperties: (T) -> MutableMap<String, JsonElement>,
                    discriminator: String?,
                ): T {
                    val element = decoder.decodeJsonElement().jsonObject
                    val value = decoder.json.decodeFromJsonElement(generated, element)
                    additionalProperties(value)["INTERCEPTED"] = JsonPrimitive(true)
                    return value
                }

                override fun <T : Any> serialize(
                    encoder: JsonEncoder,
                    value: T,
                    generated: KSerializer<T>,
                    additionalProperties: (T) -> MutableMap<String, JsonElement>,
                ) {
                    val declared = encoder.json.encodeToJsonElement(generated, value).jsonObject
                    encoder.encodeJsonElement(JsonObject(declared + ("SERIALIZED_BY_STRATEGY" to JsonPrimitive(true))))
                }
            }

        val customJson =
            Json {
                ignoreUnknownKeys = true
                serializersModule =
                    AdditionalPropertiesSerializer.module(customStrategy) +
                    com.example.allofparentorder.models.AdditionalPropertiesSerializer.module(orderStrategy)
            }

        val result = customJson.decodeFromString<Result>("""{"message":"m","extra":1}""")
        assertThat(result.message).isEqualTo("m")
        assertThat(result.properties["INTERCEPTED"]).isEqualTo(JsonPrimitive(true))

        val serialized = customJson.encodeToString(result)
        assertThat(serialized).contains("\"SERIALIZED_BY_STRATEGY\":true")

        val child = customJson.decodeFromString<OrderX>("""{"kind":"child","id":"1","label":"Alice","extra":2}""") as OrderChild
        assertThat(child.label).isEqualTo("Alice")
        assertThat(child.properties["INTERCEPTED"]).isEqualTo(JsonPrimitive(true))
    }
}
