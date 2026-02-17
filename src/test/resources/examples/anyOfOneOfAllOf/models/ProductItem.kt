package examples.anyOfOneOfAllOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class ProductItem(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  public val id: String,
  @param:JsonProperty("item_type")
  @get:JsonProperty("item_type")
  @get:NotNull
  public val itemType: String,
  @param:JsonProperty("sku")
  @get:JsonProperty("sku")
  @get:NotNull
  public val sku: String,
) : ItemReference
