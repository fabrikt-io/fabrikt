package examples.anyOfOneOfAllOf.models

import com.fasterxml.jackson.`annotation`.JsonAnyGetter
import com.fasterxml.jackson.`annotation`.JsonAnySetter
import com.fasterxml.jackson.`annotation`.JsonIgnore
import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.Any
import kotlin.String
import kotlin.collections.Map
import kotlin.collections.MutableMap

public data class OneOfAdditionalProps(
  @param:JsonProperty("second_nested_any_of_prop")
  @get:JsonProperty("second_nested_any_of_prop")
  public val secondNestedAnyOfProp: String? = null,
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
