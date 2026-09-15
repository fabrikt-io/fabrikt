package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.Int

public data class PromotionEffectBuyXGetY(
  @param:JsonProperty("buyQuantity")
  @get:JsonProperty("buyQuantity")
  public val buyQuantity: Int? = null,
) : PromotionEffectAdditionalData
