package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid

public data class ErrorWrapper(
  @param:JsonProperty("error")
  @get:JsonProperty("error")
  @get:Valid
  public val error: BaseError? = null,
)
