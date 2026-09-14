package examples.inlinedAggregatedObjects.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.String

public data class ContainerAliasedInlineObject(
  @param:JsonProperty("metaTitle")
  @get:JsonProperty("metaTitle")
  public val metaTitle: String? = null,
)
