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
  @get:JsonProperty("actionType")
  @get:NotNull
  @param:JsonProperty("actionType")
  override val actionType: String = "CHILD_TWO",
) : InheritedOneOfParent(effect)
