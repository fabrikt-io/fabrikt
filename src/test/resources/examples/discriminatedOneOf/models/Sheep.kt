package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class Sheep(
  @JsonValue
  public val `value`: String,
) {
  BAA("Baa"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, Sheep> = entries.associateBy(Sheep::value)

    public fun fromValue(`value`: String): Sheep? = mapping[value]
  }
}
