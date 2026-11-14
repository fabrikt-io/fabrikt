package examples.singletonHierarchyUnion.models

import com.fasterxml.jackson.`annotation`.JsonAnyGetter
import com.fasterxml.jackson.`annotation`.JsonAnySetter
import com.fasterxml.jackson.`annotation`.JsonIgnore
import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.String
import kotlin.collections.Map
import kotlin.collections.MutableMap

public data class MiddleBag(
  @param:JsonProperty("tag")
  @get:JsonProperty("tag")
  public val tag: String? = null,
  @get:JsonIgnore
  public val properties: MutableMap<String, MiddleContract?> = mutableMapOf(),
) {
  @JsonAnyGetter
  public fun `get`(): Map<String, MiddleContract?> = properties

  @JsonAnySetter
  public fun `set`(name: String, `value`: MiddleContract?) {
    properties[name] = value
  }
}
