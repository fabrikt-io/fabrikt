package examples.inlinedAggregatedObjects.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.String

public data class Metadata(
  @param:JsonProperty("source")
  @get:JsonProperty("source")
  public val source: String? = null,
)
