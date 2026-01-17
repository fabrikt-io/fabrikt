package examples.anyOfOneOfAllOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.String

public data class FirstAnyA(
  @param:JsonProperty("first_nested_any_of_prop")
  @get:JsonProperty("first_nested_any_of_prop")
  public val firstNestedAnyOfProp: String? = null,
)
