package examples.oneOfPolymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

/**
 * Shows which child type is being returned
 */
public enum class ParentType(
  @JsonValue
  public val `value`: String,
) {
  CHILD_TYPE_A("CHILD_TYPE_A"),
  CHILD_TYPE_B("CHILD_TYPE_B"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, ParentType> = entries.associateBy(ParentType::value)

    public fun fromValue(`value`: String): ParentType? = mapping[value]
  }
}
