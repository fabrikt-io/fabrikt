package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull

public data class PromotionEffectDiscriminated(
  @param:JsonProperty("additionalData")
  @get:JsonProperty("additionalData")
  @get:NotNull
  @get:Valid
  public val additionalData: PromotionEffectDiscriminatedAdditionalData,
)
