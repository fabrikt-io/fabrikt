package examples.arrays.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import kotlin.collections.List

public data class ContainsNamedInlinedArray(
  @param:JsonProperty("items")
  @get:JsonProperty("items")
  public val items: List<@Valid ArrayContainingComplexInlined>? = null,
)
