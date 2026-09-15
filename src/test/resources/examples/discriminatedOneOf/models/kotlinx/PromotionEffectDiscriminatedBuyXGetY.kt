package examples.discriminatedOneOf.models

import kotlin.Int
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("buyXGetY")
@Serializable
public data class PromotionEffectDiscriminatedBuyXGetY(
  @SerialName("buyQuantity")
  public val buyQuantity: Int? = null,
) : PromotionEffectDiscriminatedAdditionalData
