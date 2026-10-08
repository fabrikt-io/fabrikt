package examples.composedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class ComposedSiblingA(
  @param:JsonProperty("a")
  @get:JsonProperty("a")
  @get:NotNull
  public val a: String,
)
