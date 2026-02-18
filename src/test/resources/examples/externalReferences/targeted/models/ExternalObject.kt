package examples.externalReferences.targeted.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid

public data class ExternalObject(
  @param:JsonProperty("another")
  @get:JsonProperty("another")
  @get:Valid
  public val another: ExternalObjectTwo? = null,
  @param:JsonProperty("one_of")
  @get:JsonProperty("one_of")
  @get:Valid
  public val oneOf: ParentOneOf? = null,
)
