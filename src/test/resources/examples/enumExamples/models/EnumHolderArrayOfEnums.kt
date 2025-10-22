package examples.enumExamples.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class EnumHolderArrayOfEnums(
  @JsonValue
  public val `value`: String,
) {
  ARRAY_ENUM_ONE("array_enum_one"),
  ARRAY_ENUM_TWO("array_enum_two"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, EnumHolderArrayOfEnums> =
        entries.associateBy(EnumHolderArrayOfEnums::value)

    public fun fromValue(`value`: String): EnumHolderArrayOfEnums? = mapping[value]
  }
}
