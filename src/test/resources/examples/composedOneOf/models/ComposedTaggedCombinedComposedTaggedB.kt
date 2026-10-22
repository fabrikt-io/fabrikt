package examples.composedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kotlin.Int
import kotlin.String

public data class ComposedTaggedCombinedComposedTaggedB(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  @get:Size(min = 1)
  public val id: String,
  @param:JsonProperty(
    "b",
    required = true,
  )
  @get:JsonProperty("b")
  @get:NotNull
  public val b: Int,
) : ComposedTaggedCombined {
  @get:JsonProperty(
    value = "kind",
    access = JsonProperty.Access.READ_ONLY,
  )
  @get:NotNull
  public val kind: ComposedTaggedBKind
    get() = ComposedTaggedBKind.B
}
