package examples.anyOfOneOfAllOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.String

public data class ItemId(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  public val id: String? = null,
)
