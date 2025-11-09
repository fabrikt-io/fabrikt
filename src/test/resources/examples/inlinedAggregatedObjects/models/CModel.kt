package examples.inlinedAggregatedObjects.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import java.math.BigDecimal

public data class CModel(
  @param:JsonProperty("c")
  @get:JsonProperty("c")
  public val c: BigDecimal? = null,
)
