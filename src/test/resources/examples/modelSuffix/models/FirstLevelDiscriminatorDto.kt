package examples.modelSuffix.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class FirstLevelDiscriminatorDto(
  @JsonValue
  public val `value`: String,
) {
  SECOND_LEVEL_CHILD1("secondLevelChild1"),
  SECOND_LEVEL_CHILD2("secondLevelChild2"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, FirstLevelDiscriminatorDto> =
        entries.associateBy(FirstLevelDiscriminatorDto::value)

    public fun fromValue(`value`: String): FirstLevelDiscriminatorDto? = mapping[value]
  }
}
