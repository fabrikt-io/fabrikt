package examples.arrays.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Long
import kotlin.collections.List

public data class ArraySimpleInLined(
  @param:JsonProperty("quantities")
  @get:JsonProperty("quantities")
  @get:NotNull
  public val quantities: List<Long>,
)
