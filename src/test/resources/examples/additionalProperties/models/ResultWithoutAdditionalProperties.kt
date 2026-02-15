package examples.additionalProperties.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class ResultWithoutAdditionalProperties(
  @param:JsonProperty("message")
  @get:JsonProperty("message")
  @get:NotNull
  public val message: String,
)
