package examples.sharedCompositionUnions.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class BKind(
  @JsonValue
  public val `value`: String,
) {
  B("b"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, BKind> = entries.associateBy(BKind::value)

    public fun fromValue(`value`: String): BKind? = mapping[value]
  }
}
