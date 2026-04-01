package examples.openapi310.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Any
import kotlin.collections.List

public data class ContainsUntypedArray(
  @param:JsonProperty("untyped_array")
  @get:JsonProperty("untyped_array")
  @get:NotNull
  public val untypedArray: List<Any?>,
)
