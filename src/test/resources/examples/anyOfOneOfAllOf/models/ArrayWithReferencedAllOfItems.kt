package examples.anyOfOneOfAllOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import javax.validation.Valid
import kotlin.collections.List

public data class ArrayWithReferencedAllOfItems(
  @param:JsonProperty("items")
  @get:JsonProperty("items")
  @get:Valid
  public val items: List<RefAllOf>? = null,
)
