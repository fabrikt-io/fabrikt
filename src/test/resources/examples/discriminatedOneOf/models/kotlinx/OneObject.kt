package examples.discriminatedOneOf.models

import javax.validation.constraints.NotNull
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class OneObject(
  /**
   * Type Property
   */
  @SerialName("type")
  @get:NotNull
  public val type: String,
)
