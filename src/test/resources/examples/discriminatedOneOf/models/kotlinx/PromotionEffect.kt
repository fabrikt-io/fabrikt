package examples.discriminatedOneOf.models

import jakarta.validation.constraints.NotNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
public data class PromotionEffect(
  @SerialName("additionalData")
  @get:NotNull
  public val additionalData: JsonElement,
)
