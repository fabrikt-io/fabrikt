package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull

public data class StateA(
  @get:JsonProperty("status")
  @get:NotNull
  @param:JsonProperty("status")
  public val status: Status = Status.A,
) : State
