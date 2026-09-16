package examples.mapExamples.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class TypedObjectMapWithEnumValueKind(
  @JsonValue
  public val `value`: String,
) {
  ALPHA("alpha"),
  BETA("beta"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, TypedObjectMapWithEnumValueKind> =
        entries.associateBy(TypedObjectMapWithEnumValueKind::value)

    public fun fromValue(`value`: String): TypedObjectMapWithEnumValueKind? = mapping[value]
  }
}
