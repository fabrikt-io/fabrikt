package examples.nestedPolymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class SecondLevelDiscriminator(
  @JsonValue
  public val `value`: String,
) {
  THIRD_LEVEL_CHILD1("thirdLevelChild1"),
  THIRD_LEVEL_CHILD2("thirdLevelChild2"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, SecondLevelDiscriminator> =
        entries.associateBy(SecondLevelDiscriminator::value)

    public fun fromValue(`value`: String): SecondLevelDiscriminator? = mapping[value]
  }
}
