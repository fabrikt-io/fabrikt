package examples.enumExamples.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class EnumHolderInlinedExtensibleEnum(
  @JsonValue
  public val `value`: String,
) {
  INLINED_ONE("inlined_one"),
  INLINED_TWO("inlined_two"),
  INLINED_THREE("inlined_three"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, EnumHolderInlinedExtensibleEnum> =
        entries.associateBy(EnumHolderInlinedExtensibleEnum::value)

    public fun fromValue(`value`: String): EnumHolderInlinedExtensibleEnum? = mapping[value]
  }
}
