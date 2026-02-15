package examples.responsesSchema.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Int
import kotlin.String

public data class ErrorResponse(
  @param:JsonProperty("message")
  @get:JsonProperty("message")
  @get:NotNull
  public val message: String,
  @param:JsonProperty(
    "code",
    required = true,
  )
  @get:JsonProperty("code")
  @get:NotNull
  public val code: Int,
)
