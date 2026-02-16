package examples.faultTolerantEnums.models

import com.fasterxml.jackson.`annotation`.JsonEnumDefaultValue
import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class SimpleEnum(
  @JsonValue
  public val `value`: String,
) {
  VALUE_ONE("value_one"),
  VALUE_TWO("value_two"),
  @JsonEnumDefaultValue
  UNRECOGNIZED("UNRECOGNIZED"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, SimpleEnum> = entries.associateBy(SimpleEnum::value)

    public fun fromValue(`value`: String): SimpleEnum = mapping[value] ?: UNRECOGNIZED
  }
}
