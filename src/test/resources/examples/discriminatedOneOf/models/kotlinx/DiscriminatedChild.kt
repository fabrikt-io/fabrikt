package examples.discriminatedOneOf.models

import jakarta.validation.constraints.NotNull
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("ERR_ONE")
@Serializable
public data class DiscriminatedChild(
  @SerialName("message")
  @get:NotNull
  override val message: String,
) : DiscriminatedBase()
