package examples.nestedPolymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class RootDiscriminator(
  @JsonValue
  public val `value`: String,
) {
  FIRST_LEVEL_CHILD("firstLevelChild"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, RootDiscriminator> =
        entries.associateBy(RootDiscriminator::value)

    public fun fromValue(`value`: String): RootDiscriminator? = mapping[value]
  }
}
