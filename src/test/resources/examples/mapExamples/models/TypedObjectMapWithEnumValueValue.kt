package examples.mapExamples.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull

public data class TypedObjectMapWithEnumValueValue(
  @param:JsonProperty("kind")
  @get:JsonProperty("kind")
  @get:NotNull
  public val kind: TypedObjectMapWithEnumValueKind,
)
