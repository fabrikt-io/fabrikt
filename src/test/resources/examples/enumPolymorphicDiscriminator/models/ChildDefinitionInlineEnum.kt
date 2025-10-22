package examples.enumPolymorphicDiscriminator.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class ChildDefinitionInlineEnum(
  @JsonValue
  public val `value`: String,
) {
  ONE("one"),
  TWO("two"),
  THREE("three"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, ChildDefinitionInlineEnum> =
        entries.associateBy(ChildDefinitionInlineEnum::value)

    public fun fromValue(`value`: String): ChildDefinitionInlineEnum? = mapping[value]
  }
}
