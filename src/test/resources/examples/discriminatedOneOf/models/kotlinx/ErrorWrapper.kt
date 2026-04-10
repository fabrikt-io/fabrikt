package examples.discriminatedOneOf.models

import jakarta.validation.Valid
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ErrorWrapper(
  @SerialName("error")
  @get:Valid
  public val error: BaseError? = null,
)
