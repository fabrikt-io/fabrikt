package examples.inlinedEnumParameter.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.String

public data class Item(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  public val id: String? = null,
  @param:JsonProperty("name")
  @get:JsonProperty("name")
  public val name: String? = null,
)
