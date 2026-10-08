package examples.inlinedAggregatedObjects.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import kotlin.collections.List

public data class ContainerWithNamedAggregatedArray(
  @param:JsonProperty("entries")
  @get:JsonProperty("entries")
  public val entries: List<@Valid NamedAggregatedArray>? = null,
)
