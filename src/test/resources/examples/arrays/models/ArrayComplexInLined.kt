package examples.arrays.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlin.collections.List

public data class ArrayComplexInLined(
  @param:JsonProperty("quantities")
  @get:JsonProperty("quantities")
  @get:NotNull
  public val quantities: List<@Valid ArrayComplexInLinedQuantities>,
)
