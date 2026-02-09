package examples.oneOfMarkerInterface.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import javax.validation.constraints.NotNull
import kotlin.String

public data class StateB1(
  @get:JsonProperty("mode")
  @get:NotNull
  public val mode: String,
) : StateB
