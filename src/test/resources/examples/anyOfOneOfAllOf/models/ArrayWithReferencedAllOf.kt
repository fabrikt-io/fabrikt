package examples.anyOfOneOfAllOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import kotlin.collections.List

public data class ArrayWithReferencedAllOf(
  @param:JsonProperty("items")
  @get:JsonProperty("items")
  public val items: List<@Valid RefAllOf>? = null,
)
