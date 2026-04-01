package examples.externalReferences.targeted.models

import com.fasterxml.jackson.`annotation`.JsonAnyGetter
import com.fasterxml.jackson.`annotation`.JsonAnySetter
import com.fasterxml.jackson.`annotation`.JsonIgnore
import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableMap

public data class ExternalObjectTwo(
  @param:JsonProperty("list-others")
  @get:JsonProperty("list-others")
  @get:Valid
  public val listOthers: List<ExternalObjectThree>? = null,
  @get:JsonIgnore
  public val properties: MutableMap<String, Map<String, ExternalObjectFour?>?> = mutableMapOf(),
) {
  @JsonAnyGetter
  public fun `get`(): Map<String, Map<String, ExternalObjectFour?>?> = properties

  @JsonAnySetter
  public fun `set`(name: String, `value`: Map<String, ExternalObjectFour?>?) {
    properties[name] = value
  }
}
