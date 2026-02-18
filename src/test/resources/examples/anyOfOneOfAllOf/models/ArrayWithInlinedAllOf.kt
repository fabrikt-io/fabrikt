package examples.anyOfOneOfAllOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import kotlin.collections.List

public data class ArrayWithInlinedAllOf(
  @param:JsonProperty("items")
  @get:JsonProperty("items")
  @get:Valid
  public val items: List<ArrayWithInlinedAllOfItems>? = null,
)
