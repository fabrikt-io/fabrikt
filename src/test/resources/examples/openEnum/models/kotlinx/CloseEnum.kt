package examples.openEnum.models

import kotlin.String
import kotlin.collections.Map
import kotlinx.serialization.SerialName

public enum class CloseEnum(
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
    private val mapping: Map<String, CloseEnum> = entries.associateBy(CloseEnum::value)

    public fun fromValue(`value`: String): CloseEnum? = mapping[value]
  }
}
