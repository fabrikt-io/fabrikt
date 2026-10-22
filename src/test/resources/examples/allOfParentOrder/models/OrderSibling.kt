package examples.allOfParentOrder.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Int
import kotlin.String

public data class OrderSibling(
  @param:JsonProperty(
    "count",
    required = true,
  )
  @get:JsonProperty("count")
  @get:NotNull
  public val count: Int,
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  override val id: String,
) : OrderX(id) {
  @get:JsonProperty(
    value = "kind",
    access = JsonProperty.Access.READ_ONLY,
  )
  @get:NotNull
  override val kind: String
    get() = "sibling"
}
