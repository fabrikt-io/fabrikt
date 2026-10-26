package examples.openEnum.models

import kotlin.String
import kotlin.collections.Map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = OpenEnum.Serializer::class)
public enum class OpenEnum(
  public val `value`: String,
) {
  @SerialName("foo")
  FOO("foo"),
  @SerialName("bar")
  BAR("bar"),
  @SerialName("baz")
  BAZ("baz"),
  @SerialName("UNRECOGNIZED")
  UNRECOGNIZED("UNRECOGNIZED"),
  ;

  override fun toString(): String = value

  public object Serializer : KSerializer<OpenEnum> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("examples.openEnum.models.OpenEnum", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, `value`: OpenEnum) {
      encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): OpenEnum = fromValue(decoder.decodeString())
  }

  public companion object {
    private val mapping: Map<String, OpenEnum> = entries.associateBy(OpenEnum::value)

    public fun fromValue(`value`: String): OpenEnum = mapping[value] ?: UNRECOGNIZED
  }
}
