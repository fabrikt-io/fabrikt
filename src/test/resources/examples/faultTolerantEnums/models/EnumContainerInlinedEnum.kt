package examples.faultTolerantEnums.models

import com.fasterxml.jackson.`annotation`.JsonEnumDefaultValue
import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class EnumContainerInlinedEnum(
  @JsonValue
  public val `value`: String,
) {
  INLINED_A("inlined_a"),
  INLINED_B("inlined_b"),
  @JsonEnumDefaultValue
  UNRECOGNIZED("UNRECOGNIZED"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, EnumContainerInlinedEnum> =
        entries.associateBy(EnumContainerInlinedEnum::value)

    public fun fromValue(`value`: String): EnumContainerInlinedEnum = mapping[value] ?: UNRECOGNIZED
  }
}
