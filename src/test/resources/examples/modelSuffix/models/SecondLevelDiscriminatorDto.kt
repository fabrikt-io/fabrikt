package examples.modelSuffix.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class SecondLevelDiscriminatorDto(
  @JsonValue
  public val `value`: String,
) {
  THIRD_LEVEL_CHILD1("thirdLevelChild1"),
  THIRD_LEVEL_CHILD2("thirdLevelChild2"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, SecondLevelDiscriminatorDto> =
        entries.associateBy(SecondLevelDiscriminatorDto::value)

    public fun fromValue(`value`: String): SecondLevelDiscriminatorDto? = mapping[value]
  }
}
