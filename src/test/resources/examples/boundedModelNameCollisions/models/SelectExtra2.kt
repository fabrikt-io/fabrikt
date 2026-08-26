package examples.boundedModelNameCollisions.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class SelectExtra2(
  @JsonValue
  public val `value`: String,
) {
  EPSILON("Epsilon"),
  ZETA("Zeta"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, SelectExtra2> = entries.associateBy(SelectExtra2::value)

    public fun fromValue(`value`: String): SelectExtra2? = mapping[value]
  }
}
