package examples.boundedModelNameCollisions.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kotlin.String

public data class FooBarExtra(
  @param:JsonProperty("name")
  @get:JsonProperty("name")
  @get:NotNull
  @get:Size(min = 2)
  public val name: String,
)
