package examples.boundedModelNameCollisions.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull

public data class Product(
  @param:JsonProperty("state")
  @get:JsonProperty("state")
  @get:NotNull
  public val state: ProductStateExtra,
)
