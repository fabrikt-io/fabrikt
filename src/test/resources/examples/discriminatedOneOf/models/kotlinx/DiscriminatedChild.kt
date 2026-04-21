package examples.discriminatedOneOf.models

import jakarta.validation.constraints.NotNull
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class DiscriminatedChild(
  @SerialName("message")
  @get:NotNull
  override val message: String,
) : DiscriminatedBase(message)
