package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import kotlin.collections.List

public data class ResponseWithChildActionsAll(
  @param:JsonProperty("actions")
  @get:JsonProperty("actions")
  @get:Valid
  public val actions: List<ChildActionsAll>? = null,
)
