package examples.anyOfOneOfAllOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlin.collections.List

public data class Items(
  @param:JsonProperty("items")
  @get:JsonProperty("items")
  @get:NotNull
  @get:Valid
  public val items: List<ItemReference>,
)
