package examples.anyOfOneOfAllOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.String

public data class SecondAnyA(
  @param:JsonProperty("second_nested_any_of_prop")
  @get:JsonProperty("second_nested_any_of_prop")
  public val secondNestedAnyOfProp: String? = null,
)
