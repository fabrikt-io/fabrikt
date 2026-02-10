package examples.discriminatedOneOf.models

import javax.validation.constraints.NotNull
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("b1")
@Serializable
public data class StateB1(
  @SerialName("mode")
  @get:NotNull
  public val mode: String,
) : StateB
