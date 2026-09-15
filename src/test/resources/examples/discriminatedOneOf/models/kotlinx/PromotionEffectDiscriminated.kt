package examples.discriminatedOneOf.models

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class PromotionEffectDiscriminated(
  @SerialName("additionalData")
  @get:NotNull
  @get:Valid
  public val additionalData: PromotionEffectDiscriminatedAdditionalData,
)
