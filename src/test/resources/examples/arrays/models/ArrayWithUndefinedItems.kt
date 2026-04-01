package examples.arrays.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.Any
import kotlin.collections.List

public data class ArrayWithUndefinedItems(
  @param:JsonProperty("items_array")
  @get:JsonProperty("items_array")
  public val itemsArray: List<Any>? = null,
)
