package examples.arrays.models

import com.fasterxml.jackson.`annotation`.JsonProperty

public data class ContainsEnum(
  @param:JsonProperty("somes")
  @get:JsonProperty("somes")
  public val somes: SomeEnum? = null,
)
