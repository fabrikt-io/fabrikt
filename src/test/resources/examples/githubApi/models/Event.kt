package examples.githubApi.models

import com.fasterxml.jackson.`annotation`.JsonAnyGetter
import com.fasterxml.jackson.`annotation`.JsonAnySetter
import com.fasterxml.jackson.`annotation`.JsonIgnore
import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Any
import kotlin.String
import kotlin.collections.Map
import kotlin.collections.MutableMap

public data class Event(
  /**
   * The id of the entity this event belongs to
   */
  @param:JsonProperty("entity_id")
  @get:JsonProperty("entity_id")
  @get:NotNull
  public val entityId: String,
  /**
   * This unstructured event content must conform to the Nakadi Event Type's schema
   */
  @param:JsonProperty("data")
  @get:JsonProperty("data")
  @get:NotNull
  public val `data`: Map<String, Any?>,
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
