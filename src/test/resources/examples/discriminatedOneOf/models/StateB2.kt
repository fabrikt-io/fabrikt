package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class StateB2(
  @param:JsonProperty("mode")
  @get:JsonProperty("mode")
  @get:NotNull
  public val mode: String,
) : StateB {
  @get:JsonProperty(
    value = "status",
    access = JsonProperty.Access.READ_ONLY,
  )
  @get:NotNull
  public val status: Status
    get() = Status.B2
}
