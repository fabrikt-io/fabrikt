package examples.enumExamples.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class EnumHolderInlinedEnum(
  @JsonValue
  public val `value`: String,
) {
  INLINED_ONE("inlined_one"),
  INLINED_TWO("inlined_two"),
  INLINED_THREE("inlined_three"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, EnumHolderInlinedEnum> =
        entries.associateBy(EnumHolderInlinedEnum::value)

    public fun fromValue(`value`: String): EnumHolderInlinedEnum? = mapping[value]
  }
}
