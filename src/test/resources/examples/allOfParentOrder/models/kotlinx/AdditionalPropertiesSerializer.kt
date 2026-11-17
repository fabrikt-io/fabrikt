package examples.allOfParentOrder.models

import kotlin.Any
import kotlin.OptIn
import kotlin.String
import kotlin.collections.MutableMap
import kotlin.collections.Set
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.contextual

@OptIn(ExperimentalSerializationApi::class)
public class AdditionalPropertiesSerializer<T : Any>(
  private val generated: KSerializer<T>,
  private val additionalProperties: (T) -> MutableMap<String, JsonElement>,
  private val discriminator: String? = null,
) : KSerializer<T> {
  override val descriptor: SerialDescriptor = generated.descriptor

  private fun knownKeys(json: Json): Set<String> {
    val strategy = json.configuration.namingStrategy
    val names = (0 until descriptor.elementsCount).map { index ->
      val name = descriptor.getElementName(index)
      strategy?.serialNameForJson(descriptor, index, name) ?: name
    }
    return (names + listOfNotNull(discriminator)).toSet()
  }

  override fun deserialize(decoder: Decoder): T {
    val input = decoder as? JsonDecoder ?: return generated.deserialize(decoder)
    val strategy = input.serializersModule.getAdditionalPropertiesStrategy()
    if (strategy != null) {
      return strategy.deserialize(input, generated, additionalProperties, discriminator)
    }
    val known = knownKeys(input.json)
    val (declared, extras) = input.decodeJsonElement().jsonObject.entries.partition { it.key in known }
    val value = input.json.decodeFromJsonElement(generated, JsonObject(declared.filter { it.key != discriminator }.associate { it.key to it.value }))
    additionalProperties(value).putAll(extras.associate { it.key to it.value })
    return value
  }

  override fun serialize(encoder: Encoder, `value`: T) {
    val output = encoder as? JsonEncoder ?: run {
      generated.serialize(encoder, value)
      return
    }
    val strategy = output.serializersModule.getAdditionalPropertiesStrategy()
    if (strategy != null) {
      strategy.serialize(output, value, generated, additionalProperties)
      return
    }
    val declared = output.json.encodeToJsonElement(generated, value).jsonObject
    output.encodeJsonElement(JsonObject(additionalProperties(value) + declared))
  }

  public interface Strategy {
    public fun <T : Any> deserialize(
      decoder: JsonDecoder,
      generated: KSerializer<T>,
      additionalProperties: (T) -> MutableMap<String, JsonElement>,
      discriminator: String?,
    ): T

    public fun <T : Any> serialize(
      encoder: JsonEncoder,
      `value`: T,
      generated: KSerializer<T>,
      additionalProperties: (T) -> MutableMap<String, JsonElement>,
    )
  }

  public class StrategySerializer(
    public val strategy: Strategy,
  ) : KSerializer<Strategy> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("AdditionalPropertiesSerializer.Strategy", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Strategy = strategy

    override fun serialize(encoder: Encoder, `value`: Strategy) {
    }
  }

  public companion object {
    public fun module(strategy: Strategy): SerializersModule = SerializersModule { contextual(Strategy::class, StrategySerializer(strategy)) }

    public fun SerializersModuleBuilder.additionalPropertiesStrategy(strategy: Strategy) {
      contextual(Strategy::class, StrategySerializer(strategy))
    }

    public fun SerializersModule.getAdditionalPropertiesStrategy(): Strategy? = (getContextual(Strategy::class) as? StrategySerializer)?.strategy
  }
}
