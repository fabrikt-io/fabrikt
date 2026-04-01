package examples.oneOfPolymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import kotlin.collections.List

public data class ContainsOneOfPolymorphicTypes(
  @param:JsonProperty("one_one_of")
  @get:JsonProperty("one_one_of")
  @get:Valid
  public val oneOneOf: PolymorphicSuperTypeOne? = null,
  @param:JsonProperty("many_one_of")
  @get:JsonProperty("many_one_of")
  @get:Valid
  public val manyOneOf: List<PolymorphicSuperTypeOne>? = null,
)
