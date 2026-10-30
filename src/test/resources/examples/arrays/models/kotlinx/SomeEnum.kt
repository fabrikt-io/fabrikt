package examples.arrays.models

import kotlin.String
import kotlin.collections.Map
import kotlinx.serialization.SerialName

public enum class SomeEnum(
  public val `value`: String,
) {
  @SerialName("foo")
  FOO("foo"),
  @SerialName("bar")
  BAR("bar"),
  @SerialName("baz")
  BAZ("baz"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, SomeEnum> = entries.associateBy(SomeEnum::value)

    public fun fromValue(`value`: String): SomeEnum? = mapping[value]
  }
}
