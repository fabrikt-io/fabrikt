package examples.inlinedAggregatedObjects.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.String

public data class Annotations(
  @param:JsonProperty("url_citation")
  @get:JsonProperty("url_citation")
  public val urlCitation: String? = null,
)
