package examples.allOfParentOrder.models

import com.fasterxml.jackson.`annotation`.JsonAnyGetter
import com.fasterxml.jackson.`annotation`.JsonAnySetter
import com.fasterxml.jackson.`annotation`.JsonIgnore
import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlin.Any
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableMap

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
  public val choices: List<@Valid OrderX>? = null,
  @get:JsonIgnore
  public val properties: MutableMap<String, Any?> = mutableMapOf(),
) {
  @JsonAnyGetter
  public fun `get`(): Map<String, Any?> = properties

  @JsonAnySetter
  public fun `set`(name: String, `value`: Any?) {
    properties[name] = value
  }
}
