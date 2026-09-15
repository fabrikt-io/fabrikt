package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import java.math.BigDecimal

public data class PromotionEffectAutomaticDiscount(
  @param:JsonProperty("value")
  @get:JsonProperty("value")
  public val `value`: BigDecimal? = null,
) : PromotionEffectAdditionalData
