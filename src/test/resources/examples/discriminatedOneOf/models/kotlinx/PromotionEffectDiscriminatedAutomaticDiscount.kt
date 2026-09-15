package examples.discriminatedOneOf.models

import java.math.BigDecimal
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("automaticDiscount")
@Serializable
public data class PromotionEffectDiscriminatedAutomaticDiscount(
  @Contextual
  @SerialName("value")
  public val `value`: BigDecimal? = null,
) : PromotionEffectDiscriminatedAdditionalData
