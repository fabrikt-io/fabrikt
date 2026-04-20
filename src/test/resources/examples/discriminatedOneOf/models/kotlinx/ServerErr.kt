package examples.discriminatedOneOf.models

import jakarta.validation.constraints.NotNull
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ServerErr(
  @SerialName("message")
  @get:NotNull
  public val message: String,
  @SerialName("errorType")
  @get:NotNull
  public val errorType: BaseErrorErrorType,
  @SerialName("stackTrace")
  @get:NotNull
  public val stackTrace: String,
) : ErrorWrapperError
