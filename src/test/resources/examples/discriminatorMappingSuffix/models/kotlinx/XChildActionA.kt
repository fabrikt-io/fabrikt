package examples.discriminatorMappingSuffix.models

import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("XCHILD")
@Serializable
public data class XChildActionA(
  @SerialName("fieldX")
  public val fieldX: String? = null,
) : ParentAction()
