package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid

public data class NestedInlineContainerDetail(
  @param:JsonProperty("value")
  @get:JsonProperty("value")
  @get:Valid
  public val `value`: NestedInlineContainerValue? = null,
)
