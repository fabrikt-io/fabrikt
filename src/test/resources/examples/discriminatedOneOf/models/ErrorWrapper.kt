package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.Any

public data class ErrorWrapper(
  @param:JsonProperty("error")
  @get:JsonProperty("error")
  public val error: Any? = null,
)
