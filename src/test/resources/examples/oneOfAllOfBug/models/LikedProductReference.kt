package examples.oneOfAllOfBug.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import javax.validation.constraints.NotNull
import kotlin.String

public data class LikedProductReference(
  /**
   * Unique identifier of an item within Wardrobe.
   */
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  public val id: String,
  /**
   * Describes the kind of item and provides a structural hint regarding what optional properties
   * will be present. E.g.: ARTICLE items will always have the article attribute
   */
  @param:JsonProperty("item_type")
  @get:JsonProperty("item_type")
  @get:NotNull
  public val itemType: GenericItemType,
  /**
   * Config SKU of the article that was liked by the customer
   */
  @param:JsonProperty("config_sku")
  @get:JsonProperty("config_sku")
  public val configSku: String? = null,
  /**
   * Simple SKU that represents size liked by the customer.
   */
  @param:JsonProperty("simple_sku")
  @get:JsonProperty("simple_sku")
  public val simpleSku: String? = null,
  @param:JsonProperty("cursor")
  @get:JsonProperty("cursor")
  public val cursor: String? = null,
)
