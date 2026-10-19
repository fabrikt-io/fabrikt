package examples.externalPolymorphicDiscriminator.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid

public data class Wrapper(
  @param:JsonProperty("polymorph")
  @get:JsonProperty("polymorph")
  @get:Valid
  public val polymorph: PolymorphicEnumDiscriminator? = null,
)
