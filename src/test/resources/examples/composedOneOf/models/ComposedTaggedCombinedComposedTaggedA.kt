package examples.composedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kotlin.String

public data class ComposedTaggedCombinedComposedTaggedA(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  @get:Size(min = 1)
  public val id: String,
  @param:JsonProperty("a")
  @get:JsonProperty("a")
  @get:NotNull
  public val a: String,
) : ComposedTaggedCombined {
  @get:JsonProperty(
    value = "kind",
    access = JsonProperty.Access.READ_ONLY,
  )
  @get:NotNull
  public val kind: ComposedTaggedAKind
    get() = ComposedTaggedAKind.A
}
