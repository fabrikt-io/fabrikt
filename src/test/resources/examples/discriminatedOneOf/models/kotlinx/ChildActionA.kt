package examples.discriminatedOneOf.models

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ChildActionA(
  @SerialName("fieldA")
  public val fieldA: String? = null,
) : ParentAction()
