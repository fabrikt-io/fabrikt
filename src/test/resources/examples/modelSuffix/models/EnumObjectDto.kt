package examples.modelSuffix.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class EnumObjectDto(
  @JsonValue
  public val `value`: String,
) {
  ONE("one"),
  TWO("two"),
  THREE("three"),
  `4`("4"),
  _5("-5"),
  _6("_6"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, EnumObjectDto> = entries.associateBy(EnumObjectDto::value)

    public fun fromValue(`value`: String): EnumObjectDto? = mapping[value]
  }
}
