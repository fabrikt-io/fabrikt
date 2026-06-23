package examples.arrays.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class SomeEnum(
  @JsonValue
  public val `value`: String,
) {
  FOO("foo"),
  BAR("bar"),
  BAZ("baz"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, SomeEnum> = entries.associateBy(SomeEnum::value)

    public fun fromValue(`value`: String): SomeEnum? = mapping[value]
  }
}
