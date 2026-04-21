package examples.inlinedAggregatedObjects.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import kotlin.collections.List

public data class ContainerWithNamedAggregatedArray(
  @param:JsonProperty("entries")
  @get:JsonProperty("entries")
  @get:Valid
  public val entries: List<NamedAggregatedArray>? = null,
)
