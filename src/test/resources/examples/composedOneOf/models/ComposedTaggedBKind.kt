package examples.composedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class ComposedTaggedBKind(
  @JsonValue
  public val `value`: String,
) {
  B("b"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, ComposedTaggedBKind> =
        entries.associateBy(ComposedTaggedBKind::value)

    public fun fromValue(`value`: String): ComposedTaggedBKind? = mapping[value]
  }
}
