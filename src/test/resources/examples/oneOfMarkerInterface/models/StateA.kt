package examples.oneOfMarkerInterface.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class StateA(
  @param:JsonProperty("status")
  @get:JsonProperty("status")
  @get:NotNull
  public val status: String,
) : State
