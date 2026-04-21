package examples.discriminatedOneOf.models

import jakarta.validation.constraints.NotNull
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class NarrowedDiscriminatorResponse(
  @SerialName("errorCode")
  @get:NotNull
  public val errorCode: String,
  @SerialName("message")
  @get:NotNull
  public val message: String,
)
