package examples.allOfParentOrder.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlin.collections.List

public data class OrderHolder(
  @param:JsonProperty("value")
  @get:JsonProperty("value")
  @get:NotNull
  @get:Valid
  public val `value`: OrderX,
  @param:JsonProperty("choice")
  @get:JsonProperty("choice")
  @get:NotNull
  @get:Valid
  public val choice: OrderX,
  @param:JsonProperty("choices")
  @get:JsonProperty("choices")
  @get:Valid
  public val choices: List<OrderX>? = null,
)
