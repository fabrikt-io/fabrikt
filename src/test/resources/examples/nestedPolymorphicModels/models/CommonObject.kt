package examples.nestedPolymorphicModels.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class CommonObject(
  @param:JsonProperty("filed1")
  @get:JsonProperty("filed1")
  @get:NotNull
  public val filed1: String,
  @param:JsonProperty("field2")
  @get:JsonProperty("field2")
  @get:NotNull
  public val field2: String,
)
