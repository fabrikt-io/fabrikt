package examples.discriminatedOneOf.models

import javax.validation.constraints.NotNull
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("b2")
@Serializable
public data class StateB2(
  @SerialName("mode")
  @get:NotNull
  public val mode: String,
) : StateB
