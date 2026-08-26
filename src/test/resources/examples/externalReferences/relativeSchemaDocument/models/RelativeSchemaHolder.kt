package examples.externalReferences.relativeSchemaDocument.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid

public data class RelativeSchemaHolder(
  @param:JsonProperty("relative")
  @get:JsonProperty("relative")
  @get:Valid
  public val relative: RelativeSchema? = null,
)
