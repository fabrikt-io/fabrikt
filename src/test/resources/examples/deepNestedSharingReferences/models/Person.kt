package examples.deepNestedSharingReferences.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid

public data class Person(
  @param:JsonProperty("home_address")
  @get:JsonProperty("home_address")
  @get:Valid
  public val homeAddress: Address? = null,
)
