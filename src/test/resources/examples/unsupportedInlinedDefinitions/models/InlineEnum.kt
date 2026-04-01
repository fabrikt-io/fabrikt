package examples.unsupportedInlinedDefinitions.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class InlineEnum(
  @JsonValue
  public val `value`: String,
) {
  ACTIVE("active"),
  INACTIVE("inactive"),
  ARCHIVED("archived"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, InlineEnum> = entries.associateBy(InlineEnum::value)

    public fun fromValue(`value`: String): InlineEnum? = mapping[value]
  }
}
