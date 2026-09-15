package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Any

public data class PromotionEffect(
  @param:JsonProperty("additionalData")
  @get:JsonProperty("additionalData")
  @get:NotNull
  public val additionalData: Any,
)
