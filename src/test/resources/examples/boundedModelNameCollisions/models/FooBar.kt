package examples.boundedModelNameCollisions.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import kotlin.Int

public data class FooBar(
  @param:JsonProperty(
    "size",
    required = true,
  )
  @get:JsonProperty("size")
  @get:NotNull
  @get:DecimalMin(
    value = "1",
    inclusive = true,
  )
  public val size: Int,
)
