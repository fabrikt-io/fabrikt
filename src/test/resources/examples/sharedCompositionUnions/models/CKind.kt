package examples.sharedCompositionUnions.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class CKind(
  @JsonValue
  public val `value`: String,
) {
  C("c"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, CKind> = entries.associateBy(CKind::value)

    public fun fromValue(`value`: String): CKind? = mapping[value]
  }
}
