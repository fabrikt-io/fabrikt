package examples.githubApi.models

import com.fasterxml.jackson.`annotation`.JsonAnyGetter
import com.fasterxml.jackson.`annotation`.JsonAnySetter
import com.fasterxml.jackson.`annotation`.JsonIgnore
import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kotlin.Any
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableMap

public data class EventResults(
  @param:JsonProperty("change_events")
  @get:JsonProperty("change_events")
  @get:NotNull
  @get:Size(min = 0)
  @get:Valid
  public val changeEvents: List<Event>,
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
