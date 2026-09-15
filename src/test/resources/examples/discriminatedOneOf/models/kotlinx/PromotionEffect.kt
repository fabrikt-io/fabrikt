package examples.discriminatedOneOf.models

import jakarta.validation.constraints.NotNull
import kotlin.Any
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class PromotionEffect(
  @SerialName("additionalData")
  @get:NotNull
  public val additionalData: Any,
)
