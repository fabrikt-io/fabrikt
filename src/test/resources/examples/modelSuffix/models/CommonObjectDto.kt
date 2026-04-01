package examples.modelSuffix.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class CommonObjectDto(
  @param:JsonProperty("field1")
  @get:JsonProperty("field1")
  @get:NotNull
  public val field1: String,
  @param:JsonProperty("field2")
  @get:JsonProperty("field2")
  @get:NotNull
  public val field2: String,
)
