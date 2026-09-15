package examples.discriminatedOneOf.models

import java.math.BigDecimal
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class PromotionEffectAutomaticDiscount(
  @Contextual
  @SerialName("value")
  public val `value`: BigDecimal? = null,
) : PromotionEffectAdditionalData
