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
  @get:JsonProperty("actionType")
  @get:NotNull
  @param:JsonProperty("actionType")
  override val actionType: String = "CHILD_ONE",
) : InheritedOneOfParent(effect)
