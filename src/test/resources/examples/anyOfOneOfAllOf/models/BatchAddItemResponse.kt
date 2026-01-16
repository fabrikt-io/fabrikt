package examples.anyOfOneOfAllOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import javax.validation.Valid
import kotlin.collections.List

public data class BatchAddItemResponse(
  @param:JsonProperty("items")
  @get:JsonProperty("items")
  @get:Valid
  public val items: List<BatchAddItemResponseItems>? = null,
)
