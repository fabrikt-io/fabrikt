package examples.composedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class ComposedTaggedAKind(
  @JsonValue
  public val `value`: String,
) {
  A("a"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, ComposedTaggedAKind> =
        entries.associateBy(ComposedTaggedAKind::value)

    public fun fromValue(`value`: String): ComposedTaggedAKind? = mapping[value]
  }
}
