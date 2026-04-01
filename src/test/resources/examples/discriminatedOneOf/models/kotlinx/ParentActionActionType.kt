package examples.discriminatedOneOf.models

import kotlin.String
import kotlin.collections.Map
import kotlinx.serialization.SerialName

public enum class ParentActionActionType(
  public val `value`: String,
) {
  @SerialName("CHILD_A")
  CHILD_A("CHILD_A"),
  @SerialName("CHILD_B")
  CHILD_B("CHILD_B"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, ParentActionActionType> =
        entries.associateBy(ParentActionActionType::value)

    public fun fromValue(`value`: String): ParentActionActionType? = mapping[value]
  }
}
