package examples.discriminatedOneOf.models

import javax.validation.constraints.NotNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("b2")
@Serializable
public data class StateB2(
  @SerialName("mode")
  @get:NotNull
  public val mode: StateB2Mode,
) : StateB
