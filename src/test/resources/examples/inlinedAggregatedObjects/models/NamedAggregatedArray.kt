package examples.inlinedAggregatedObjects.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import kotlin.collections.List

public data class NamedAggregatedArray(
  @param:JsonProperty("annotations")
  @get:JsonProperty("annotations")
  public val annotations: List<@Valid Annotations>? = null,
  @param:JsonProperty("metadata")
  @get:JsonProperty("metadata")
  @get:Valid
  public val metadata: Metadata? = null,
)
