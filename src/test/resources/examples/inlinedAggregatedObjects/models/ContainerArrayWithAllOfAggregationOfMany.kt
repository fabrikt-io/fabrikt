package examples.inlinedAggregatedObjects.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import kotlin.String
import kotlin.collections.List

public data class ContainerArrayWithAllOfAggregationOfMany(
  @param:JsonProperty("annotations")
  @get:JsonProperty("annotations")
  public val annotations: List<@Valid ContainerAnnotations>? = null,
  /**
   * The identifier of the chat message.
   */
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  public val id: String? = null,
)
