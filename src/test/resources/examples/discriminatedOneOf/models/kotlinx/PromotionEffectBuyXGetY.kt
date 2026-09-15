package examples.discriminatedOneOf.models

import kotlin.Int
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class PromotionEffectBuyXGetY(
  @SerialName("buyQuantity")
  public val buyQuantity: Int? = null,
) : PromotionEffectAdditionalData
