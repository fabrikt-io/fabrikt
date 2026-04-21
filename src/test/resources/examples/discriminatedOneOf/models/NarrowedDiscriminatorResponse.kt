package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class NarrowedDiscriminatorResponse(
  @param:JsonProperty("errorCode")
  @get:JsonProperty("errorCode")
  @get:NotNull
  public val errorCode: String,
  @param:JsonProperty("message")
  @get:JsonProperty("message")
  @get:NotNull
  public val message: String,
)
