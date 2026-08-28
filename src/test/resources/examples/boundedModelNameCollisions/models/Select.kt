package examples.boundedModelNameCollisions.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class Select(
  @JsonValue
  public val `value`: String,
) {
  ALPHA("Alpha"),
  BETA("Beta"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, Select> = entries.associateBy(Select::value)

    public fun fromValue(`value`: String): Select? = mapping[value]
  }
}
