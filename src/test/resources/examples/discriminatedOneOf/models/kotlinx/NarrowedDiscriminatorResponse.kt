package examples.discriminatedOneOf.models

import jakarta.validation.constraints.NotNull
import kotlin.OptIn
import kotlin.String
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class NarrowedDiscriminatorResponse(
  @SerialName("errorCode")
  @get:NotNull
  @EncodeDefault
  @OptIn(ExperimentalSerializationApi::class)
  public val errorCode: String = "ERR_ONE",
  @SerialName("message")
  @get:NotNull
  public val message: String,
)
