package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class InheritedOneOfChildTwo(
  @param:JsonProperty("effect")
  @get:JsonProperty("effect")
  @get:Valid
  override val effect: InheritedOneOfParentEffect? = null,
  @param:JsonProperty("extraTwo")
  @get:JsonProperty("extraTwo")
  public val extraTwo: String? = null,
) : InheritedOneOfParent(effect) {
  @get:JsonProperty(
    value = "actionType",
    access = JsonProperty.Access.READ_ONLY,
  )
  @get:NotNull
  override val actionType: String
    get() = "CHILD_TWO"
}
