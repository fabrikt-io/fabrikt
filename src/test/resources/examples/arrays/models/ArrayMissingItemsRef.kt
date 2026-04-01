package examples.arrays.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.Any
import kotlin.collections.List

public data class ArrayMissingItemsRef(
  @param:JsonProperty("broken_ref")
  @get:JsonProperty("broken_ref")
  public val brokenRef: List<Any>? = null,
)
