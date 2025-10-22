package examples.nestedPolymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class FirstLevelDiscriminator(
  @JsonValue
  public val `value`: String,
) {
  SECOND_LEVEL_CHILD1("secondLevelChild1"),
  SECOND_LEVEL_CHILD2("secondLevelChild2"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, FirstLevelDiscriminator> =
        entries.associateBy(FirstLevelDiscriminator::value)

    public fun fromValue(`value`: String): FirstLevelDiscriminator? = mapping[value]
  }
}
