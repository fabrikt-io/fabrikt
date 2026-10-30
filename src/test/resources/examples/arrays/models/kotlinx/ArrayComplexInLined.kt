package examples.arrays.models

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ArrayComplexInLined(
  @SerialName("quantities")
  @get:NotNull
  public val quantities: List<@Valid ArrayComplexInLinedQuantities>,
)
