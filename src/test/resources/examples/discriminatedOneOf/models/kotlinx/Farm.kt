package examples.discriminatedOneOf.models

import jakarta.validation.constraints.NotNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Farm(
  @SerialName("animal")
  @get:NotNull
  public val animal: Animal,
)
