package examples.discriminatedOneOf.models

import jakarta.validation.Valid
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("CHILD_TWO")
@Serializable
public data class InheritedOneOfChildTwo(
  @SerialName("effect")
  @get:Valid
  override val effect: InheritedOneOfParentEffect? = null,
  @SerialName("extraTwo")
  public val extraTwo: String? = null,
) : InheritedOneOfParent()
