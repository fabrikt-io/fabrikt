package examples.anyOfOneOfAllOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.String

public data class FirstOneA(
  @param:JsonProperty("first_nested_one_of_prop")
  @get:JsonProperty("first_nested_one_of_prop")
  public val firstNestedOneOfProp: String? = null,
)
