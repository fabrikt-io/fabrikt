package examples.discriminatedOneOf.models

import kotlin.Int
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("b")
@Serializable
public data class InheritedOneOfBranchB(
  @SerialName("bQty")
  public val bQty: Int? = null,
) : InheritedOneOfParentEffect
