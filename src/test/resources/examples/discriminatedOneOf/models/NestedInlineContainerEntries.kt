package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid

public data class NestedInlineContainerEntries(
  @param:JsonProperty("selection")
  @get:JsonProperty("selection")
  @get:Valid
  public val selection: NestedInlineContainerSelection? = null,
)
