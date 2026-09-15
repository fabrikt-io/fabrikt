package examples.discriminatedOneOf.models

import kotlin.Int
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("a")
@Serializable
public data class InheritedOneOfBranchA(
  @SerialName("aQty")
  public val aQty: Int? = null,
) : InheritedOneOfParentEffect
