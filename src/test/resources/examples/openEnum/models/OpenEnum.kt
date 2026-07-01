package examples.openEnum.models

import com.fasterxml.jackson.`annotation`.JsonEnumDefaultValue
import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class OpenEnum(
  @JsonValue
  public val `value`: String,
) {
  FOO("foo"),
  BAR("bar"),
  BAZ("baz"),
  @JsonEnumDefaultValue
  UNRECOGNIZED("UNRECOGNIZED"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, OpenEnum> = entries.associateBy(OpenEnum::value)

    public fun fromValue(`value`: String): OpenEnum = mapping[value] ?: UNRECOGNIZED
  }
}
