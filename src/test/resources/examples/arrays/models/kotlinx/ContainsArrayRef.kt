package examples.arrays.models

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlin.collections.List
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ContainsArrayRef(
  @SerialName("weight_on_mars")
  @get:NotNull
  public val weightOnMars: List<@Valid ArrayRef>,
)
