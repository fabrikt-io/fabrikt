package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull

public data class ErrorWrapperDiscriminated(
  @param:JsonProperty("error")
  @get:JsonProperty("error")
  @get:NotNull
  @get:Valid
  public val error: ErrorWrapperDiscriminatedError,
)
