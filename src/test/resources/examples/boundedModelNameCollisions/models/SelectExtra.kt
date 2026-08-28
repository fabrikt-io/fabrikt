package examples.boundedModelNameCollisions.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class SelectExtra(
  @JsonValue
  public val `value`: String,
) {
  GAMMA("Gamma"),
  DELTA("Delta"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, SelectExtra> = entries.associateBy(SelectExtra::value)

    public fun fromValue(`value`: String): SelectExtra? = mapping[value]
  }
}
