package examples.openEnum.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class CloseEnum(
  @JsonValue
  public val `value`: String,
) {
  FOO("foo"),
  BAR("bar"),
  BAZ("baz"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, CloseEnum> = entries.associateBy(CloseEnum::value)

    public fun fromValue(`value`: String): CloseEnum? = mapping[value]
  }
}
