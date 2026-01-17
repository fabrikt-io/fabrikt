package examples.anyOfOneOfAllOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.String

public data class ContainsNestedAnyOf(
  @param:JsonProperty("first_nested_any_of_prop")
  @get:JsonProperty("first_nested_any_of_prop")
  public val firstNestedAnyOfProp: String? = null,
  @param:JsonProperty("second_nested_any_of_prop")
  @get:JsonProperty("second_nested_any_of_prop")
  public val secondNestedAnyOfProp: String? = null,
)
