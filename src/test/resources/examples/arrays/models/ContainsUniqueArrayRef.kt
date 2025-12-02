package examples.arrays.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import java.util.LinkedHashSet
import javax.validation.Valid
import javax.validation.constraints.NotNull

public data class ContainsUniqueArrayRef(
  @param:JsonProperty("weight_on_mars")
  @get:JsonProperty("weight_on_mars")
  @get:NotNull
  @get:Valid
  public val weightOnMars: LinkedHashSet<ArrayRef>,
)
