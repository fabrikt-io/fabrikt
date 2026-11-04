package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid

public data class NestedInlineContainerNested(
  @param:JsonProperty("detail")
  @get:JsonProperty("detail")
  @get:Valid
  public val detail: NestedInlineContainerDetail? = null,
)
