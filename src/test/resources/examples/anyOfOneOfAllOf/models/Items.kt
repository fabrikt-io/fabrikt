package examples.anyOfOneOfAllOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Any
import kotlin.collections.List

public data class Items(
  @param:JsonProperty("items")
  @get:JsonProperty("items")
  @get:NotNull
  public val items: List<Any>,
)
