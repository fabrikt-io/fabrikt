package examples.externalReferences.targeted.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class ExternalObjectThreeEnum(
  @JsonValue
  public val `value`: String,
) {
  ONE("one"),
  TWO("two"),
  THREE("three"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, ExternalObjectThreeEnum> =
        entries.associateBy(ExternalObjectThreeEnum::value)

    public fun fromValue(`value`: String): ExternalObjectThreeEnum? = mapping[value]
  }
}
