package examples.oneOfAllOfBug.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import kotlin.Any
import kotlin.collections.List

/**
 * List of wardrobe items
 */
public data class Items(
  @param:JsonProperty("items")
  @get:JsonProperty("items")
  @get:NotNull
  @get:Size(min = 0)
  public val items: List<Any>,
)
