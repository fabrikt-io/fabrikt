package examples.allOfParentOrder.models

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class OrderHolder(
  @SerialName("value")
  @get:NotNull
  @get:Valid
  public val `value`: OrderX,
  @SerialName("choice")
  @get:NotNull
  @get:Valid
  public val choice: OrderX,
  @SerialName("choices")
  public val choices: List<@Valid OrderX>? = null,
)
