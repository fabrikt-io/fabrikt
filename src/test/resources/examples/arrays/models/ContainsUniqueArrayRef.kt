package examples.arrays.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import java.util.LinkedHashSet

public data class ContainsUniqueArrayRef(
  @param:JsonProperty("weight_on_mars")
  @get:JsonProperty("weight_on_mars")
  @get:NotNull
  @get:Valid
  public val weightOnMars: LinkedHashSet<ArrayRef>,
)
