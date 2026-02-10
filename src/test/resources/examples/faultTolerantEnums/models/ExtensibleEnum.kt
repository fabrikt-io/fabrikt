package examples.faultTolerantEnums.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class ExtensibleEnum(
  @JsonValue
  public val `value`: String,
) {
  EXTENSIBLE_ONE("extensible_one"),
  EXTENSIBLE_TWO("extensible_two"),
  UNRECOGNIZED("UNRECOGNIZED"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, ExtensibleEnum> = entries.associateBy(ExtensibleEnum::value)

    public fun fromValue(`value`: String): ExtensibleEnum = mapping[value] ?: UNRECOGNIZED
  }
}
