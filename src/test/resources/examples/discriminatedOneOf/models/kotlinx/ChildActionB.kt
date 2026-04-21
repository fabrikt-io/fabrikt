package examples.discriminatedOneOf.models

import kotlin.Int
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ChildActionB(
  @SerialName("fieldB")
  public val fieldB: Int? = null,
) : ParentAction(), ChildActionsAll
