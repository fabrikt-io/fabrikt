package examples.enumExamples.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class EnumObject(
  @JsonValue
  public val `value`: String,
) {
  ONE("one"),
  TWO("two"),
  THREE("three"),
  `4`("4"),
  _5("-5"),
  _6("_6"),
  REALLY_LONG_ENUM_VALUE_GOES_HERE_AND_IT_IS_VERY_DESCRIPTIVE("really long enum value goes here and it is very descriptive"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, EnumObject> = entries.associateBy(EnumObject::value)

    public fun fromValue(`value`: String): EnumObject? = mapping[value]
  }
}
