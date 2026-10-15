package examples.sharedCompositionMappedContracts.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import kotlin.Int

public data class Details(
  @param:JsonProperty(
    "value",
    required = true,
  )
  @get:JsonProperty("value")
  @get:NotNull
  @get:DecimalMin(
    value = "1",
    inclusive = true,
  )
  public val `value`: Int,
)
