package examples.externalReferences.targeted.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid

public data class ContainingExternalReference(
  @param:JsonProperty("some-external-reference")
  @get:JsonProperty("some-external-reference")
  @get:Valid
  public val someExternalReference: ExternalObject? = null,
)
