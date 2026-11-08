package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull

public data class Farm(
  @param:JsonProperty("animal")
  @get:JsonProperty("animal")
  @get:NotNull
  public val animal: Animal,
)
