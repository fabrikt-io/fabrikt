package examples.discriminatedOneOf.models

import jakarta.validation.Valid
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("CHILD_ONE")
@Serializable
public data class InheritedOneOfChildOne(
  @SerialName("effect")
  @get:Valid
  override val effect: InheritedOneOfParentEffect? = null,
  @SerialName("extraOne")
  public val extraOne: String? = null,
) : InheritedOneOfParent()
