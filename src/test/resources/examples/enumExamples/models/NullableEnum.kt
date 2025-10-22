package examples.enumExamples.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class NullableEnum(
  @JsonValue
  public val `value`: String,
) {
  FOO("foo"),
  BAR("bar"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, NullableEnum> = entries.associateBy(NullableEnum::value)

    public fun fromValue(`value`: String): NullableEnum? = mapping[value]
  }
}
