package examples.arrays.models

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlin.collections.LinkedHashSet
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ContainsUniqueArrayRef(
  @SerialName("weight_on_mars")
  @get:NotNull
  public val weightOnMars: LinkedHashSet<@Valid ArrayRef>,
)
