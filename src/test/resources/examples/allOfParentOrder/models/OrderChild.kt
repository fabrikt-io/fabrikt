package examples.allOfParentOrder.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class OrderChild(
  @param:JsonProperty("label")
  @get:JsonProperty("label")
  @get:NotNull
  public val label: String,
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
    get() = "child"
}
