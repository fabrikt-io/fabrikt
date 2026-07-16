package examples.openEnum.models

import kotlin.String
import kotlin.collections.Map
import kotlinx.serialization.SerialName

public enum class OpenEnum(
  public val `value`: String,
) {
  @SerialName("foo")
  FOO("foo"),
  @SerialName("bar")
  BAR("bar"),
  @SerialName("baz")
  BAZ("baz"),
  @SerialName("UNRECOGNIZED")
  UNRECOGNIZED("UNRECOGNIZED"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, OpenEnum> = entries.associateBy(OpenEnum::value)

    public fun fromValue(`value`: String): OpenEnum = mapping[value] ?: UNRECOGNIZED
  }
}
