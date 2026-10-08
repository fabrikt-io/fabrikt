package examples.composedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Int

public data class ComposedNamedB(
  @param:JsonProperty(
    "b",
    required = true,
  )
  @get:JsonProperty("b")
  @get:NotNull
  public val b: Int,
) : ComposedNamedVariant
