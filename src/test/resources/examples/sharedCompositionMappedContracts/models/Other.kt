package examples.sharedCompositionMappedContracts.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.String

public data class Other(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  public val id: String? = null,
)
