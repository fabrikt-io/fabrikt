package examples.inlinedAggregatedObjects.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import java.math.BigDecimal

public data class BModel(
  @param:JsonProperty("b")
  @get:JsonProperty("b")
  public val b: BigDecimal? = null,
)
