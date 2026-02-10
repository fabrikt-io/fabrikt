package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import javax.validation.constraints.NotNull
import kotlin.String

public data class StateB2(
  @get:JsonProperty("mode")
  @get:NotNull
  public val mode: String,
  @get:JsonProperty("status")
  @get:NotNull
  @param:JsonProperty("status")
  public val status: Status = Status.B2,
) : StateB
