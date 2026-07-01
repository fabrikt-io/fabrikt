package examples.openEnum.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class SomeObj(
  @SerialName("open_enum")
  public val openEnum: OpenEnum? = null,
  @SerialName("close_enum")
  public val closeEnum: CloseEnum? = null,
)
