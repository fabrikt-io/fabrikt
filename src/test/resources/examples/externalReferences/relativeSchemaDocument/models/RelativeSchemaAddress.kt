package examples.externalReferences.relativeSchemaDocument.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.String

public data class RelativeSchemaAddress(
  @param:JsonProperty("street")
  @get:JsonProperty("street")
  public val street: String? = null,
  @param:JsonProperty("city")
  @get:JsonProperty("city")
  public val city: String? = null,
)
