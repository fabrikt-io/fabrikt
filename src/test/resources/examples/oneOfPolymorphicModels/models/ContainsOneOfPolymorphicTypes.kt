package examples.oneOfPolymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import kotlin.Any
import kotlin.collections.List

public data class ContainsOneOfPolymorphicTypes(
  @param:JsonProperty("one_one_of")
  @get:JsonProperty("one_one_of")
  public val oneOneOf: Any? = null,
  @param:JsonProperty("many_one_of")
  @get:JsonProperty("many_one_of")
  public val manyOneOf: List<@Valid ContainsOneOfPolymorphicTypesManyOneOf>? = null,
)
