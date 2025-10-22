package examples.modelSuffix.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

public enum class RootDiscriminatorDto(
  @JsonValue
  public val `value`: String,
) {
  FIRST_LEVEL_CHILD("firstLevelChild"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, RootDiscriminatorDto> =
        entries.associateBy(RootDiscriminatorDto::value)

    public fun fromValue(`value`: String): RootDiscriminatorDto? = mapping[value]
  }
}
