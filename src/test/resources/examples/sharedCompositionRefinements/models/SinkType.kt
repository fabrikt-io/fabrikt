package examples.sharedCompositionRefinements.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class SinkType(
  @JsonValue
  public val `value`: String,
) {
  KAFKA("Kafka"),
  NAKADI("Nakadi"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, SinkType> = entries.associateBy(SinkType::value)

    public fun fromValue(`value`: String): SinkType? = mapping[value]
  }
}
