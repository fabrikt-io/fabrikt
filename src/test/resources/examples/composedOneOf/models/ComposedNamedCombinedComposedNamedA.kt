package examples.composedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kotlin.String

public data class ComposedNamedCombinedComposedNamedA(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  @get:Size(min = 1)
  public val id: String,
  @param:JsonProperty("a")
  @get:JsonProperty("a")
  @get:NotNull
  public val a: String,
) : ComposedNamedCombined
