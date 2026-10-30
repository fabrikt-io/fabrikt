package examples.arrays.models

import jakarta.validation.constraints.NotNull
import kotlin.Long
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ArraySimpleInLined(
  @SerialName("quantities")
  @get:NotNull
  public val quantities: List<Long>,
)
