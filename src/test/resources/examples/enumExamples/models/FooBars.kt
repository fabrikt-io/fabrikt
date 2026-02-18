package examples.enumExamples.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import kotlin.collections.List

public data class FooBars(
  @param:JsonProperty("prop_one")
  @get:JsonProperty("prop_one")
  public val propOne: List<FooBarsFoo>? = null,
  @param:JsonProperty("prop_two")
  @get:JsonProperty("prop_two")
  @get:Valid
  public val propTwo: List<FooBarsBar>? = null,
)
