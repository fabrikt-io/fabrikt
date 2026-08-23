package examples.discriminatorMappingSuffix.models

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("CHILD")
@Serializable
public data class ChildActionA(
  @SerialName("fieldA")
  public val fieldA: String? = null,
) : ParentAction()
