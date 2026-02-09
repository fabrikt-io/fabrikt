package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import javax.validation.constraints.NotNull

public data class StateB1(
  @get:JsonProperty("mode")
  @get:NotNull
  public val mode: StateB1Mode,
  @get:JsonProperty("status")
  @get:NotNull
  @param:JsonProperty("status")
  public val status: Status = Status.B1,
) : StateB
