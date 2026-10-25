package examples.normalizedNameConflation.models

import jakarta.validation.constraints.NotNull
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Child(
  @SerialName("${'$'}type")
  @get:NotNull
  public val `$type`: String,
  @SerialName("type")
  public val type: String? = null,
  @SerialName("value")
  public val `value`: String? = null,
)
