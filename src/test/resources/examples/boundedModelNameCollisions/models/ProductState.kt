package examples.boundedModelNameCollisions.models

import com.fasterxml.jackson.`annotation`.JsonProperty

public data class ProductState(
  @param:JsonProperty("state")
  @get:JsonProperty("state")
  public val state: ProductStateState? = null,
)
