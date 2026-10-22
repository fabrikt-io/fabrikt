package examples.anyOfOneOfAllOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class ProductItem(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  public val id: String,
  @param:JsonProperty("sku")
  @get:JsonProperty("sku")
  @get:NotNull
  public val sku: String,
) : ItemReference {
  @get:JsonProperty(
    value = "item_type",
    access = JsonProperty.Access.READ_ONLY,
  )
  @get:NotNull
  public val itemType: String
    get() = "PRODUCT"
}
