package examples.sharedCompositionUnions.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class OtherKind(
  @JsonValue
  public val `value`: String,
) {
  OTHER("other"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, OtherKind> = entries.associateBy(OtherKind::value)

    public fun fromValue(`value`: String): OtherKind? = mapping[value]
  }
}
