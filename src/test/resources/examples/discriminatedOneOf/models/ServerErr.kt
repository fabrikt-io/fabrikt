package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class ServerErr(
  @param:JsonProperty("message")
  @get:JsonProperty("message")
  @get:NotNull
  public val message: String,
  @param:JsonProperty("errorType")
  @get:JsonProperty("errorType")
  @get:NotNull
  public val errorType: BaseErrorErrorType,
  @param:JsonProperty("stackTrace")
  @get:JsonProperty("stackTrace")
  @get:NotNull
  public val stackTrace: String,
)
