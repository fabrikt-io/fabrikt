package examples.anyOfOneOfAllOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class ComplexSecondOneA(
  @param:JsonProperty("more_nested_prop_one")
  @get:JsonProperty("more_nested_prop_one")
  @get:NotNull
  public val moreNestedPropOne: String,
)
