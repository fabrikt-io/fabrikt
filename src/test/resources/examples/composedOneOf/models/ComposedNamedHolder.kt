package examples.composedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Any

public data class ComposedNamedHolder(
  @param:JsonProperty("value")
  @get:JsonProperty("value")
  @get:NotNull
  public val `value`: Any,
)
