package examples.nestedPolymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull

public data class SecondLevelMetadata(
  @param:JsonProperty("obj")
  @get:JsonProperty("obj")
  @get:NotNull
  @get:Valid
  public val obj: CommonObject,
)
