package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class InheritedOneOfChildOne(
  @param:JsonProperty("effect")
  @get:JsonProperty("effect")
  @get:Valid
  override val effect: InheritedOneOfParentEffect? = null,
  @param:JsonProperty("extraOne")
  @get:JsonProperty("extraOne")
  public val extraOne: String? = null,
) : InheritedOneOfParent(effect) {
  @get:JsonProperty(
    value = "actionType",
    access = JsonProperty.Access.READ_ONLY,
  )
  @get:NotNull
  override val actionType: String
    get() = "CHILD_ONE"
}
