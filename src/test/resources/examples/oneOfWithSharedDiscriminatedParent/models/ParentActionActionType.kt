package examples.oneOfWithSharedDiscriminatedParent.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class ParentActionActionType(
  @JsonValue
  public val `value`: String,
) {
  CHILD_A("CHILD_A"),
  CHILD_B("CHILD_B"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, ParentActionActionType> =
        entries.associateBy(ParentActionActionType::value)

    public fun fromValue(`value`: String): ParentActionActionType? = mapping[value]
  }
}
