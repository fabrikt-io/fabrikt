package examples.sharedCompositionUnions.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid

public data class InlineContainerNested(
  @param:JsonProperty("choice")
  @get:JsonProperty("choice")
  @get:Valid
  public val choice: InlineContainerChoice? = null,
)
