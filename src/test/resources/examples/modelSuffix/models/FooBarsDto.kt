package examples.modelSuffix.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import kotlin.collections.List

public data class FooBarsDto(
  @param:JsonProperty("prop_one")
  @get:JsonProperty("prop_one")
  public val propOne: List<FooBarsDtoFooDto>? = null,
  @param:JsonProperty("prop_two")
  @get:JsonProperty("prop_two")
  @get:Valid
  public val propTwo: List<FooBarsDtoBarDto>? = null,
)
