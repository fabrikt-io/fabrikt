package examples.anyOfOneOfAllOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.String

public data class FirstOneB(
  @param:JsonProperty("first_property")
  @get:JsonProperty("first_property")
  public val firstProperty: String? = null,
)
